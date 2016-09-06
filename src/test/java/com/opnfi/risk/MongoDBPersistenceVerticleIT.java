package com.opnfi.risk;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MongoDBPersistenceVerticleIT {
    private final DateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static Vertx vertx;
    private static MongoClient mongoClient;

    @BeforeClass
    public static void setUp(TestContext context) {
        MongoDBPersistenceVerticleIT.vertx = Vertx.vertx();
        JsonObject config = new JsonObject();
        config.put("db_name", "OpnFi-Risk-Test" + UUID.randomUUID().getLeastSignificantBits());
        config.put("connection_string", "mongodb://localhost:" + System.getProperty("mongodb.port", "27017"));
        DeploymentOptions options = new DeploymentOptions().setConfig(config);
        MongoDBPersistenceVerticleIT.vertx.deployVerticle(MongoDBPersistenceVerticle.class.getName(), options, context.asyncAssertSuccess());
        MongoDBPersistenceVerticleIT.mongoClient = MongoClient.createShared(MongoDBPersistenceVerticleIT.vertx, config);
    }

    @Test
    public void checkCollectionsExist(TestContext context) {
        List<String> requiredCollections = new ArrayList<>();
        requiredCollections.add("ers.TradingSessionStatus");
        requiredCollections.add("ers.MarginComponent");
        requiredCollections.add("ers.TotalMarginRequirement");
        requiredCollections.add("ers.MarginShortfallSurplus");
        requiredCollections.add("ers.PositionReport");
        requiredCollections.add("ers.RiskLimit");
        final Async async = context.async();
        MongoDBPersistenceVerticleIT.mongoClient.getCollections(ar -> {
            if (ar.succeeded()) {
                if (ar.result().containsAll(requiredCollections)) {
                    async.complete();
                } else {
                    requiredCollections.removeAll(ar.result());
                    context.fail("Following collections were not created: " + requiredCollections);
                }
            } else {
                context.fail(ar.cause());
            }
        });
    }

    @Test
    public void testStoreTradingSessionStatus(TestContext context) {
        final Async asyncStore = context.async();
        JsonObject tradingSessionStatus = new JsonObject();
        tradingSessionStatus.put("received", new JsonObject().put("$date", this.timestampFormatter.format(new Date())));
        tradingSessionStatus.put("reqId", "REQID");
        tradingSessionStatus.put("sesId", "SESID");
        tradingSessionStatus.put("stat", "STAT");
        tradingSessionStatus.put("statRejRsn", "STATREJRSN");
        tradingSessionStatus.put("txt", "TXT");
        MongoDBPersistenceVerticleIT.vertx.eventBus().send("ers.TradingSessionStatus", tradingSessionStatus, ar -> {
            context.assertTrue(ar.succeeded());
            asyncStore.complete();
        });
        asyncStore.awaitSuccess();
        final Async asyncFind = context.async();
        MongoDBPersistenceVerticleIT.mongoClient.findOne("ers.TradingSessionStatus", tradingSessionStatus, null, ar -> {
            if (ar.succeeded()) {
                context.assertEquals("REQID", ar.result().getString("reqId"));
                context.assertEquals("SESID", ar.result().getString("sesId"));
                context.assertEquals("STAT", ar.result().getString("stat"));
                context.assertEquals("STATREJRSN", ar.result().getString("statRejRsn"));
                context.assertEquals("TXT", ar.result().getString("txt"));
                asyncFind.complete();
            } else {
                context.fail("Unable to find find TradingSessionStatus document");
            }
        });
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        MongoDBPersistenceVerticleIT.vertx.close(context.asyncAssertSuccess());
    }
}
