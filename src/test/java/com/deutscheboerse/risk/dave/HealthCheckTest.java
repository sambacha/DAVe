package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.deutscheboerse.risk.dave.healthcheck.HealthCheck.Component.HTTP;
import static com.deutscheboerse.risk.dave.healthcheck.HealthCheck.Component.MONGO;

/**
 * @author Created by schojak on 8.2.17.
 */
@RunWith(VertxUnitRunner.class)
public class HealthCheckTest {
    private final static String MAP_NAME = "healthCheck";
    private final static String HTTP_KEY = "HTTP";
    private final static String MONGO_KEY = "MONGO";

    //private static Vertx vertx;

    /*@BeforeClass
    public static void setUp(TestContext context) throws IOException {
        HealthCheckTest.vertx = Vertx.vertx();
    }*/

    @Test
    public void testInitialization(TestContext context) {
        Vertx vertx = Vertx.vertx();

        HealthCheck healthCheck = new HealthCheck(vertx);

        for (HealthCheck.Component component: HealthCheck.Component.values()) {
            context.assertFalse(healthCheck.isComponentReady(component), component.name() + " readiness should be initialized to false");
        }

        context.assertFalse(healthCheck.ready(), "Initial state of healthCheck should be false");

        vertx.close();
    }

    @Test
    public void testMongoReadiness(TestContext context) {
        Vertx vertx = Vertx.vertx();

        HealthCheck healthCheck = new HealthCheck(vertx).setComponentReady(MONGO);

        context.assertTrue(healthCheck.isComponentReady(MONGO), "Mongo readiness should return true");
        context.assertTrue((Boolean) vertx.sharedData().getLocalMap(MAP_NAME).get(MONGO_KEY), "Mongo readiness should equal true in shared data");

        vertx.close();
    }

    @Test
    public void testHttpReadiness(TestContext context) {
        Vertx vertx = Vertx.vertx();

        HealthCheck healthCheck = new HealthCheck(vertx).setComponentReady(HTTP);

        context.assertTrue(healthCheck.isComponentReady(HTTP), "Http readiness should return true");
        context.assertTrue((Boolean) vertx.sharedData().getLocalMap(MAP_NAME).get(HTTP_KEY), "Http readiness should equal true in shared data");

        vertx.close();
    }

    @Test
    public void testReadiness(TestContext context) {
        Vertx vertx;

        vertx = Vertx.vertx();
        context.assertFalse(new HealthCheck(vertx).ready(), "Nothing is ready, should return false");
        vertx.close();

        vertx = Vertx.vertx();
        context.assertFalse(new HealthCheck(vertx).setComponentReady(HTTP).ready(), "Only HTTP is ready, not the whole application");
        vertx.close();

        vertx = Vertx.vertx();
        context.assertFalse(new HealthCheck(vertx).setComponentReady(MONGO).ready(), "Only Mongo is ready, not the whole application");
        vertx.close();

        vertx = Vertx.vertx();
        context.assertTrue(new HealthCheck(vertx).setComponentReady(MONGO).setComponentReady(HTTP).ready(), "Everything is ready, the whole app should ne ready");
        vertx.close();
    }

    /*@AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }*/
}
