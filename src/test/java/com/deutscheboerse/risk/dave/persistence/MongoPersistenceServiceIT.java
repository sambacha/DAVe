package com.deutscheboerse.risk.dave.persistence;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.deutscheboerse.risk.dave.log.TestAppender;
import com.deutscheboerse.risk.dave.model.*;
import com.deutscheboerse.risk.dave.utils.DummyData;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.deutscheboerse.risk.dave.model.AbstractModel.CollectionType.HISTORY;
import static com.deutscheboerse.risk.dave.model.AbstractModel.CollectionType.LATEST;

@RunWith(VertxUnitRunner.class)
public class MongoPersistenceServiceIT {
    private static final TestAppender testAppender = TestAppender.getAppender(MongoPersistenceService.class);
    private static final Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

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
    private static PersistenceService persistenceProxy;

    @BeforeClass
    public static void setUp(TestContext context) {
        MongoPersistenceServiceIT.vertx = Vertx.vertx();

        JsonObject mongoConfig = new JsonObject();
        mongoConfig.put("db_name", "DAVe-Test" + UUID.randomUUID().getLeastSignificantBits());
        mongoConfig.put("connection_string", "mongodb://localhost:" + System.getProperty("mongodb.port", "27017"));
        MongoPersistenceServiceIT.mongoClient = MongoClient.createShared(MongoPersistenceServiceIT.vertx, mongoConfig);

        ProxyHelper.registerService(PersistenceService.class, vertx, new MongoPersistenceService(vertx, mongoClient), PersistenceService.SERVICE_ADDRESS);
        MongoPersistenceServiceIT.persistenceProxy = ProxyHelper.createProxy(PersistenceService.class, vertx, PersistenceService.SERVICE_ADDRESS);
        MongoPersistenceServiceIT.persistenceProxy.initialize(context.asyncAssertSuccess());

        rootLogger.addAppender(testAppender);
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
        MongoPersistenceServiceIT.mongoClient.getCollections(ar -> {
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
        TradingSessionStatusModel model = new TradingSessionStatusModel();
        DummyData.tradingSessionStatusJson.forEach(doc -> {
            final Async asyncHistory = context.async();
            final Async asyncLatest = context.async();

            mongoClient.insert(model.getHistoryCollection(), doc.copy(), res -> {
                context.assertTrue(res.succeeded());
                asyncHistory.complete();
            });

            JsonObject query = new JsonObject();
            query.put("sesId", doc.getValue("sesId"));

            mongoClient.replaceDocumentsWithOptions(model.getLatestCollection(), query, doc.copy().put("received", doc.getJsonObject("received").getString("$date")), new UpdateOptions().setUpsert(true), res -> {
                context.assertTrue(res.succeeded());
                asyncLatest.complete();
            });

            asyncHistory.awaitSuccess();
            asyncLatest.awaitSuccess();
        });

        // Test the latest query
        final Async asyncLatest = context.async();
        persistenceProxy.queryTradingSessionStatus(LATEST, new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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

        // Test the history query
        final Async asyncHistory = context.async();
        persistenceProxy.queryTradingSessionStatus(HISTORY, new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        PositionReportModel model = new PositionReportModel();
        DummyData.positionReportJson.forEach(doc -> {
            final Async asyncHistory = context.async();
            final Async asyncLatest = context.async();

            mongoClient.insert(model.getHistoryCollection(), doc.copy(), res -> {
                context.assertTrue(res.succeeded());
                asyncHistory.complete();
            });

            JsonObject query = new JsonObject();
            query.put("clearer", doc.getValue("clearer"));
            query.put("member", doc.getValue("member"));
            query.put("account", doc.getValue("account"));
            query.put("clss", doc.getValue("clss"));
            query.put("symbol", doc.getValue("symbol"));
            query.put("putCall", doc.getValue("putCall"));
            query.put("strikePrice", doc.getValue("strikePrice"));
            query.put("optAttribute", doc.getValue("optAttribute"));
            query.put("maturityMonthYear", doc.getValue("maturityMonthYear"));

            mongoClient.replaceDocumentsWithOptions(model.getLatestCollection(), query, doc.copy().put("received", doc.getJsonObject("received").getString("$date")), new UpdateOptions().setUpsert(true), res -> {
                context.assertTrue(res.succeeded());
                asyncLatest.complete();
            });

            asyncHistory.awaitSuccess();
            asyncLatest.awaitSuccess();
        });

        // Test the latest query
        final Async asyncLatest = context.async();
        persistenceProxy.queryPositionReport(LATEST, new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        persistenceProxy.queryPositionReport(LATEST, new JsonObject().put("clearer", "ABCFR").put("member", "ABCFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        persistenceProxy.queryPositionReport(HISTORY, new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        persistenceProxy.queryPositionReport(HISTORY, new JsonObject().put("clearer", "ABCFR").put("member", "ABCFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        MarginComponentModel model = new MarginComponentModel();
        DummyData.marginComponentJson.forEach(doc -> {
            final Async asyncHistory = context.async();
            final Async asyncLatest = context.async();

            mongoClient.insert(model.getHistoryCollection(), doc.copy(), res -> {
                context.assertTrue(res.succeeded());
                asyncHistory.complete();
            });

            JsonObject query = new JsonObject();
            query.put("clearer", doc.getValue("clearer"));
            query.put("member", doc.getValue("member"));
            query.put("account", doc.getValue("account"));
            query.put("clss", doc.getValue("clss"));
            query.put("ccy", doc.getValue("ccy"));

            mongoClient.replaceDocumentsWithOptions(model.getLatestCollection(), query, doc.copy().put("received", doc.getJsonObject("received").getString("$date")).put("txnTm", doc.getJsonObject("txnTm").getString("$date")), new UpdateOptions().setUpsert(true), res -> {
                context.assertTrue(res.succeeded());
                asyncLatest.complete();
            });

            asyncHistory.awaitSuccess();
            asyncLatest.awaitSuccess();
        });

        // Test the latest query
        final Async asyncLatest = context.async();
        persistenceProxy.queryMarginComponent(LATEST, new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        persistenceProxy.queryMarginComponent(LATEST, new JsonObject().put("clearer", "ABCFR").put("member", "DEFFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        persistenceProxy.queryMarginComponent(HISTORY, new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        persistenceProxy.queryMarginComponent(HISTORY, new JsonObject().put("clearer", "ABCFR").put("member", "DEFFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        TotalMarginRequirementModel model = new TotalMarginRequirementModel();
        DummyData.totalMarginRequirementJson.forEach(doc -> {
            final Async asyncHistory = context.async();
            final Async asyncLatest = context.async();

            mongoClient.insert(model.getHistoryCollection(), doc.copy(), res -> {
                context.assertTrue(res.succeeded());
                asyncHistory.complete();
            });

            JsonObject query = new JsonObject();
            query.put("clearer", doc.getString("clearer"));
            query.put("pool", doc.getString("pool"));
            query.put("member", doc.getString("member"));
            query.put("account", doc.getString("account"));
            query.put("ccy", doc.getString("ccy"));

            mongoClient.replaceDocumentsWithOptions(model.getLatestCollection(), query, doc.copy().put("received", doc.getJsonObject("received").getString("$date")).put("txnTm", doc.getJsonObject("txnTm").getString("$date")), new UpdateOptions().setUpsert(true), res -> {
                context.assertTrue(res.succeeded());
                asyncLatest.complete();
            });

            asyncHistory.awaitSuccess();
            asyncLatest.awaitSuccess();
        });

        // Test the latest query
        final Async asyncLatest = context.async();
        persistenceProxy.queryTotalMarginRequirement(LATEST, new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        persistenceProxy.queryTotalMarginRequirement(LATEST,  new JsonObject().put("clearer", "ABCFR").put("member", "DEFFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        persistenceProxy.queryTotalMarginRequirement(HISTORY, new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        persistenceProxy.queryTotalMarginRequirement(HISTORY, new JsonObject().put("clearer", "ABCFR").put("member", "DEFFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        MarginShortfallSurplusModel model = new MarginShortfallSurplusModel();
        DummyData.marginShortfallSurplusJson.forEach(doc -> {
            final Async asyncHistory = context.async();
            final Async asyncLatest = context.async();

            mongoClient.insert(model.getHistoryCollection(), doc.copy(), res -> {
                context.assertTrue(res.succeeded());
                asyncHistory.complete();
            });

            JsonObject query = new JsonObject();
            query.put("clearer", doc.getValue("clearer"));
            query.put("pool", doc.getValue("pool"));
            query.put("member", doc.getValue("member"));
            query.put("clearingCcy", doc.getValue("clearingCcy"));
            query.put("ccy", doc.getValue("ccy"));

            mongoClient.replaceDocumentsWithOptions(model.getLatestCollection(), query, doc.copy().put("received", doc.getJsonObject("received").getString("$date")).put("txnTm", doc.getJsonObject("txnTm").getString("$date")), new UpdateOptions().setUpsert(true), res -> {
                context.assertTrue(res.succeeded());
                asyncLatest.complete();
            });

            asyncHistory.awaitSuccess();
            asyncLatest.awaitSuccess();
        });

        // Test the latest query
        final Async asyncLatest = context.async();
        persistenceProxy.queryMarginShortfallSurplus(LATEST, new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        persistenceProxy.queryMarginShortfallSurplus(LATEST,  new JsonObject().put("clearer", "ABCFR").put("member", "DEFFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        persistenceProxy.queryMarginShortfallSurplus(HISTORY,  new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        persistenceProxy.queryMarginShortfallSurplus(HISTORY, new JsonObject().put("clearer", "ABCFR").put("member", "DEFFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        RiskLimitModel model = new RiskLimitModel();
        DummyData.riskLimitJson.forEach(doc -> {
            final Async asyncHistory = context.async();
            final Async asyncLatest = context.async();

            mongoClient.insert(model.getHistoryCollection(), doc.copy(), res -> {
                context.assertTrue(res.succeeded());
                asyncHistory.complete();
            });

            JsonObject query = new JsonObject();
            query.put("clearer", doc.getValue("clearer"));
            query.put("member", doc.getValue("member"));
            query.put("maintainer", doc.getValue("maintainer"));
            query.put("limitType", doc.getValue("limitType"));

            mongoClient.replaceDocumentsWithOptions(model.getLatestCollection(), query, doc.copy().put("received", doc.getJsonObject("received").getString("$date")).put("txnTm", doc.getJsonObject("txnTm").getString("$date")), new UpdateOptions().setUpsert(true), res -> {
                context.assertTrue(res.succeeded());
                asyncLatest.complete();
            });

            asyncHistory.awaitSuccess();
            asyncLatest.awaitSuccess();
        });

        // Test the latest query
        final Async asyncLatest = context.async();
        persistenceProxy.queryRiskLimit(LATEST, new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        persistenceProxy.queryRiskLimit(LATEST, new JsonObject().put("clearer", "ABCFR").put("member", "DEFFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        persistenceProxy.queryRiskLimit(HISTORY, new JsonObject(), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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
        persistenceProxy.queryRiskLimit(HISTORY, new JsonObject().put("clearer", "ABCFR").put("member", "DEFFR"), ar -> {
            if (ar.succeeded())
            {
                try {
                    JsonArray response = new JsonArray(ar.result());

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

    @Test
    public void testGetCollectionsError(TestContext context) throws InterruptedException {
        this.testErrorInInitialize(context, "getCollections", "Failed to get collection list");
    }

    @Test
    public void testCreateCollectionError(TestContext context) throws InterruptedException {
        this.testErrorInInitialize(context, "createCollection", "Failed to add all collections");
    }

    @Test
    public void testCreateIndexWithOptionsError(TestContext context) throws InterruptedException {
        this.testErrorInInitialize(context, "createIndexWithOptions", "Failed to create all needed indexes in Mongo");
    }

    @Test
    public void testConnectionStatusBackOnline(TestContext context) throws InterruptedException {
        JsonObject proxyConfig = new JsonObject().put("functionsToFail", new JsonArray().add("find"));

        final PersistenceService persistenceErrorProxy = getPersistenceErrorProxy(proxyConfig);
        persistenceErrorProxy.initialize(context.asyncAssertSuccess());

        Appender<ILoggingEvent> stdout = rootLogger.getAppender("STDOUT");
        rootLogger.detachAppender(stdout);
        testAppender.start();
        persistenceErrorProxy.queryRiskLimit(LATEST, new JsonObject(), context.asyncAssertFailure());
        testAppender.waitForMessageContains(Level.INFO, "Back online");
        testAppender.stop();
        rootLogger.addAppender(stdout);

        persistenceErrorProxy.close();
    }

    @Test
    public void testConnectionStatusError(TestContext context) throws InterruptedException {
        JsonObject proxyConfig = new JsonObject().put("functionsToFail", new JsonArray().add("find").add("runCommand"));

        final PersistenceService persistenceErrorProxy = getPersistenceErrorProxy(proxyConfig);
        persistenceErrorProxy.initialize(context.asyncAssertSuccess());

        Appender<ILoggingEvent> stdout = rootLogger.getAppender("STDOUT");
        rootLogger.detachAppender(stdout);
        testAppender.start();
        persistenceErrorProxy.queryRiskLimit(LATEST, new JsonObject(), context.asyncAssertFailure());
        testAppender.waitForMessageContains(Level.ERROR, "Still disconnected");
        testAppender.stop();
        rootLogger.addAppender(stdout);

        persistenceErrorProxy.close();
    }

    private void testErrorInInitialize(TestContext context, String functionToFail, String expectedErrorMessage) throws InterruptedException {
        JsonObject proxyConfig = new JsonObject().put("functionsToFail", new JsonArray().add(functionToFail));
        final PersistenceService persistenceErrorProxy = getPersistenceErrorProxy(proxyConfig);

        testAppender.start();
        persistenceErrorProxy.initialize(context.asyncAssertSuccess());
        testAppender.waitForMessageContains(Level.ERROR, expectedErrorMessage);
        testAppender.waitForMessageContains(Level.ERROR, "Initialize failed, trying again...");
        testAppender.stop();

        persistenceErrorProxy.close();
    }

    private PersistenceService getPersistenceErrorProxy(JsonObject config) {
        MongoErrorClient mongoErrorClient = new MongoErrorClient(config);

        ProxyHelper.registerService(PersistenceService.class, vertx, new MongoPersistenceService(vertx, mongoErrorClient), PersistenceService.SERVICE_ADDRESS+"Error");
        return ProxyHelper.createProxy(PersistenceService.class, vertx, PersistenceService.SERVICE_ADDRESS+"Error");
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        MongoPersistenceServiceIT.rootLogger.detachAppender(testAppender);
        MongoPersistenceServiceIT.persistenceProxy.close();
        MongoPersistenceServiceIT.vertx.close(context.asyncAssertSuccess());
    }
}
