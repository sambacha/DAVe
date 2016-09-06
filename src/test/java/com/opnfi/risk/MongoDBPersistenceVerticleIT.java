package com.opnfi.risk;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
    private final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
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

    @Test
    public void testStorePositionReport(TestContext context) {
        final Async asyncStore = context.async();
        JsonObject positionReport = new JsonObject();
        positionReport.put("clearer", "CLEARER");
        positionReport.put("member", "MEMBER");
        positionReport.put("account", "ACCOUNT");
        positionReport.put("txnTm", new JsonObject().put("$date", "2013-12-18T14:56:58.100Z"));
        positionReport.put("bizDt", new JsonObject().put("$date", "2013-12-17T00:00:00.000Z"));
        positionReport.put("reqId", "REQID");
        positionReport.put("rptId", "RPTID");
        positionReport.put("sesId", "SESSID");
        positionReport.put("symbol", "SYMBOL");
        positionReport.put("putCall", "PUTCALL");
        positionReport.put("maturityMonthYear", "MMY");
        positionReport.put("strikePrice", "STRIKE");
        positionReport.put("optAttribute", "OPTAT");
        positionReport.put("crossMarginLongQty", 10);
        positionReport.put("crossMarginShortQty", 10);
        positionReport.put("optionExcerciseQty", 10);
        positionReport.put("optionAssignmentQty", 10);
        positionReport.put("allocationTradeQty", 10);
        positionReport.put("deliveryNoticeQty", 10);
        positionReport.put("received", new JsonObject().put("$date", this.timestampFormatter.format(new Date())));

        MongoDBPersistenceVerticleIT.vertx.eventBus().send("ers.PositionReport", positionReport, ar -> {
            context.assertTrue(ar.succeeded());
            asyncStore.complete();
        });
        asyncStore.awaitSuccess();
        final Async asyncFind = context.async();
        MongoDBPersistenceVerticleIT.mongoClient.findOne("ers.PositionReport", positionReport, null, ar -> {
            if (ar.succeeded()) {
                context.assertEquals(ar.result().getString("clearer"), "CLEARER");
                context.assertEquals(ar.result().getString("member"), "MEMBER");
                context.assertEquals(ar.result().getString("account"), "ACCOUNT");
                context.assertEquals(ar.result().getJsonObject("txnTm"), new JsonObject().put("$date", "2013-12-18T14:56:58.1Z"));
                context.assertEquals(ar.result().getJsonObject("bizDt"), new JsonObject().put("$date", "2013-12-17T00:00:00Z"));
                context.assertEquals(ar.result().getString("reqId"), "REQID");
                context.assertEquals(ar.result().getString("rptId"), "RPTID");
                context.assertEquals(ar.result().getString("sesId"), "SESSID");
                context.assertEquals(ar.result().getString("symbol"), "SYMBOL");
                context.assertEquals(ar.result().getString("putCall"), "PUTCALL");
                context.assertEquals(ar.result().getString("maturityMonthYear"), "MMY");
                context.assertEquals(ar.result().getString("strikePrice"), "STRIKE");
                context.assertEquals(ar.result().getString("optAttribute"), "OPTAT");
                context.assertEquals(ar.result().getInteger("crossMarginLongQty"), 10);
                context.assertEquals(ar.result().getInteger("crossMarginShortQty"), 10);
                context.assertEquals(ar.result().getInteger("optionExcerciseQty"), 10);
                context.assertEquals(ar.result().getInteger("optionAssignmentQty"), 10);
                context.assertEquals(ar.result().getInteger("allocationTradeQty"), 10);
                context.assertEquals(ar.result().getInteger("deliveryNoticeQty"), 10);
                asyncFind.complete();
            } else {
                context.fail("Unable to find find PositionReport document");
            }
        });
    }

    @Test
    public void testStoreMarginComponent(TestContext context) {
        final Async asyncStore = context.async();
        JsonObject marginComponent = new JsonObject();
        marginComponent.put("clearer", "CLEARER");
        marginComponent.put("member", "MEMBER");
        marginComponent.put("account", "ACCOUNT");
        marginComponent.put("clss", "CLASS");
        marginComponent.put("ccy", "CURRENCY");
        marginComponent.put("txnTm", new JsonObject().put("$date", "2013-12-18T14:56:58.100Z"));
        marginComponent.put("bizDt", new JsonObject().put("$date", "2013-12-17T00:00:00.000Z"));
        marginComponent.put("reqId", "REQID");
        marginComponent.put("rptId", "RPTID");
        marginComponent.put("sesId", "SESSID");
        marginComponent.put("variationMargin", 10.0);
        marginComponent.put("premiumMargin", 10.0);
        marginComponent.put("liquiMargin", 10.0);
        marginComponent.put("spreadMargin", 10.0);
        marginComponent.put("additionalMargin", 10.0);
        marginComponent.put("received", new JsonObject().put("$date", this.timestampFormatter.format(new Date())));

        MongoDBPersistenceVerticleIT.vertx.eventBus().send("ers.MarginComponent", marginComponent, ar -> {
            context.assertTrue(ar.succeeded());
            asyncStore.complete();
        });
        asyncStore.awaitSuccess();
        final Async asyncFind = context.async();
        MongoDBPersistenceVerticleIT.mongoClient.findOne("ers.MarginComponent", marginComponent, null, ar -> {
            if (ar.succeeded()) {
                context.assertEquals(ar.result().getString("clearer"), "CLEARER");
                context.assertEquals(ar.result().getString("member"), "MEMBER");
                context.assertEquals(ar.result().getString("account"), "ACCOUNT");
                context.assertEquals(ar.result().getString("clss"), "CLASS");
                context.assertEquals(ar.result().getString("ccy"), "CURRENCY");
                context.assertEquals(ar.result().getJsonObject("txnTm"), new JsonObject().put("$date", "2013-12-18T14:56:58.1Z"));
                context.assertEquals(ar.result().getJsonObject("bizDt"), new JsonObject().put("$date", "2013-12-17T00:00:00Z"));
                context.assertEquals(ar.result().getString("reqId"), "REQID");
                context.assertEquals(ar.result().getString("rptId"), "RPTID");
                context.assertEquals(ar.result().getString("sesId"), "SESSID");
                context.assertEquals(ar.result().getDouble("variationMargin"), 10.0);
                context.assertEquals(ar.result().getDouble("premiumMargin"), 10.0);
                context.assertEquals(ar.result().getDouble("liquiMargin"), 10.0);
                context.assertEquals(ar.result().getDouble("spreadMargin"), 10.0);
                context.assertEquals(ar.result().getDouble("additionalMargin"), 10.0);
                asyncFind.complete();
            } else {
                context.fail("Unable to find find MarginComponent document");
            }
        });
    }

    @Test
    public void testStoreTotalMarginRequirement(TestContext context) {
        final Async asyncStore = context.async();
        JsonObject totalMarginRequirement = new JsonObject();
        totalMarginRequirement.put("clearer", "CLEARER");
        totalMarginRequirement.put("pool", "POOL");
        totalMarginRequirement.put("member", "MEMBER");
        totalMarginRequirement.put("account", "ACCOUNT");
        totalMarginRequirement.put("ccy", "CURRENCY");
        totalMarginRequirement.put("txnTm", new JsonObject().put("$date", "2013-12-18T14:56:58.100Z"));
        totalMarginRequirement.put("bizDt", new JsonObject().put("$date", "2013-12-17T00:00:00.000Z"));
        totalMarginRequirement.put("reqId", "REQID");
        totalMarginRequirement.put("rptId", "RPTID");
        totalMarginRequirement.put("sesId", "SESSID");
        totalMarginRequirement.put("unadjustedMargin", 10.0);
        totalMarginRequirement.put("adjustedMargin", 10.0);
        totalMarginRequirement.put("received", new JsonObject().put("$date", this.timestampFormatter.format(new Date())));

        MongoDBPersistenceVerticleIT.vertx.eventBus().send("ers.TotalMarginRequirement", totalMarginRequirement, ar -> {
            context.assertTrue(ar.succeeded());
            asyncStore.complete();
        });
        asyncStore.awaitSuccess();
        final Async asyncFind = context.async();
        MongoDBPersistenceVerticleIT.mongoClient.findOne("ers.TotalMarginRequirement", totalMarginRequirement, null, ar -> {
            if (ar.succeeded()) {
                context.assertEquals(ar.result().getString("clearer"), "CLEARER");
                context.assertEquals(ar.result().getString("pool"), "POOL");
                context.assertEquals(ar.result().getString("member"), "MEMBER");
                context.assertEquals(ar.result().getString("account"), "ACCOUNT");
                context.assertEquals(ar.result().getString("ccy"), "CURRENCY");
                context.assertEquals(ar.result().getJsonObject("txnTm"), new JsonObject().put("$date", "2013-12-18T14:56:58.1Z"));
                context.assertEquals(ar.result().getJsonObject("bizDt"), new JsonObject().put("$date", "2013-12-17T00:00:00Z"));
                context.assertEquals(ar.result().getString("reqId"), "REQID");
                context.assertEquals(ar.result().getString("rptId"), "RPTID");
                context.assertEquals(ar.result().getString("sesId"), "SESSID");
                context.assertEquals(ar.result().getDouble("unadjustedMargin"), 10.0);
                context.assertEquals(ar.result().getDouble("adjustedMargin"), 10.0);
                asyncFind.complete();
            } else {
                context.fail("Unable to find find TotalMarginRequirement document");
            }
        });
    }

    @Test
    public void testStoreMarginShortfallSurplus(TestContext context) {
        final Async asyncStore = context.async();
        JsonObject totalMarginRequirement = new JsonObject();
        totalMarginRequirement.put("clearer", "CLEARER");
        totalMarginRequirement.put("pool", "POOL");
        totalMarginRequirement.put("poolType", "POOLTYPE");
        totalMarginRequirement.put("member", "MEMBER");
        totalMarginRequirement.put("clearingCcy", "CLEARINGCURRENCY");
        totalMarginRequirement.put("ccy", "CURRENCY");
        totalMarginRequirement.put("txnTm", new JsonObject().put("$date", "2013-12-18T14:56:58.100Z"));
        totalMarginRequirement.put("bizDt", new JsonObject().put("$date", "2013-12-17T00:00:00.000Z"));
        totalMarginRequirement.put("reqId", "REQID");
        totalMarginRequirement.put("rptId", "RPTID");
        totalMarginRequirement.put("sesId", "SESSID");
        totalMarginRequirement.put("marginRequirement", 10.0);
        totalMarginRequirement.put("securityCollateral", 10.0);
        totalMarginRequirement.put("cashBalance", 10.0);
        totalMarginRequirement.put("shortfallSurplus", 10.0);
        totalMarginRequirement.put("marginCall", 10.0);
        totalMarginRequirement.put("received", new JsonObject().put("$date", this.timestampFormatter.format(new Date())));

        MongoDBPersistenceVerticleIT.vertx.eventBus().send("ers.MarginShortfallSurplus", totalMarginRequirement, ar -> {
            context.assertTrue(ar.succeeded());
            asyncStore.complete();
        });
        asyncStore.awaitSuccess();
        final Async asyncFind = context.async();
        MongoDBPersistenceVerticleIT.mongoClient.findOne("ers.MarginShortfallSurplus", totalMarginRequirement, null, ar -> {
            if (ar.succeeded()) {
                context.assertEquals(ar.result().getString("clearer"), "CLEARER");
                context.assertEquals(ar.result().getString("pool"), "POOL");
                context.assertEquals(ar.result().getString("poolType"), "POOLTYPE");
                context.assertEquals(ar.result().getString("member"), "MEMBER");
                context.assertEquals(ar.result().getString("clearingCcy"), "CLEARINGCURRENCY");
                context.assertEquals(ar.result().getString("ccy"), "CURRENCY");
                context.assertEquals(ar.result().getJsonObject("txnTm"), new JsonObject().put("$date", "2013-12-18T14:56:58.1Z"));
                context.assertEquals(ar.result().getJsonObject("bizDt"), new JsonObject().put("$date", "2013-12-17T00:00:00Z"));
                context.assertEquals(ar.result().getString("reqId"), "REQID");
                context.assertEquals(ar.result().getString("rptId"), "RPTID");
                context.assertEquals(ar.result().getString("sesId"), "SESSID");
                context.assertEquals(ar.result().getDouble("marginRequirement"), 10.0);
                context.assertEquals(ar.result().getDouble("securityCollateral"), 10.0);
                context.assertEquals(ar.result().getDouble("cashBalance"), 10.0);
                context.assertEquals(ar.result().getDouble("shortfallSurplus"), 10.0);
                context.assertEquals(ar.result().getDouble("marginCall"), 10.0);
                asyncFind.complete();
            } else {
                context.fail("Unable to find find MarginShortfallSurplus document");
            }
        });
    }

    @Test
    public void testStoreRiskLimit(TestContext context) {
        final Async asyncStore = context.async();
        JsonObject riskLimit = new JsonObject();
        riskLimit.put("clearer", "CLEARER");
        riskLimit.put("member", "MEMBER");
        riskLimit.put("maintainer", "MAINTAINER");
        riskLimit.put("txnTm", new JsonObject().put("$date", "2013-12-18T14:56:58.100Z"));
        riskLimit.put("reqId", "REQID");
        riskLimit.put("rptId", "RPTID");
        riskLimit.put("reqRslt", "REQRSLT");
        riskLimit.put("txt", "TXT");
        riskLimit.put("limitType", "LIMITTYPE");
        riskLimit.put("utilization", 10.0);
        riskLimit.put("warningLevel", 10.0);
        riskLimit.put("throttleLevel", 10.0);
        riskLimit.put("rejectLevel", 10.0);
        riskLimit.put("received", new JsonObject().put("$date", this.timestampFormatter.format(new Date())));

        MongoDBPersistenceVerticleIT.vertx.eventBus().send("ers.RiskLimit", new JsonArray().add(riskLimit), ar -> {
            context.assertTrue(ar.succeeded());
            asyncStore.complete();
        });
        asyncStore.awaitSuccess();
        final Async asyncFind = context.async();
        MongoDBPersistenceVerticleIT.mongoClient.findOne("ers.RiskLimit", riskLimit, null, ar -> {
            if (ar.succeeded()) {
                context.assertEquals(ar.result().getString("clearer"), "CLEARER");
                context.assertEquals(ar.result().getString("member"), "MEMBER");
                context.assertEquals(ar.result().getString("maintainer"), "MAINTAINER");
                context.assertEquals(ar.result().getJsonObject("txnTm"), new JsonObject().put("$date", "2013-12-18T14:56:58.1Z"));
                context.assertEquals(ar.result().getString("reqId"), "REQID");
                context.assertEquals(ar.result().getString("rptId"), "RPTID");
                context.assertEquals(ar.result().getString("reqRslt"), "REQRSLT");
                context.assertEquals(ar.result().getString("txt"), "TXT");
                context.assertEquals(ar.result().getString("limitType"), "LIMITTYPE");
                context.assertEquals(ar.result().getDouble("utilization"), 10.0);
                context.assertEquals(ar.result().getDouble("warningLevel"), 10.0);
                context.assertEquals(ar.result().getDouble("throttleLevel"), 10.0);
                context.assertEquals(ar.result().getDouble("rejectLevel"), 10.0);
                asyncFind.complete();
            } else {
                context.fail("Unable to find find RiskLimit document");
            }
        });
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        MongoDBPersistenceVerticleIT.vertx.close(context.asyncAssertSuccess());
    }
}
