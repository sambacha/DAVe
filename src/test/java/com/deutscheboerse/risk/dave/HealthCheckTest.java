package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import com.deutscheboerse.risk.dave.persistence.EchoPersistenceService;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import com.deutscheboerse.risk.dave.utils.TestConfig;
import com.google.inject.AbstractModule;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.deutscheboerse.risk.dave.healthcheck.HealthCheck.Component.HTTP;
import static com.deutscheboerse.risk.dave.healthcheck.HealthCheck.Component.PERSISTENCE_SERVICE;

@RunWith(VertxUnitRunner.class)
public class HealthCheckTest {
    private final static String MAP_NAME = "healthCheck";
    private final static String HTTP_KEY = "HTTP";
    private final static String PERSISTENCE_KEY = "PERSISTENCE_SERVICE";

    private static Vertx vertx;

    @BeforeClass
    public static void setUp(TestContext context) {
        vertx = Vertx.vertx();

        JsonObject config = TestConfig.getGlobalConfig();
        config.put("guice_binder", HealthCheckTest.EchoBinder.class.getName());
        DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(config);

        vertx.deployVerticle(MainVerticle.class.getName(), deploymentOptions, context.asyncAssertSuccess());
    }

    private Handler<HttpClientResponse> assertEqualsHttpHandler(int expectedCode, String expectedText, TestContext context) {
        final Async async = context.async();
        return response -> {
            context.assertEquals(expectedCode, response.statusCode());
            response.bodyHandler(body -> {
                try {
                    context.assertEquals(expectedText, body.toString());
                    async.complete();
                } catch (Exception e) {
                    context.fail(e);
                }
            });
        };
    }

    @Test
    public void testUnitInitialization(TestContext context) {
        Vertx vertx = Vertx.vertx();

        HealthCheck healthCheck = new HealthCheck(vertx);

        for (HealthCheck.Component component: HealthCheck.Component.values()) {
            context.assertFalse(healthCheck.isComponentReady(component), component.name() + " readiness should be initialized to false");
        }

        context.assertFalse(healthCheck.ready(), "Initial state of healthCheck should be false");

        vertx.close();
    }

    @Test
    public void testUnitPersistenceReadiness(TestContext context) {
        Vertx vertx = Vertx.vertx();

        HealthCheck healthCheck = new HealthCheck(vertx).setComponentReady(PERSISTENCE_SERVICE);

        context.assertTrue(healthCheck.isComponentReady(PERSISTENCE_SERVICE), "Persistence readiness should return true");
        context.assertTrue((Boolean) vertx.sharedData().getLocalMap(MAP_NAME).get(PERSISTENCE_KEY), "Persistence readiness should equal true in shared data");

        vertx.close();
    }

    @Test
    public void testUnitHttpReadiness(TestContext context) {
        Vertx vertx = Vertx.vertx();

        HealthCheck healthCheck = new HealthCheck(vertx).setComponentReady(HTTP);

        context.assertTrue(healthCheck.isComponentReady(HTTP), "Http readiness should return true");
        context.assertTrue((Boolean) vertx.sharedData().getLocalMap(MAP_NAME).get(HTTP_KEY), "Http readiness should equal true in shared data");

        vertx.close();
    }

    @Test
    public void testUnitReadiness(TestContext context) {
        Vertx vertx;

        vertx = Vertx.vertx();
        context.assertFalse(new HealthCheck(vertx).ready(), "Nothing is ready, should return false");
        vertx.close();

        vertx = Vertx.vertx();
        context.assertFalse(new HealthCheck(vertx).setComponentReady(HTTP).ready(), "Only HTTP is ready, not the whole application");
        vertx.close();

        vertx = Vertx.vertx();
        context.assertFalse(new HealthCheck(vertx).setComponentReady(PERSISTENCE_SERVICE).ready(), "Only Persistence is ready, not the whole application");
        vertx.close();

        vertx = Vertx.vertx();
        context.assertTrue(new HealthCheck(vertx).setComponentReady(PERSISTENCE_SERVICE).setComponentReady(HTTP).ready(), "Everything is ready, the whole app should ne ready");
        vertx.close();
    }

    @Test
    public void testApplicationHealth(TestContext context) throws InterruptedException {
        JsonObject expected = new JsonObject()
                .put("checks", new JsonArray().add(new JsonObject()
                        .put("id", "healthz")
                        .put("status", "UP")))
                .put("outcome", "UP");
        vertx.createHttpClient().getNow(TestConfig.HTTP_PORT, "localhost", HttpVerticle.REST_HEALTHZ,
                assertEqualsHttpHandler(200, expected.encode(), context));
    }

    @Test
    public void testApplicationReadinessOk(TestContext context) throws InterruptedException {
        JsonObject expected = new JsonObject()
                .put("checks", new JsonArray().add(new JsonObject()
                        .put("id", "readiness")
                        .put("status", "UP")))
                .put("outcome", "UP");
        vertx.createHttpClient().getNow(TestConfig.HTTP_PORT, "localhost", HttpVerticle.REST_READINESS,
                assertEqualsHttpHandler(200, expected.encode(), context));
    }

    @Test
    public void testApplicationReadinessNok(TestContext context) throws InterruptedException {
        HealthCheck healthCheck = new HealthCheck(vertx);
        healthCheck.setComponentFailed(HTTP);

        JsonObject expected = new JsonObject()
                .put("checks", new JsonArray().add(new JsonObject()
                        .put("id", "readiness")
                        .put("status", "DOWN")))
                .put("outcome", "DOWN");
        vertx.createHttpClient().getNow(TestConfig.HTTP_PORT, "localhost", HttpVerticle.REST_READINESS,
                assertEqualsHttpHandler(503, expected.encode(), context));
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        HealthCheckTest.vertx.close(context.asyncAssertSuccess());
    }

    public static class EchoBinder extends AbstractModule {
        @Override
        protected void configure() {
            bind(PersistenceService.class).to(EchoPersistenceService.class);
        }
    }
}
