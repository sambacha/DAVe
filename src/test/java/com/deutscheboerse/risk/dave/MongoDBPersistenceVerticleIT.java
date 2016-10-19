package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.utils.DummyData;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.text.ParseException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MongoDBPersistenceVerticleIT {

    private final static List<String> fields;
    static {
        fields = new ArrayList<>();
        fields.add("reqId");
        fields.add("sesId");
        fields.add("stat");
        fields.add("statRejRsn");
        fields.add("txt");
        fields.add("received");
        fields.add("clearer");
        fields.add("member");
        fields.add("account");
        fields.add("clss");
        fields.add("ccy");
        fields.add("txnTm");
        fields.add("bizDt");
        fields.add("rptId");
        fields.add("variationMargin");
        fields.add("premiumMargin");
        fields.add("liquiMargin");
        fields.add("spreadMargin");
        fields.add("additionalMargin");
        fields.add("lastReportRequested");
        fields.add("symbol");
        fields.add("putCall");
        fields.add("maturityMonthYear");
        fields.add("strikePrice");
        fields.add("optAttribute");
        fields.add("crossMarginLongQty");
        fields.add("crossMarginShortQty");
        fields.add("optionExcerciseQty");
        fields.add("optionAssignmentQty");
        fields.add("allocationTradeQty");
        fields.add("deliveryNoticeQty");
        fields.add("pool");
        fields.add("unadjustedMargin");
        fields.add("adjustedMargin");
        fields.add("poolType");
        fields.add("clearingCcy");
        fields.add("marginRequirement");
        fields.add("securityCollateral");
        fields.add("cashBalance");
        fields.add("shortfallSurplus");
        fields.add("marginCall");
        fields.add("reqRslt");
        fields.add("txt");
        fields.add("limitType");
        fields.add("utilization");
        fields.add("warningLevel");
        fields.add("throttleLevel");
        fields.add("rejectLevel");
    }

    private static Vertx vertx;
    private static MongoClient mongoClient;

    @BeforeClass
    public static void setUp(TestContext context) {
        MongoDBPersistenceVerticleIT.vertx = Vertx.vertx();
        JsonObject config = new JsonObject();
        config.put("db_name", "DAVe-Test" + UUID.randomUUID().getLeastSignificantBits());
        config.put("connection_string", "mongodb://localhost:" + System.getProperty("mongodb.port", "27017"));
        DeploymentOptions options = new DeploymentOptions().setConfig(config);
        MongoDBPersistenceVerticleIT.vertx.deployVerticle(MongoDBPersistenceVerticle.class.getName(), options, context.asyncAssertSuccess());
        MongoDBPersistenceVerticleIT.mongoClient = MongoClient.createShared(MongoDBPersistenceVerticleIT.vertx, config);
    }

    private JsonObject transformDatesInDummyData(JsonObject data) throws ParseException {
        String[] dateKeys = {"received", "txnTm"};
        for (String key : dateKeys) {
            if (data.containsKey(key)) {
                String timestamp = data.getJsonObject(key).getString("$date");
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                data.remove(key);
                data.put(key, zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            }
        }
        return data;
    }

    private JsonObject transformDatesInActualData(JsonObject data) throws ParseException {
        String[] dateKeys = {"received", "txnTm"};
        for (String key : dateKeys) {
            if (data.containsKey(key)) {
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(data.getString(key), DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC));
                data.remove(key);
                data.put(key, zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            }
        }
        return data;
    }

    private void compareMessages(TestContext context, JsonObject expected, JsonObject actual) throws ParseException {
        JsonObject transformedExpected = transformDatesInDummyData(expected.copy());
        JsonObject transformedActual = transformDatesInActualData(actual.copy());

        fields.forEach(key -> {
            if (transformedActual.containsKey(key))
            {
                if (transformedExpected.getValue(key) == null)
                {
                    context.assertNull(transformedExpected.getValue(key));
                }
                else if (transformedExpected.getValue(key) instanceof String)
                {
                    context.assertEquals(transformedExpected.getString(key), transformedActual.getString(key), key + " are not equal in " + Json.encodePrettily(transformedExpected) + " versus " + Json.encodePrettily(transformedActual));
                }
                else if (transformedExpected.getValue(key) instanceof Double)
                {
                    context.assertEquals(transformedExpected.getDouble(key), transformedActual.getDouble(key), key + " are not equal in " + Json.encodePrettily(transformedExpected) + " versus " + Json.encodePrettily(transformedActual));
                }
                else if (transformedExpected.getValue(key) instanceof Integer)
                {
                    context.assertEquals(transformedExpected.getInteger(key), transformedActual.getInteger(key), key + " are not equal in " + Json.encodePrettily(transformedExpected) + " versus " + Json.encodePrettily(transformedActual));
                }
                else
                {
                    context.fail("Found unknown type for key " + key + ": " + transformedExpected.getValue(key).getClass().getName());
                }

            }
        });
    }

    @Test
    public void checkCollectionsExist(TestContext context) {
        List<String> requiredCollections = new ArrayList<>();
        requiredCollections.add("ers.TradingSessionStatus");
        requiredCollections.add("ers.TradingSessionStatus.latest");
        requiredCollections.add("ers.MarginComponent");
        requiredCollections.add("ers.MarginComponent.latest");
        requiredCollections.add("ers.TotalMarginRequirement");
        requiredCollections.add("ers.TotalMarginRequirement.latest");
        requiredCollections.add("ers.MarginShortfallSurplus");
        requiredCollections.add("ers.MarginShortfallSurplus.latest");
        requiredCollections.add("ers.PositionReport");
        requiredCollections.add("ers.PositionReport.latest");
        requiredCollections.add("ers.RiskLimit");
        requiredCollections.add("ers.RiskLimit.latest");
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
            final Async asyncSend = context.async();
            vertx.eventBus().send("ers.TradingSessionStatus", tss, ar -> {
                context.assertTrue(ar.succeeded());
                asyncSend.complete();
                });

            asyncSend.awaitSuccess();
        });

        // Test the latest query
        final Async asyncLatest = context.async();
        vertx.eventBus().send("query.latestTradingSessionStatus", new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(1, response.size());

                    compareMessages(context, DummyData.tradingSessionStatusJson.get(1), response.getJsonObject(0));
                    asyncLatest.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.latestTradingSessionStatus!");
            }
        });

        asyncLatest.awaitSuccess();

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
                context.fail("Didn't receive a response to query.historyTradingSessionStatus!");
            }
        });

        asyncHistory.awaitSuccess();
    }

    @Test
    public void testPositionReport(TestContext context) throws InterruptedException {
        // Feed the data into the store
        DummyData.positionReportJson.forEach(pr -> {
            final Async asyncSend = context.async();
            vertx.eventBus().send("ers.PositionReport", pr, ar -> {
                context.assertTrue(ar.succeeded());
                asyncSend.complete();
            });

            asyncSend.awaitSuccess();
        });

        // Test the latest query
        final Async asyncLatest = context.async();
        vertx.eventBus().send("query.latestPositionReport", new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(2, response.size());

                    compareMessages(context, DummyData.positionReportJson.get(2), response.getJsonObject(0));
                    compareMessages(context, DummyData.positionReportJson.get(3), response.getJsonObject(1));
                    asyncLatest.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.latestPositionReport!");
            }
        });

        asyncLatest.awaitSuccess();

        // Test the latest query with filter
        final Async asyncLatestFilter = context.async();
        vertx.eventBus().send("query.latestPositionReport", new JsonObject().put("clearer", "ABCFR").put("member", "ABCFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(1, response.size());

                    compareMessages(context, DummyData.positionReportJson.get(3), response.getJsonObject(0));
                    asyncLatestFilter.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.latestPositionReport!");
            }
        });

        asyncLatestFilter.awaitSuccess();

        // Test the history query
        final Async asyncHistory = context.async();
        vertx.eventBus().send("query.historyPositionReport", new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(4, response.size());

                    compareMessages(context, DummyData.positionReportJson.get(0), response.getJsonObject(0));
                    compareMessages(context, DummyData.positionReportJson.get(1), response.getJsonObject(1));
                    compareMessages(context, DummyData.positionReportJson.get(2), response.getJsonObject(2));
                    compareMessages(context, DummyData.positionReportJson.get(3), response.getJsonObject(3));
                    asyncHistory.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.historyPositionReport!");
            }
        });

        asyncHistory.awaitSuccess();

        // Test the history query with filter
        final Async asyncHistoryFilter = context.async();
        vertx.eventBus().send("query.historyPositionReport", new JsonObject().put("clearer", "ABCFR").put("member", "ABCFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(2, response.size());

                    compareMessages(context, DummyData.positionReportJson.get(1), response.getJsonObject(0));
                    compareMessages(context, DummyData.positionReportJson.get(3), response.getJsonObject(1));
                    asyncHistoryFilter.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.historyPositionReport!");
            }
        });

        asyncHistoryFilter.awaitSuccess();
    }

    @Test
    public void testMarginComponent(TestContext context) throws InterruptedException {
        // Feed the data into the store
        DummyData.marginComponentJson.forEach(mc -> {
            final Async asyncSend = context.async();
            vertx.eventBus().send("ers.MarginComponent", mc, ar -> {
                context.assertTrue(ar.succeeded());
                asyncSend.complete();
            });

            asyncSend.awaitSuccess();
        });

        // Test the latest query
        final Async asyncLatest = context.async();
        vertx.eventBus().send("query.latestMarginComponent", new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(2, response.size());

                    compareMessages(context, DummyData.marginComponentJson.get(2), response.getJsonObject(0));
                    compareMessages(context, DummyData.marginComponentJson.get(3), response.getJsonObject(1));
                    asyncLatest.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.latestMarginComponent!");
            }
        });

        asyncLatest.awaitSuccess();

        // Test the latest query with filter
        final Async asyncLatestFilter = context.async();
        vertx.eventBus().send("query.latestMarginComponent", new JsonObject().put("clearer", "ABCFR").put("member", "DEFFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(1, response.size());

                    compareMessages(context, DummyData.marginComponentJson.get(2), response.getJsonObject(0));
                    asyncLatestFilter.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.latestMarginComponent!");
            }
        });

        asyncLatestFilter.awaitSuccess();

        // Test the history query
        final Async asyncHistory = context.async();
        vertx.eventBus().send("query.historyMarginComponent", new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(4, response.size());

                    compareMessages(context, DummyData.marginComponentJson.get(0), response.getJsonObject(0));
                    compareMessages(context, DummyData.marginComponentJson.get(1), response.getJsonObject(1));
                    compareMessages(context, DummyData.marginComponentJson.get(2), response.getJsonObject(2));
                    compareMessages(context, DummyData.marginComponentJson.get(3), response.getJsonObject(3));
                    asyncHistory.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.historyMarginComponent!");
            }
        });

        asyncHistory.awaitSuccess();

        // Test the history query with filter
        final Async asyncHistoryFilter = context.async();
        vertx.eventBus().send("query.historyMarginComponent", new JsonObject().put("clearer", "ABCFR").put("member", "DEFFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(2, response.size());

                    compareMessages(context, DummyData.marginComponentJson.get(0), response.getJsonObject(0));
                    compareMessages(context, DummyData.marginComponentJson.get(2), response.getJsonObject(1));
                    asyncHistoryFilter.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.historyMarginComponent!");
            }
        });

        asyncHistoryFilter.awaitSuccess();
    }


    @Test
    public void testTotalMarginRequirement(TestContext context) throws InterruptedException {
        // Feed the data into the store
        DummyData.totalMarginRequirementJson.forEach(tmr -> {
            final Async asyncSend = context.async();
            vertx.eventBus().send("ers.TotalMarginRequirement", tmr, ar -> {
                context.assertTrue(ar.succeeded());
                asyncSend.complete();
            });

            asyncSend.awaitSuccess();
        });

        // Test the latest query
        final Async asyncLatest = context.async();
        vertx.eventBus().send("query.latestTotalMarginRequirement", new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(2, response.size());

                    compareMessages(context, DummyData.totalMarginRequirementJson.get(2), response.getJsonObject(0));
                    compareMessages(context, DummyData.totalMarginRequirementJson.get(3), response.getJsonObject(1));
                    asyncLatest.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.latestTotalMarginRequirement!");
            }
        });

        asyncLatest.awaitSuccess();

        // Test the latest query with filter
        final Async asyncLatestFilter = context.async();
        vertx.eventBus().send("query.latestTotalMarginRequirement", new JsonObject().put("clearer", "ABCFR").put("member", "DEFFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(1, response.size());

                    compareMessages(context, DummyData.totalMarginRequirementJson.get(2), response.getJsonObject(0));
                    asyncLatestFilter.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.latestTotalMarginRequirement!");
            }
        });

        asyncLatestFilter.awaitSuccess();

        // Test the history query
        final Async asyncHistory = context.async();
        vertx.eventBus().send("query.historyTotalMarginRequirement", new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(4, response.size());

                    compareMessages(context, DummyData.totalMarginRequirementJson.get(0), response.getJsonObject(0));
                    compareMessages(context, DummyData.totalMarginRequirementJson.get(1), response.getJsonObject(1));
                    compareMessages(context, DummyData.totalMarginRequirementJson.get(2), response.getJsonObject(2));
                    compareMessages(context, DummyData.totalMarginRequirementJson.get(3), response.getJsonObject(3));
                    asyncHistory.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.historyTotalMarginRequirement!");
            }
        });

        asyncHistory.awaitSuccess();

        // Test the history query with filter
        final Async asyncHistoryFilter = context.async();
        vertx.eventBus().send("query.historyTotalMarginRequirement", new JsonObject().put("clearer", "ABCFR").put("member", "DEFFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(2, response.size());

                    compareMessages(context, DummyData.totalMarginRequirementJson.get(0), response.getJsonObject(0));
                    compareMessages(context, DummyData.totalMarginRequirementJson.get(2), response.getJsonObject(1));
                    asyncHistoryFilter.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.historyTotalMarginRequirement!");
            }
        });

        asyncHistoryFilter.awaitSuccess();
    }

    @Test
    public void testMarginShortfallSurplus(TestContext context) throws InterruptedException {
        // Feed the data into the store
        DummyData.marginShortfallSurplusJson.forEach(mss -> {
            final Async asyncSend = context.async();
            vertx.eventBus().send("ers.MarginShortfallSurplus", mss, ar -> {
                context.assertTrue(ar.succeeded());
                asyncSend.complete();
            });

            asyncSend.awaitSuccess();
        });

        // Test the latest query
        final Async asyncLatest = context.async();
        vertx.eventBus().send("query.latestMarginShortfallSurplus", new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(2, response.size());

                    compareMessages(context, DummyData.marginShortfallSurplusJson.get(2), response.getJsonObject(0));
                    compareMessages(context, DummyData.marginShortfallSurplusJson.get(3), response.getJsonObject(1));
                    asyncLatest.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.latestMarginShortfallSurplus!");
            }
        });

        asyncLatest.awaitSuccess();

        // Test the latest query with filter
        final Async asyncLatestFilter = context.async();
        vertx.eventBus().send("query.latestMarginShortfallSurplus", new JsonObject().put("clearer", "ABCFR").put("member", "DEFFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(1, response.size());

                    compareMessages(context, DummyData.marginShortfallSurplusJson.get(2), response.getJsonObject(0));
                    asyncLatestFilter.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.latestMarginShortfallSurplus!");
            }
        });

        asyncLatestFilter.awaitSuccess();

        // Test the history query
        final Async asyncHistory = context.async();
        vertx.eventBus().send("query.historyMarginShortfallSurplus", new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(4, response.size());

                    compareMessages(context, DummyData.marginShortfallSurplusJson.get(0), response.getJsonObject(0));
                    compareMessages(context, DummyData.marginShortfallSurplusJson.get(1), response.getJsonObject(1));
                    compareMessages(context, DummyData.marginShortfallSurplusJson.get(2), response.getJsonObject(2));
                    compareMessages(context, DummyData.marginShortfallSurplusJson.get(3), response.getJsonObject(3));
                    asyncHistory.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.historyMarginShortfallSurplus!");
            }
        });

        asyncHistory.awaitSuccess();

        // Test the history query with filter
        final Async asyncHistoryFilter = context.async();
        vertx.eventBus().send("query.historyMarginShortfallSurplus", new JsonObject().put("clearer", "ABCFR").put("member", "DEFFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(2, response.size());

                    compareMessages(context, DummyData.marginShortfallSurplusJson.get(0), response.getJsonObject(0));
                    compareMessages(context, DummyData.marginShortfallSurplusJson.get(2), response.getJsonObject(1));
                    asyncHistoryFilter.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.historyMarginShortfallSurplus!");
            }
        });

        asyncHistoryFilter.awaitSuccess();
    }

    @Test
    public void testRiskLimit(TestContext context) throws InterruptedException {
        // Feed the data into the store
        DummyData.riskLimitJson.forEach(rl -> {
            final Async asyncSend = context.async();
            vertx.eventBus().send("ers.RiskLimit", rl, ar -> {
                context.assertTrue(ar.succeeded());
                asyncSend.complete();
            });

            asyncSend.awaitSuccess();
        });

        // Test the latest query
        final Async asyncLatest = context.async();
        vertx.eventBus().send("query.latestRiskLimit", new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(2, response.size());

                    compareMessages(context, DummyData.riskLimitJson.get(2), response.getJsonObject(0));
                    compareMessages(context, DummyData.riskLimitJson.get(3), response.getJsonObject(1));
                    asyncLatest.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.latestRiskLimit!");
            }
        });

        asyncLatest.awaitSuccess();

        // Test the latest query with filter
        final Async asyncLatestFilter = context.async();
        vertx.eventBus().send("query.latestRiskLimit", new JsonObject().put("clearer", "ABCFR").put("member", "DEFFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(1, response.size());

                    compareMessages(context, DummyData.riskLimitJson.get(2), response.getJsonObject(0));
                    asyncLatestFilter.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.latestRiskLimit!");
            }
        });

        asyncLatestFilter.awaitSuccess();

        // Test the history query
        final Async asyncHistory = context.async();
        vertx.eventBus().send("query.historyRiskLimit", new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(4, response.size());

                    compareMessages(context, DummyData.riskLimitJson.get(0), response.getJsonObject(0));
                    compareMessages(context, DummyData.riskLimitJson.get(1), response.getJsonObject(1));
                    compareMessages(context, DummyData.riskLimitJson.get(2), response.getJsonObject(2));
                    compareMessages(context, DummyData.riskLimitJson.get(3), response.getJsonObject(3));
                    asyncHistory.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.historyRiskLimit!");
            }
        });

        asyncHistory.awaitSuccess();

        // Test the history query with filter
        final Async asyncHistoryFilter = context.async();
        vertx.eventBus().send("query.historyRiskLimit", new JsonObject().put("clearer", "ABCFR").put("member", "DEFFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray((String) ar.result().body());

                    context.assertEquals(2, response.size());

                    compareMessages(context, DummyData.riskLimitJson.get(0), response.getJsonObject(0));
                    compareMessages(context, DummyData.riskLimitJson.get(2), response.getJsonObject(1));
                    asyncHistoryFilter.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            }
            else
            {
                context.fail("Didn't receive a response to query.historyRiskLimit!");
            }
        });

        asyncHistoryFilter.awaitSuccess();
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        MongoDBPersistenceVerticleIT.vertx.close(context.asyncAssertSuccess());
    }
}
