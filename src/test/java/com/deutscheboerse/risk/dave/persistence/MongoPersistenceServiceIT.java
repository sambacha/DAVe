package com.deutscheboerse.risk.dave.persistence;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.deutscheboerse.risk.dave.log.TestAppender;
import com.deutscheboerse.risk.dave.model.*;
import com.deutscheboerse.risk.dave.utils.DataHelper;
import com.deutscheboerse.risk.dave.utils.MongoFiller;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

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
    public void testAccountMargin(TestContext context) throws InterruptedException {
        MongoFiller mongoFiller = new MongoFiller(context, mongoClient);

        // Feed the data into the store
        mongoFiller.feedAccountMarginCollection(1, 30000);
        AccountMarginModel firstModel = (AccountMarginModel)mongoFiller.getLastModel().orElse(new AccountMarginModel());

        mongoFiller.feedAccountMarginCollection(2, 30000);
        AccountMarginModel secondModel = (AccountMarginModel)mongoFiller.getLastModel().orElse(new AccountMarginModel());

        // Check data
        persistenceProxy.findAccountMargin(RequestType.HISTORY, DataHelper.getQueryParams(firstModel), context.asyncAssertSuccess(res ->
                context.assertEquals(DataHelper.getMongoDocument(firstModel), new JsonArray(res).getJsonObject(0))
        ));
        persistenceProxy.findAccountMargin(RequestType.LATEST, DataHelper.getQueryParams(secondModel), context.asyncAssertSuccess(res ->
                context.assertEquals(DataHelper.getMongoDocument(secondModel), new JsonArray(res).getJsonObject(0))
        ));
    }


    @Test
    public void testLiquiGroupMargin(TestContext context) throws InterruptedException {
        MongoFiller mongoFiller = new MongoFiller(context, mongoClient);

        // Feed the data into the store
        mongoFiller.feedLiquiGroupMarginCollection(1, 30000);
        LiquiGroupMarginModel firstModel = (LiquiGroupMarginModel)mongoFiller.getLastModel().orElse(new LiquiGroupMarginModel());

        mongoFiller.feedLiquiGroupMarginCollection(2, 30000);
        LiquiGroupMarginModel secondModel = (LiquiGroupMarginModel)mongoFiller.getLastModel().orElse(new LiquiGroupMarginModel());

        // Check data
        persistenceProxy.findLiquiGroupMargin(RequestType.HISTORY, DataHelper.getQueryParams(firstModel), context.asyncAssertSuccess(res ->
                context.assertEquals(DataHelper.getMongoDocument(firstModel), new JsonArray(res).getJsonObject(0))
        ));
        persistenceProxy.findLiquiGroupMargin(RequestType.LATEST, DataHelper.getQueryParams(secondModel), context.asyncAssertSuccess(res ->
                context.assertEquals(DataHelper.getMongoDocument(secondModel), new JsonArray(res).getJsonObject(0))
        ));
    }

    @Test
    public void testLiquiGroupSplitMargin(TestContext context) throws InterruptedException {
        MongoFiller mongoFiller = new MongoFiller(context, mongoClient);

        // Feed the data into the store
        mongoFiller.feedLiquiGroupSplitMarginCollection(1, 30000);
        LiquiGroupSplitMarginModel firstModel = (LiquiGroupSplitMarginModel)mongoFiller.getLastModel().orElse(new LiquiGroupSplitMarginModel());

        mongoFiller.feedLiquiGroupSplitMarginCollection(2, 30000);
        LiquiGroupSplitMarginModel secondModel = (LiquiGroupSplitMarginModel)mongoFiller.getLastModel().orElse(new LiquiGroupSplitMarginModel());

        // Check data
        persistenceProxy.findLiquiGroupSplitMargin(RequestType.HISTORY, DataHelper.getQueryParams(firstModel), context.asyncAssertSuccess(res ->
                context.assertEquals(DataHelper.getMongoDocument(firstModel), new JsonArray(res).getJsonObject(0))
        ));
        persistenceProxy.findLiquiGroupSplitMargin(RequestType.LATEST, DataHelper.getQueryParams(secondModel), context.asyncAssertSuccess(res ->
                context.assertEquals(DataHelper.getMongoDocument(secondModel), new JsonArray(res).getJsonObject(0))
        ));
    }

    @Test
    public void testPoolMargin(TestContext context) throws InterruptedException {
        MongoFiller mongoFiller = new MongoFiller(context, mongoClient);

        // Feed the data into the store
        mongoFiller.feedPoolMarginCollection(1, 30000);
        PoolMarginModel firstModel = (PoolMarginModel)mongoFiller.getLastModel().orElse(new PoolMarginModel());

        mongoFiller.feedPoolMarginCollection(2, 30000);
        PoolMarginModel secondModel = (PoolMarginModel)mongoFiller.getLastModel().orElse(new PoolMarginModel());

        // Check data
        persistenceProxy.findPoolMargin(RequestType.HISTORY, DataHelper.getQueryParams(firstModel), context.asyncAssertSuccess(res ->
                context.assertEquals(DataHelper.getMongoDocument(firstModel), new JsonArray(res).getJsonObject(0))
        ));
        persistenceProxy.findPoolMargin(RequestType.LATEST, DataHelper.getQueryParams(secondModel), context.asyncAssertSuccess(res ->
                context.assertEquals(DataHelper.getMongoDocument(secondModel), new JsonArray(res).getJsonObject(0))
        ));
    }

    @Test
    public void testPositionReportMargin(TestContext context) throws InterruptedException {
        MongoFiller mongoFiller = new MongoFiller(context, mongoClient);

        // Feed the data into the store
        mongoFiller.feedPositionReportCollection(1, 30000);
        PositionReportModel firstModel = (PositionReportModel)mongoFiller.getLastModel().orElse(new PositionReportModel());

        mongoFiller.feedPositionReportCollection(2, 30000);
        PositionReportModel secondModel = (PositionReportModel)mongoFiller.getLastModel().orElse(new PositionReportModel());

        // Check data
        persistenceProxy.findPositionReport(RequestType.HISTORY, DataHelper.getQueryParams(firstModel), context.asyncAssertSuccess(res ->
            context.assertEquals(DataHelper.getMongoDocument(firstModel), new JsonArray(res).getJsonObject(0))
        ));
        persistenceProxy.findPositionReport(RequestType.LATEST, DataHelper.getQueryParams(secondModel), context.asyncAssertSuccess(res ->
            context.assertEquals(DataHelper.getMongoDocument(secondModel), new JsonArray(res).getJsonObject(0))
        ));
    }

    @Test
    public void testRiskLimitUtilization(TestContext context) throws InterruptedException {
        MongoFiller mongoFiller = new MongoFiller(context, mongoClient);

        // Feed the data into the store
        mongoFiller.feedRiskLimitUtilizationCollection(1, 30000);
        RiskLimitUtilizationModel firstModel = (RiskLimitUtilizationModel)mongoFiller.getLastModel().orElse(new RiskLimitUtilizationModel());

        mongoFiller.feedRiskLimitUtilizationCollection(2, 30000);
        RiskLimitUtilizationModel secondModel = (RiskLimitUtilizationModel)mongoFiller.getLastModel().orElse(new RiskLimitUtilizationModel());

        // Check data
        persistenceProxy.findRiskLimitUtilization(RequestType.HISTORY, DataHelper.getQueryParams(firstModel), context.asyncAssertSuccess(res ->
                context.assertEquals(DataHelper.getMongoDocument(firstModel), new JsonArray(res).getJsonObject(0))
        ));
        persistenceProxy.findRiskLimitUtilization(RequestType.LATEST, DataHelper.getQueryParams(secondModel), context.asyncAssertSuccess(res ->
                context.assertEquals(DataHelper.getMongoDocument(secondModel), new JsonArray(res).getJsonObject(0))
        ));
    }

    @Test
    public void testConnectionStatusBackOnline(TestContext context) throws InterruptedException {
        JsonObject proxyConfig = new JsonObject().put("functionsToFail", new JsonArray().add("runCommand:aggregate"));

        final PersistenceService persistenceErrorProxy = getPersistenceErrorProxy(proxyConfig);
        persistenceErrorProxy.initialize(context.asyncAssertSuccess());

        Appender<ILoggingEvent> stdout = rootLogger.getAppender("STDOUT");
        rootLogger.detachAppender(stdout);
        testAppender.start();
        persistenceErrorProxy.findAccountMargin(RequestType.HISTORY, new JsonObject(), context.asyncAssertFailure());
        testAppender.waitForMessageContains(Level.INFO, "Back online");
        testAppender.stop();
        rootLogger.addAppender(stdout);

        persistenceErrorProxy.close();
    }

    @Test
    public void testConnectionStatusError(TestContext context) throws InterruptedException {
        JsonObject proxyConfig = new JsonObject().put("functionsToFail", new JsonArray().add("runCommand:aggregate").add("runCommand:ping"));

        final PersistenceService persistenceErrorProxy = getPersistenceErrorProxy(proxyConfig);
        persistenceErrorProxy.initialize(context.asyncAssertSuccess());

        Appender<ILoggingEvent> stdout = rootLogger.getAppender("STDOUT");
        rootLogger.detachAppender(stdout);
        testAppender.start();
        persistenceErrorProxy.findAccountMargin(RequestType.HISTORY, new JsonObject(), context.asyncAssertFailure());
        testAppender.waitForMessageContains(Level.ERROR, "Still disconnected");
        testAppender.stop();
        rootLogger.addAppender(stdout);

        persistenceErrorProxy.close();
    }

    private PersistenceService getPersistenceErrorProxy(JsonObject config) {
        MongoErrorClient mongoErrorClient = new MongoErrorClient(config);

        ProxyHelper.registerService(PersistenceService.class, vertx, new MongoPersistenceService(vertx, mongoErrorClient), PersistenceService.SERVICE_ADDRESS+"Error");
        return ProxyHelper.createProxy(PersistenceService.class, vertx, PersistenceService.SERVICE_ADDRESS+"Error");
    }

    @After
    public void cleanup() {
        testAppender.clear();
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        MongoPersistenceServiceIT.rootLogger.detachAppender(testAppender);
        MongoPersistenceServiceIT.persistenceProxy.close();
        MongoPersistenceServiceIT.vertx.close(context.asyncAssertSuccess());
    }
}
