package com.opnfi.risk;

import com.opnfi.risk.utils.DummyData;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MongoDBPersistenceVerticleIT {
    private final DateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    private final DateFormat mongoTimestampFormatter;
    {
        mongoTimestampFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        mongoTimestampFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final DateFormat mongoDateFormatter;
    {
        mongoDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        mongoDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final static List<String> fields;
    static {
        fields = new ArrayList<>();
        fields.add("reqId");
        fields.add("sesId");
        fields.add("stat");
        fields.add("statRejRsn");
        fields.add("txt");
        //fields.add("received");
        fields.add("clearer");
        fields.add("member");
        fields.add("account");
        fields.add("clss");
        fields.add("ccy");
        //fields.add("txnTm");
        //fields.add("bizDt");
        fields.add("rptId");
        fields.add("variationMargin");
        fields.add("premiumMargin");
        fields.add("liquiMargin");
        fields.add("spreadMargin");
        fields.add("additionalMargin");
        //fields.add("");
    }

    //private final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
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

    private JsonObject transformDummyData(JsonObject data) throws ParseException {
        /*System.out.println(Json.encodePrettily(data));

        if (data.containsKey("received")) {
            System.out.println(Json.encodePrettily(data.getJsonObject("received")));

            data.put("received", patchTimestamp(data.getJsonObject("received")));
        }

        if (data.containsKey("txnTm")) {
            System.out.println(Json.encodePrettily(data.getJsonObject("txnTm")));
            data.put("txnTm", patchTimestamp(data.getJsonObject("txnTm")));
        }

        if (data.containsKey("bizDt")) {
            data.put("bizDt", patchDate(data.getJsonObject("bizDt")));
        }*/

        return data;
    }

    private String patchTimestamp(JsonObject date) throws ParseException {
        System.out.println(date.getString("$date"));
        return mongoTimestampFormatter.format(timestampFormatter.parse(date.getString("$date")));
    }

    private String patchDate(JsonObject date) throws ParseException {
        return mongoDateFormatter.format(dateFormatter.parse(date.getString("$date")));
    }

    private JsonObject transformQueryResult(JsonObject data)
    {
        /*if (data.containsKey("_id")) {
            data.remove("_id");
        }

        if (data.containsKey("id")) {
            data.remove("id");
        }*/

        return data;
    }

    private void compareMessages(TestContext context, JsonObject expected, JsonObject actual) throws ParseException {
        JsonObject transformedExpected = transformDummyData(expected.copy());
        JsonObject transformedActual = transformQueryResult(actual.copy());

        fields.forEach(key -> {
            if (transformedActual.containsKey(key))
            {
                context.assertEquals(transformedExpected.getValue(key), transformedActual.getValue(key));
            }
        });
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
    public void testTradingSessionStatus(TestContext context) throws InterruptedException {
        // Feed the data into the store
        DummyData.tradingSessionStatusJson.forEach(tss -> {
            vertx.eventBus().publish("ers.TradingSessionStatus", tss);
        });

        // Test the latest query
        final Async asyncLatest = context.async();
        vertx.eventBus().send("query.latestTradingSessionStatus", new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonObject response = new JsonObject((String) ar.result().body());

                    compareMessages(context, DummyData.tradingSessionStatusJson.get(1), response);
                    asyncLatest.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't received a response to query.latestTradingSessionStatus!");
            }
        });

        // Test the latest query
        final Async asyncHistory = context.async();
        vertx.eventBus().send("query.historyTradingSessionStatus", new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(2, response.size());
                    compareMessages(context, DummyData.tradingSessionStatusJson.get(0), response.getJsonObject(0));
                    compareMessages(context, DummyData.tradingSessionStatusJson.get(1), response.getJsonObject(1));
                    asyncHistory.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't received a response to query.historyTradingSessionStatus!");
            }
        });
    }

    @Test
    public void testMarginComponent(TestContext context) throws InterruptedException {
        // Feed the data into the store
        DummyData.marginComponentsJson.forEach(tss -> {
            vertx.eventBus().publish("ers.MarginComponent", tss);
        });

        // Test the latest query
        final Async asyncLatest = context.async();
        vertx.eventBus().send("query.latestMarginComponent", new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    compareMessages(context, DummyData.marginComponentsJson.get(2), response.getJsonObject(1));
                    compareMessages(context, DummyData.marginComponentsJson.get(3), response.getJsonObject(0));
                    asyncLatest.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't received a response to query.latestMarginComponent!");
            }
        });



        // Test the latest query
        final Async asyncHistory = context.async();
        vertx.eventBus().send("query.historyMarginComponent", new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(4, response.size());

                    compareMessages(context, DummyData.marginComponentsJson.get(0), response.getJsonObject(0));
                    compareMessages(context, DummyData.marginComponentsJson.get(1), response.getJsonObject(1));
                    compareMessages(context, DummyData.marginComponentsJson.get(2), response.getJsonObject(2));
                    compareMessages(context, DummyData.marginComponentsJson.get(3), response.getJsonObject(3));
                    asyncHistory.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't received a response to query.historyMarginComponent!");
            }
        });

        asyncHistory.awaitSuccess();
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

    /*@Test
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
    }*/

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
