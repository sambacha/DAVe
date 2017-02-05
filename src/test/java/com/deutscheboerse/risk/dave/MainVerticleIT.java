package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.utils.DummyData;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

@RunWith(VertxUnitRunner.class)
public class MainVerticleIT {
    private static Vertx vertx;
    private static int httpPort;
    private static int mongoPort;
    private static MongoClient mongoClient;

    @BeforeClass
    public static void setUp(TestContext context) {
        vertx = Vertx.vertx();

        httpPort = Integer.getInteger("http.port", 8080);
        mongoPort = Integer.getInteger("mongodb.port", 27017);

        JsonObject config = new JsonObject();
        config.put("http", new JsonObject().put("httpPort", httpPort));
        config.put("mongodb", new JsonObject().put("dbName", "DAVe-MainVerticleTest").put("connectionUrl", "mongodb://localhost:" + mongoPort));
        vertx.deployVerticle(MainVerticle.class.getName(), new DeploymentOptions().setConfig(config), context.asyncAssertSuccess());

        JsonObject dbConfig = new JsonObject();
        dbConfig.put("db_name", "DAVe-Test" + UUID.randomUUID().getLeastSignificantBits());
        dbConfig.put("connection_string", "mongodb://localhost:" + System.getProperty("mongodb.port", "27017"));
        MainVerticleIT.mongoClient = MongoClient.createShared(MainVerticleIT.vertx, dbConfig);
    }

    private void sendDummyData(TestContext context) {
        DummyData.positionReportJson.forEach(pr -> {
            final Async asyncSend = context.async();
            vertx.eventBus().send("ers.PositionReport", pr, ar -> {
                context.assertTrue(ar.succeeded());
                asyncSend.complete();
            });

            asyncSend.awaitSuccess();
        });
    }

    @Test
    public void testPositionReport(TestContext context) throws InterruptedException {
        sendDummyData(context);

        Thread.sleep(1000);

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(httpPort, "localhost", "/api/v1.0/pr/latest/ABCFR/ABCFR/A1/*/OGBL/C/10800/0/201401", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                try {
                    JsonArray positions = body.toJsonArray();

                    context.assertEquals(1, positions.size());

                    JsonObject pos = positions.getJsonObject(0);

                    context.assertEquals("ABCFR", pos.getString("clearer"));
                    context.assertEquals("ABCFR", pos.getString("member"));
                    context.assertNull(pos.getString("reqID"));
                    context.assertEquals("A1", pos.getString("account"));
                    //context.assertEquals("ITD", pos.getString("sesId"));
                    context.assertEquals("13198434645156", pos.getString("rptId"));
                    context.assertEquals("C", pos.getString("putCall"));
                    context.assertEquals("201401", pos.getString("maturityMonthYear"));
                    context.assertEquals("10800", pos.getString("strikePrice"));
                    context.assertEquals("OGBL", pos.getString("symbol"));
                    context.assertEquals(700.0, pos.getDouble("crossMarginLongQty"));
                    context.assertEquals(800.0, pos.getDouble("crossMarginShortQty"));
                    context.assertEquals(0.0, pos.getDouble("optionExcerciseQty"));
                    context.assertEquals(0.0, pos.getDouble("optionAssignmentQty"));
                    context.assertNull(pos.getDouble("allocationTradeQty"));
                    context.assertNull(pos.getDouble("deliveryNoticeQty"));
                    asyncRest.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            });
        });
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        MainVerticleIT.vertx.close(context.asyncAssertSuccess());
    }
}
