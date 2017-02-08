package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * Created by schojak on 8.2.17.
 */
@RunWith(VertxUnitRunner.class)
public class HealthCheckTest {
    private final static String MAP_NAME = "healthCheck";
    private final static String HTTP_KEY = "httpReady";
    private final static String MONGO_KEY = "mongoReady";

    //private static Vertx vertx;

    /*@BeforeClass
    public static void setUp(TestContext context) throws IOException {
        HealthCheckTest.vertx = Vertx.vertx();
    }*/

    @Test
    public void testInitialization(TestContext context) {
        Vertx vertx = Vertx.vertx();

        HealthCheck healthCheck = new HealthCheck(vertx).initialize();

        context.assertNotNull(vertx.sharedData().getLocalMap(MAP_NAME).get(MONGO_KEY), "Mongo readiness should not be null");
        context.assertNotNull(vertx.sharedData().getLocalMap(MAP_NAME).get(HTTP_KEY), "HTTP readiness should not be null");
        context.assertFalse(healthCheck.getMongoState(), "Mongo readiness should return false");
        context.assertFalse(healthCheck.getHttpState(), "Http readiness should return false");
        context.assertFalse((Boolean) vertx.sharedData().getLocalMap(MAP_NAME).get(MONGO_KEY), "Mongo readiness should be initialized to false");
        context.assertFalse((Boolean) vertx.sharedData().getLocalMap(MAP_NAME).get(HTTP_KEY), "HTTP readiness should be initialized to false");

        vertx.close();
    }

    @Test
    public void testMongoReadyness(TestContext context) {
        Vertx vertx = Vertx.vertx();

        HealthCheck healthCheck = new HealthCheck(vertx).initialize().setMongoState(true);

        context.assertTrue(healthCheck.getMongoState(), "Mongo readiness should return true");
        context.assertTrue((Boolean) vertx.sharedData().getLocalMap(MAP_NAME).get(MONGO_KEY), "Mongo readiness should equal true in shared data");

        vertx.close();
    }

    @Test
    public void testHttpReadyness(TestContext context) {
        Vertx vertx = Vertx.vertx();

        HealthCheck healthCheck = new HealthCheck(vertx).initialize().setHttpState(true);

        context.assertTrue(healthCheck.getHttpState(), "Http readiness should return true");
        context.assertTrue((Boolean) vertx.sharedData().getLocalMap(MAP_NAME).get(HTTP_KEY), "Http readiness should equal true in shared data");

        vertx.close();
    }

    @Test
    public void testReadiness(TestContext context) {
        Vertx vertx;

        vertx = Vertx.vertx();
        context.assertFalse(new HealthCheck(vertx).initialize().ready(), "Nothing is ready, should retrun false");
        vertx.close();

        vertx = Vertx.vertx();
        context.assertFalse(new HealthCheck(vertx).initialize().setHttpState(true).ready(), "Only HTTP is ready, not the whole application");
        vertx.close();

        vertx = Vertx.vertx();
        context.assertFalse(new HealthCheck(vertx).initialize().setMongoState(true).ready(), "Only Mongo is ready, not the whole application");
        vertx.close();

        vertx = Vertx.vertx();
        context.assertTrue(new HealthCheck(vertx).initialize().setMongoState(true).setHttpState(true).ready(), "Everything is ready, the whole app should ne ready");
        vertx.close();
    }

    /*@AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }*/
}
