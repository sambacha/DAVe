package com.deutscheboerse.risk.dave.persistence;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.deutscheboerse.risk.dave.log.TestAppender;
import com.deutscheboerse.risk.dave.model.*;
import com.deutscheboerse.risk.dave.utils.DataHelper;
import com.deutscheboerse.risk.dave.utils.TestConfig;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Function;

@RunWith(VertxUnitRunner.class)
public class GrpcPersistenceServiceTest {
    private static final TestAppender testAppender = TestAppender.getAppender(GrpcPersistenceService.class);
    private static final Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    private static Vertx vertx;
    private static StoreManagerMock storageManager;
    private static PersistenceService persistenceProxy;

    @BeforeClass
    public static void setUp(TestContext context) throws IOException {
        GrpcPersistenceServiceTest.vertx = Vertx.vertx();

        JsonObject config = TestConfig.getStoreManagerConfig();
        storageManager = new StoreManagerMock(vertx);
        storageManager.listen(context.asyncAssertSuccess());

        ProxyHelper.registerService(PersistenceService.class, vertx, new GrpcPersistenceService(vertx, config), PersistenceService.SERVICE_ADDRESS);
        GrpcPersistenceServiceTest.persistenceProxy = ProxyHelper.createProxy(PersistenceService.class, vertx, PersistenceService.SERVICE_ADDRESS);
        GrpcPersistenceServiceTest.persistenceProxy.initialize(context.asyncAssertSuccess());

        rootLogger.addAppender(testAppender);
    }

    @Test
    public void testAccountMarginQuery(TestContext context) throws IOException {
        this.testQuery(context, "accountMargin", AccountMarginModel::new,
                persistenceProxy::queryRiskLimitUtilization);
    }

    @Test
    public void testLiquiGroupMarginQuery(TestContext context) throws IOException {
        this.testQuery(context, "liquiGroupMargin", LiquiGroupMarginModel::new,
                persistenceProxy::queryLiquiGroupMargin);
    }

    @Test
    public void testLiquiGroupSplitMarginQuery(TestContext context) throws IOException {
        this.testQuery(context, "liquiGroupSplitMargin", LiquiGroupSplitMarginModel::new,
                persistenceProxy::queryLiquiGroupSplitMargin);
    }

    @Test
    public void testPoolMarginQuery(TestContext context) throws IOException {
        this.testQuery(context, "poolMargin", PoolMarginModel::new,
                persistenceProxy::queryPoolMargin);
    }

    @Test
    public void testPositionReportQuery(TestContext context) throws IOException {
        this.testQuery(context, "positionReport", PositionReportModel::new,
                persistenceProxy::queryPositionReport);
    }

    @Test
    public void testRiskLimitUtilizationQuery(TestContext context) throws IOException {
        this.testQuery(context, "riskLimitUtilization", RiskLimitUtilizationModel::new,
                persistenceProxy::queryRiskLimitUtilization);
    }

    @Test
    public void testQueryFailure(TestContext context) throws InterruptedException {
        storageManager.setHealth(false);
        testAppender.start();
        Async queryAsync = context.async();
        persistenceProxy.queryAccountMargin(RequestType.HISTORY, new AccountMarginModel(new JsonObject()),
                context.asyncAssertFailure(ar -> queryAsync.complete()));
        queryAsync.awaitSuccess();
        testAppender.waitForMessageContains(Level.ERROR, "INVALID_ARGUMENT");
        testAppender.stop();
        storageManager.setHealth(true);
    }

    @Test
    public void testExceptionHandler(TestContext context) throws InterruptedException {
        Async closeAsync = context.async();
        storageManager.close(context.asyncAssertSuccess(i -> closeAsync.complete()));
        closeAsync.awaitSuccess();
        testAppender.start();
        Async queryAsync = context.async();
        persistenceProxy.queryAccountMargin(RequestType.HISTORY, new AccountMarginModel(new JsonObject()),
                context.asyncAssertFailure(ar -> queryAsync.complete()));
        queryAsync.awaitSuccess();
        testAppender.waitForMessageContains(Level.ERROR, "UNAVAILABLE");
        storageManager.listen(context.asyncAssertSuccess());
        testAppender.stop();
    }

    private interface QueryFunction {
        void query(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler);
    }

    private <T extends AbstractModel>
    void testQuery(TestContext context,
                   String dataFolder,
                   Function<JsonObject, T> modelFactory,
                   QueryFunction queryFunction) throws IOException {

        int firstMsgCount = DataHelper.getJsonObjectCount(dataFolder, 1);
        Async asyncQuery1 = context.async(firstMsgCount);
        DataHelper.readTTSaveFile(dataFolder, 1, (json) -> {
            T model = modelFactory.apply(json);
            queryFunction.query(RequestType.HISTORY, model, ar -> {
                if (ar.succeeded()) {
                    asyncQuery1.countDown();
                } else {
                    context.fail(ar.cause());
                }
            });
        });
        asyncQuery1.awaitSuccess(30000);

        int secondMsgCount = DataHelper.getJsonObjectCount(dataFolder, 2);
        Async asyncQuery2 = context.async(secondMsgCount);
        DataHelper.readTTSaveFile(dataFolder, 2, (json) -> {
            T model = modelFactory.apply(json);
            queryFunction.query(RequestType.LATEST, model, ar -> {
                if (ar.succeeded()) {
                    asyncQuery2.countDown();
                } else {
                    context.fail(ar.cause());
                }
            });
        });
        asyncQuery2.awaitSuccess(30000);
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        persistenceProxy.close();
        storageManager.close(context.asyncAssertSuccess());
        vertx.close(context.asyncAssertSuccess());
    }
}
