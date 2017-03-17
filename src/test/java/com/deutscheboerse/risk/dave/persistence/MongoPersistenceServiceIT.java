package com.deutscheboerse.risk.dave.persistence;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.deutscheboerse.risk.dave.log.TestAppender;
import com.deutscheboerse.risk.dave.model.*;
import com.deutscheboerse.risk.dave.utils.MongoFiller;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(VertxUnitRunner.class)
public class MongoPersistenceServiceIT {
    private static final TestAppender testAppender = TestAppender.getAppender(MongoPersistenceService.class);
    private static final Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    private static Vertx vertx;
    private static MongoClient mongoClient;
    private static PersistenceService persistenceProxy;

    @BeforeClass
    public static void setUp(TestContext context) {
        MongoPersistenceServiceIT.vertx = Vertx.vertx();

        JsonObject mongoConfig = new JsonObject();
        mongoConfig.put("db_name", "DAVe-Test" + UUID.randomUUID().getLeastSignificantBits());
        mongoConfig.put("connection_string", "mongodb://localhost:" + System.getProperty("mongodb.port", "27017")+"/?waitqueuemultiple=20000");
        MongoPersistenceServiceIT.mongoClient = MongoClient.createShared(MongoPersistenceServiceIT.vertx, mongoConfig);

        ProxyHelper.registerService(PersistenceService.class, vertx, new MongoPersistenceService(vertx, MongoPersistenceServiceIT.mongoClient), PersistenceService.SERVICE_ADDRESS);
        MongoPersistenceServiceIT.persistenceProxy = ProxyHelper.createProxy(PersistenceService.class, vertx, PersistenceService.SERVICE_ADDRESS);
        MongoPersistenceServiceIT.persistenceProxy.initialize(context.asyncAssertSuccess());

        rootLogger.addAppender(testAppender);
    }

    @Test
    public void checkCollectionsExist(TestContext context) {
        List<String> requiredCollections = new ArrayList<>();
        requiredCollections.add("AccountMargin");
        requiredCollections.add("AccountMargin.latest");
        requiredCollections.add("LiquiGroupMargin");
        requiredCollections.add("LiquiGroupMargin.latest");
        requiredCollections.add("LiquiGroupSplitMargin");
        requiredCollections.add("LiquiGroupSplitMargin.latest");
        requiredCollections.add("PoolMargin");
        requiredCollections.add("PoolMargin.latest");
        requiredCollections.add("PositionReport");
        requiredCollections.add("PositionReport.latest");
        requiredCollections.add("RiskLimitUtilization");
        requiredCollections.add("RiskLimitUtilization.latest");
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
    public void testAccountMargin(TestContext context) throws InterruptedException {
        MongoFiller mongoFiller = new MongoFiller(context, persistenceProxy);

        // Feed the data into the store
        int ttsaveCount1 = mongoFiller.feedAccountMarginCollection(1, 30000);
        AccountMarginModel historyModel = (AccountMarginModel)mongoFiller.getLastModel().orElse(new AccountMarginModel());

        int ttsaveCount2 = mongoFiller.feedAccountMarginCollection(2, 30000);
        AccountMarginModel latestModel = (AccountMarginModel)mongoFiller.getLastModel().orElse(new AccountMarginModel());

        // Check size of collections
        checkCollectionSize(context, historyModel.getHistoryCollection(), ttsaveCount1 + ttsaveCount2);
        checkCollectionSize(context, latestModel.getLatestCollection(), ttsaveCount2);

        // Check data
        persistenceProxy.find(historyModel.getHistoryCollection(), historyModel.getQueryParams(), context.asyncAssertSuccess(res ->
                context.assertEquals(historyModel.getMongoDocument(), new JsonArray(res).getJsonObject(0))
        ));
        persistenceProxy.find(latestModel.getLatestCollection(), latestModel.getQueryParams(), context.asyncAssertSuccess(res ->
                context.assertEquals(latestModel.getMongoDocument(), new JsonArray(res).getJsonObject(0))
        ));
    }


    @Test
    public void testLiquiGroupMargin(TestContext context) throws InterruptedException {
        MongoFiller mongoFiller = new MongoFiller(context, persistenceProxy);

        // Feed the data into the store
        int ttsaveCount1 = mongoFiller.feedLiquiGroupMarginCollection(1, 30000);
        LiquiGroupMarginModel historyModel = (LiquiGroupMarginModel)mongoFiller.getLastModel().orElse(new LiquiGroupMarginModel());

        int ttsaveCount2 = mongoFiller.feedLiquiGroupMarginCollection(2, 30000);
        LiquiGroupMarginModel latestModel = (LiquiGroupMarginModel)mongoFiller.getLastModel().orElse(new LiquiGroupMarginModel());

        // Check size of collections
        checkCollectionSize(context, historyModel.getHistoryCollection(), ttsaveCount1 + ttsaveCount2);
        checkCollectionSize(context, latestModel.getLatestCollection(), ttsaveCount2);

        // Check data
        persistenceProxy.find(historyModel.getHistoryCollection(), historyModel.getQueryParams(), context.asyncAssertSuccess(res ->
                context.assertEquals(historyModel.getMongoDocument(), new JsonArray(res).getJsonObject(0))
        ));
        persistenceProxy.find(latestModel.getLatestCollection(), latestModel.getQueryParams(), context.asyncAssertSuccess(res ->
                context.assertEquals(latestModel.getMongoDocument(), new JsonArray(res).getJsonObject(0))
        ));
    }

    @Test
    public void testLiquiGroupSplitMargin(TestContext context) throws InterruptedException {
        MongoFiller mongoFiller = new MongoFiller(context, persistenceProxy);

        // Feed the data into the store
        int ttsaveCount1 = mongoFiller.feedLiquiGroupSplitMarginCollection(1, 30000);
        LiquiGroupSplitMarginModel historyModel = (LiquiGroupSplitMarginModel)mongoFiller.getLastModel().orElse(new LiquiGroupSplitMarginModel());

        int ttsaveCount2 = mongoFiller.feedLiquiGroupSplitMarginCollection(2, 30000);
        LiquiGroupSplitMarginModel latestModel = (LiquiGroupSplitMarginModel)mongoFiller.getLastModel().orElse(new LiquiGroupSplitMarginModel());

        // Check size of collections
        checkCollectionSize(context, historyModel.getHistoryCollection(), ttsaveCount1 + ttsaveCount2);
        checkCollectionSize(context, latestModel.getLatestCollection(), ttsaveCount2);

        // Check data
        persistenceProxy.find(historyModel.getHistoryCollection(), historyModel.getQueryParams(), context.asyncAssertSuccess(res ->
                context.assertEquals(historyModel.getMongoDocument(), new JsonArray(res).getJsonObject(0))
        ));
        persistenceProxy.find(latestModel.getLatestCollection(), latestModel.getQueryParams(), context.asyncAssertSuccess(res ->
                context.assertEquals(latestModel.getMongoDocument(), new JsonArray(res).getJsonObject(0))
        ));
    }

    @Test
    public void testPoolMargin(TestContext context) throws InterruptedException {
        MongoFiller mongoFiller = new MongoFiller(context, persistenceProxy);

        // Feed the data into the store
        int ttsaveCount1 = mongoFiller.feedPoolMarginCollection(1, 30000);
        PoolMarginModel historyModel = (PoolMarginModel)mongoFiller.getLastModel().orElse(new PoolMarginModel());

        int ttsaveCount2 = mongoFiller.feedPoolMarginCollection(2, 30000);
        PoolMarginModel latestModel = (PoolMarginModel)mongoFiller.getLastModel().orElse(new PoolMarginModel());

        // Check size of collections
        checkCollectionSize(context, historyModel.getHistoryCollection(), ttsaveCount1 + ttsaveCount2);
        checkCollectionSize(context, latestModel.getLatestCollection(), ttsaveCount2);

        // Check data
        persistenceProxy.find(historyModel.getHistoryCollection(), historyModel.getQueryParams(), context.asyncAssertSuccess(res ->
                context.assertEquals(historyModel.getMongoDocument(), new JsonArray(res).getJsonObject(0))
        ));
        persistenceProxy.find(latestModel.getLatestCollection(), latestModel.getQueryParams(), context.asyncAssertSuccess(res ->
                context.assertEquals(latestModel.getMongoDocument(), new JsonArray(res).getJsonObject(0))
        ));
    }

    @Test
    public void testPositionReportMargin(TestContext context) throws InterruptedException {
        MongoFiller mongoFiller = new MongoFiller(context, persistenceProxy);

        // Feed the data into the store
        int ttsaveCount1 = mongoFiller.feedPositionReportCollection(1, 30000);
        PositionReportModel historyModel = (PositionReportModel)mongoFiller.getLastModel().orElse(new PositionReportModel());

        int ttsaveCount2 = mongoFiller.feedPositionReportCollection(2, 30000);
        PositionReportModel latestModel = (PositionReportModel)mongoFiller.getLastModel().orElse(new PositionReportModel());

        // Check size of collections
        checkCollectionSize(context, historyModel.getHistoryCollection(), ttsaveCount1 + ttsaveCount2);
        checkCollectionSize(context, latestModel.getLatestCollection(), ttsaveCount2);

        // Check data
        persistenceProxy.find(historyModel.getHistoryCollection(), historyModel.getQueryParams(), context.asyncAssertSuccess(res ->
                context.assertEquals(historyModel.getMongoDocument(), new JsonArray(res).getJsonObject(0))
        ));
        persistenceProxy.find(latestModel.getLatestCollection(), latestModel.getQueryParams(), context.asyncAssertSuccess(res ->
                context.assertEquals(latestModel.getMongoDocument(), new JsonArray(res).getJsonObject(0))
        ));
    }

    @Test
    public void testRiskLimitUtilization(TestContext context) throws InterruptedException {
        MongoFiller mongoFiller = new MongoFiller(context, persistenceProxy);

        // Feed the data into the store
        int ttsaveCount1 = mongoFiller.feedRiskLimitUtilizationCollection(1, 30000);
        RiskLimitUtilizationModel historyModel = (RiskLimitUtilizationModel)mongoFiller.getLastModel().orElse(new RiskLimitUtilizationModel());

        int ttsaveCount2 = mongoFiller.feedRiskLimitUtilizationCollection(2, 30000);
        RiskLimitUtilizationModel latestModel = (RiskLimitUtilizationModel)mongoFiller.getLastModel().orElse(new RiskLimitUtilizationModel());

        // Check size of collections
        checkCollectionSize(context, historyModel.getHistoryCollection(), ttsaveCount1 + ttsaveCount2);
        checkCollectionSize(context, latestModel.getLatestCollection(), ttsaveCount2);

        // Check data
        persistenceProxy.find(historyModel.getHistoryCollection(), historyModel.getQueryParams(), context.asyncAssertSuccess(res ->
                context.assertEquals(historyModel.getMongoDocument(), new JsonArray(res).getJsonObject(0))
        ));
        persistenceProxy.find(latestModel.getLatestCollection(), latestModel.getQueryParams(), context.asyncAssertSuccess(res ->
                context.assertEquals(latestModel.getMongoDocument(), new JsonArray(res).getJsonObject(0))
        ));
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
        JsonObject proxyConfig = new JsonObject().put("functionsToFail", new JsonArray().add("findWithOptions"));

        final PersistenceService persistenceErrorProxy = getPersistenceErrorProxy(proxyConfig);
        persistenceErrorProxy.initialize(context.asyncAssertSuccess());

        Appender<ILoggingEvent> stdout = rootLogger.getAppender("STDOUT");
        rootLogger.detachAppender(stdout);
        testAppender.start();
        persistenceErrorProxy.find(new AccountMarginModel().getHistoryCollection(), new JsonObject(), context.asyncAssertFailure());
        testAppender.waitForMessageContains(Level.INFO, "Back online");
        testAppender.stop();
        rootLogger.addAppender(stdout);

        persistenceErrorProxy.close();
    }

    @Test
    public void testConnectionStatusError(TestContext context) throws InterruptedException {
        JsonObject proxyConfig = new JsonObject().put("functionsToFail", new JsonArray().add("findWithOptions").add("runCommand"));

        final PersistenceService persistenceErrorProxy = getPersistenceErrorProxy(proxyConfig);
        persistenceErrorProxy.initialize(context.asyncAssertSuccess());

        Appender<ILoggingEvent> stdout = rootLogger.getAppender("STDOUT");
        rootLogger.detachAppender(stdout);
        testAppender.start();
        persistenceErrorProxy.find(new AccountMarginModel().getHistoryCollection(), new JsonObject(), context.asyncAssertFailure());
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

    private void checkCollectionSize(TestContext context, String collection, long count) {
        Async asyncHistoryCount = context.async();
        MongoPersistenceServiceIT.mongoClient.count(collection, new JsonObject(), ar -> {
            if (ar.succeeded()) {
                context.assertEquals(count, ar.result());
                asyncHistoryCount.complete();
            } else {
                context.fail(ar.cause());
            }
        });
        asyncHistoryCount.awaitSuccess(5000);
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        MongoPersistenceServiceIT.rootLogger.detachAppender(testAppender);
        MongoPersistenceServiceIT.persistenceProxy.close();
        MongoPersistenceServiceIT.vertx.close(context.asyncAssertSuccess());
    }
}
