package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import com.deutscheboerse.risk.dave.model.PositionReportModel;
import com.deutscheboerse.risk.dave.utils.DummyData;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

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

    @Test
    public void testHealth(TestContext context) throws InterruptedException {
        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(httpPort, "localhost", "/healthz", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                try {
                    String response = body.toString();
                    context.assertEquals("ok", response);
                    asyncRest.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            });
        });
    }

    @Test
    public void testReadinessOk(TestContext context) throws InterruptedException {
        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(httpPort, "localhost", "/readiness", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                try {
                    String response = body.toString();
                    context.assertEquals("ok", response);
                    asyncRest.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            });
        });
    }

    @Test
    public void testReadinessNok(TestContext context) throws InterruptedException {
        final Async asyncRest = context.async();

        HealthCheck healthCheck = new HealthCheck(vertx);
        healthCheck.setHttpState(false);

        vertx.createHttpClient().getNow(httpPort, "localhost", "/readiness", res -> {
            context.assertEquals(503, res.statusCode());
            res.bodyHandler(body -> {
                try {
                    String response = body.toString();
                    context.assertEquals("nok", response);
                    healthCheck.setHttpState(true);
                    asyncRest.complete();
                }
                catch (Exception e)
                {
                    healthCheck.setHttpState(true);
                    context.fail(e);
                }
            });
        });
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        HealthCheckIT.vertx.close(context.asyncAssertSuccess());
    }
}
