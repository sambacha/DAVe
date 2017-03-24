package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
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

@RunWith(VertxUnitRunner.class)
public class HealthCheckIT {
    private static Vertx vertx;
    private static int httpPort;
    private static int mongoPort;

    @BeforeClass
    public static void setUp(TestContext context) {
        vertx = Vertx.vertx();

        httpPort = Integer.getInteger("http.port", 8080);
        mongoPort = Integer.getInteger("mongodb.port", 27017);

        JsonObject config = new JsonObject();
        config.put("http", new JsonObject().put("port", httpPort));
        config.put("mongodb", new JsonObject().put("dbName", "DAVe-MainVerticleTest").put("connectionUrl", "mongodb://localhost:" + mongoPort));
        vertx.deployVerticle(MainVerticle.class.getName(), new DeploymentOptions().setConfig(config), context.asyncAssertSuccess());
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
    public void testHealth(TestContext context) throws InterruptedException {
        JsonObject expected = new JsonObject()
                .put("checks", new JsonArray().add(new JsonObject()
                .put("id", "healthz")
                .put("status", "UP")))
                .put("outcome", "UP");
        vertx.createHttpClient().getNow(httpPort, "localhost", HttpVerticle.REST_HEALTHZ,
                assertEqualsHttpHandler(200, expected.encode(), context));
    }

    @Test
    public void testReadinessOk(TestContext context) throws InterruptedException {
        JsonObject expected = new JsonObject()
                .put("checks", new JsonArray().add(new JsonObject()
                .put("id", "readiness")
                .put("status", "UP")))
                .put("outcome", "UP");
        vertx.createHttpClient().getNow(httpPort, "localhost", HttpVerticle.REST_READINESS,
            assertEqualsHttpHandler(200, expected.encode(), context));
    }

    @Test
    public void testReadinessNok(TestContext context) throws InterruptedException {
        HealthCheck healthCheck = new HealthCheck(vertx);
        healthCheck.setComponentFailed(HTTP);

        JsonObject expected = new JsonObject()
                .put("checks", new JsonArray().add(new JsonObject()
                .put("id", "readiness")
                .put("status", "DOWN")))
                .put("outcome", "DOWN");
        vertx.createHttpClient().getNow(httpPort, "localhost", HttpVerticle.REST_READINESS,
                assertEqualsHttpHandler(503, expected.encode(), context));
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        HealthCheckIT.vertx.close(context.asyncAssertSuccess());
    }
}
