package com.deutscheboerse.risk.dave.persistence;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.deutscheboerse.risk.dave.log.TestAppender;
import com.deutscheboerse.risk.dave.model.*;
import com.deutscheboerse.risk.dave.utils.DataHelper;
import com.deutscheboerse.risk.dave.utils.TestConfig;
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

@RunWith(VertxUnitRunner.class)
public class RestPersistenceServiceTest {
    private static final TestAppender testAppender = TestAppender.getAppender(RestPersistenceService.class);
    private static final Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    private static Vertx vertx;
    private static StoreManagerMock storageManager;
    private static PersistenceService persistenceProxy;

    @BeforeClass
    public static void setUp(TestContext context) {
        RestPersistenceServiceTest.vertx = Vertx.vertx();

        JsonObject config = TestConfig.getStoreManagerConfig();
        storageManager = new StoreManagerMock(vertx, config);
        storageManager.listen(context.asyncAssertSuccess());

        ProxyHelper.registerService(PersistenceService.class, vertx, new RestPersistenceService(vertx, config), PersistenceService.SERVICE_ADDRESS);
        RestPersistenceServiceTest.persistenceProxy = ProxyHelper.createProxy(PersistenceService.class, vertx, PersistenceService.SERVICE_ADDRESS);
        RestPersistenceServiceTest.persistenceProxy.initialize(context.asyncAssertSuccess());

        rootLogger.addAppender(testAppender);
    }

    @Test
    public void testAccountMarginQuery(TestContext context) throws IOException {
        int firstMsgCount = DataHelper.getJsonObjectCount("accountMargin", 1);
        Async asyncQuery1 = context.async(firstMsgCount);
        DataHelper.readTTSaveFile("accountMargin", 1, (json) -> {
            AccountMarginModel accountMarginModel = new AccountMarginModel(json);
            persistenceProxy.queryAccountMargin(RequestType.HISTORY, accountMarginModel, ar -> {
                if (ar.succeeded()) {
                    asyncQuery1.countDown();
                } else {
                    context.fail(ar.cause());
                }
            });
        });
        asyncQuery1.awaitSuccess(30000);

        int secondMsgCount = DataHelper.getJsonObjectCount("accountMargin", 2);
        Async asyncQuery2 = context.async(secondMsgCount);
        DataHelper.readTTSaveFile("accountMargin", 2, (json) -> {
            AccountMarginModel accountMarginModel = new AccountMarginModel(json);
            persistenceProxy.queryAccountMargin(RequestType.LATEST, accountMarginModel, ar -> {
                if (ar.succeeded()) {
                    asyncQuery2.countDown();
                } else {
                    context.fail(ar.cause());
                }
            });
        });
        asyncQuery2.awaitSuccess(30000);
    }

    @Test
    public void testLiquiGroupMarginQuery(TestContext context) throws IOException {
        int firstMsgCount = DataHelper.getJsonObjectCount("liquiGroupMargin", 1);
        Async asyncQuery1 = context.async(firstMsgCount);
        DataHelper.readTTSaveFile("liquiGroupMargin", 1, (json) -> {
            LiquiGroupMarginModel liquiGroupMarginModel = new LiquiGroupMarginModel(json);
            persistenceProxy.queryLiquiGroupMargin(RequestType.HISTORY, liquiGroupMarginModel, ar -> {
                if (ar.succeeded()) {
                    asyncQuery1.countDown();
                } else {
                    context.fail(ar.cause());
                }
            });
        });
        asyncQuery1.awaitSuccess(30000);

        int secondMsgCount = DataHelper.getJsonObjectCount("liquiGroupMargin", 2);
        Async asyncQuery2 = context.async(secondMsgCount);
        DataHelper.readTTSaveFile("liquiGroupMargin", 2, (json) -> {
            LiquiGroupMarginModel liquiGroupMarginModel = new LiquiGroupMarginModel(json);
            persistenceProxy.queryLiquiGroupMargin(RequestType.LATEST, liquiGroupMarginModel, ar -> {
                if (ar.succeeded()) {
                    asyncQuery2.countDown();
                } else {
                    context.fail(ar.cause());
                }
            });
        });
        asyncQuery2.awaitSuccess(30000);
    }

    @Test
    public void testLiquiGroupSplitMarginQuery(TestContext context) throws IOException {
        int firstMsgCount = DataHelper.getJsonObjectCount("liquiGroupSplitMargin", 1);
        Async asyncQuery1 = context.async(firstMsgCount);
        DataHelper.readTTSaveFile("liquiGroupSplitMargin", 1, (json) -> {
            LiquiGroupSplitMarginModel liquiGroupSplitMarginModel = new LiquiGroupSplitMarginModel(json);
            persistenceProxy.queryLiquiGroupSplitMargin(RequestType.HISTORY, liquiGroupSplitMarginModel, ar -> {
                if (ar.succeeded()) {
                    asyncQuery1.countDown();
                } else {
                    context.fail(ar.cause());
                }
            });
        });
        asyncQuery1.awaitSuccess(30000);

        int secondMsgCount = DataHelper.getJsonObjectCount("liquiGroupSplitMargin", 2);
        Async asyncQuery2 = context.async(secondMsgCount);
        DataHelper.readTTSaveFile("liquiGroupSplitMargin", 2, (json) -> {
            LiquiGroupSplitMarginModel liquiGroupSplitMarginModel = new LiquiGroupSplitMarginModel(json);
            persistenceProxy.queryLiquiGroupSplitMargin(RequestType.LATEST, liquiGroupSplitMarginModel, ar -> {
                if (ar.succeeded()) {
                    asyncQuery2.countDown();
                } else {
                    context.fail(ar.cause());
                }
            });
        });
        asyncQuery2.awaitSuccess(30000);
    }

    @Test
    public void testPoolMarginQuery(TestContext context) throws IOException {
        int firstMsgCount = DataHelper.getJsonObjectCount("poolMargin", 1);
        Async asyncFirstSnapshotQuery = context.async(firstMsgCount);
        DataHelper.readTTSaveFile("poolMargin", 1, (json) -> {
            PoolMarginModel poolMarginModel = new PoolMarginModel(json);
            persistenceProxy.queryPoolMargin(RequestType.HISTORY, poolMarginModel, ar -> {
                if (ar.succeeded()) {
                    asyncFirstSnapshotQuery.countDown();
                } else {
                    context.fail(ar.cause());
                }
            });
        });
        asyncFirstSnapshotQuery.awaitSuccess(30000);

        int secondMsgCount = DataHelper.getJsonObjectCount("poolMargin", 2);
        Async asyncSecondSnapshotQuery = context.async(secondMsgCount);
        DataHelper.readTTSaveFile("poolMargin", 2, (json) -> {
            PoolMarginModel poolMarginModel = new PoolMarginModel(json);
            persistenceProxy.queryPoolMargin(RequestType.LATEST, poolMarginModel, ar -> {
                if (ar.succeeded()) {
                    asyncSecondSnapshotQuery.countDown();
                } else {
                    context.fail(ar.cause());
                }
            });
        });
        asyncSecondSnapshotQuery.awaitSuccess(30000);
    }

    @Test
    public void testPositionReportQuery(TestContext context) throws IOException {
        int firstMsgCount = DataHelper.getJsonObjectCount("positionReport", 1);
        Async asyncFirstSnapshotQuery = context.async(firstMsgCount);
        DataHelper.readTTSaveFile("positionReport", 1, (json) -> {
            PositionReportModel positionReportModel = new PositionReportModel(json);
            persistenceProxy.queryPositionReport(RequestType.HISTORY, positionReportModel, ar -> {
                if (ar.succeeded()) {
                    asyncFirstSnapshotQuery.countDown();
                } else {
                    context.fail(ar.cause());
                }
            });
        });
        asyncFirstSnapshotQuery.awaitSuccess(30000);
        int secondMsgCount = DataHelper.getJsonObjectCount("positionReport", 2);
        Async asyncSecondSnapshotQuery = context.async(secondMsgCount);
        DataHelper.readTTSaveFile("positionReport", 2, (json) -> {
            PositionReportModel positionReportModel = new PositionReportModel(json);
            persistenceProxy.queryPositionReport(RequestType.LATEST, positionReportModel, ar -> {
                if (ar.succeeded()) {
                    asyncSecondSnapshotQuery.countDown();
                } else {
                    context.fail(ar.cause());
                }
            });
        });
        asyncSecondSnapshotQuery.awaitSuccess(30000);
    }

    @Test
    public void testRiskLimitUtilizationQuery(TestContext context) throws IOException {
        int firstMsgCount = DataHelper.getJsonObjectCount("riskLimitUtilization", 1);
        Async asyncFirstSnapshotQuery = context.async(firstMsgCount);
        DataHelper.readTTSaveFile("riskLimitUtilization", 1, (json) -> {
            RiskLimitUtilizationModel model = new RiskLimitUtilizationModel(json);
            persistenceProxy.queryRiskLimitUtilization(RequestType.HISTORY, model, ar -> {
                if (ar.succeeded()) {
                    asyncFirstSnapshotQuery.countDown();
                } else {
                    context.fail(ar.cause());
                }
            });
        });
        asyncFirstSnapshotQuery.awaitSuccess(30000);
        int secondMsgCount = DataHelper.getJsonObjectCount("riskLimitUtilization", 2);
        Async asyncSecondSnapshotQuery = context.async(secondMsgCount);
        DataHelper.readTTSaveFile("riskLimitUtilization", 2, (json) -> {
            RiskLimitUtilizationModel model = new RiskLimitUtilizationModel(json);
            persistenceProxy.queryRiskLimitUtilization(RequestType.LATEST, model, ar -> {
                if (ar.succeeded()) {
                    asyncSecondSnapshotQuery.countDown();
                } else {
                    context.fail(ar.cause());
                }
            });
        });
        asyncSecondSnapshotQuery.awaitSuccess(30000);
    }

    @Test
    public void testInitialConnectionFailed(TestContext context) throws InterruptedException {
        storageManager.setHealth(false);
        testAppender.start();
        persistenceProxy.initialize(context.asyncAssertSuccess());
        testAppender.waitForMessageContains(Level.ERROR, "Cannot connect to StoreManager, trying again...");
        testAppender.stop();
        storageManager.setHealth(true);
    }

    @Test
    public void testQueryFailure(TestContext context) throws InterruptedException {
        storageManager.setHealth(false);
        testAppender.start();
        Async queryAsync = context.async();
        persistenceProxy.queryAccountMargin(RequestType.HISTORY, new AccountMarginModel(new JsonObject()),
                context.asyncAssertFailure(ar -> queryAsync.complete()));
        queryAsync.awaitSuccess();
        testAppender.waitForMessageContains(Level.ERROR, "/api/v1.0/query/am/history failed:");
        testAppender.stop();
        storageManager.setHealth(true);
    }

    @Test
    public void testBackOnline(TestContext context) throws InterruptedException {
        storageManager.setHealth(false);
        testAppender.start();
        Async queryAsync = context.async();
        persistenceProxy.queryAccountMargin(RequestType.HISTORY, new AccountMarginModel(new JsonObject()),
                context.asyncAssertFailure(ar -> queryAsync.complete()));
        queryAsync.awaitSuccess();
        testAppender.waitForMessageContains(Level.ERROR, "/api/v1.0/query/am/history failed:");
        testAppender.waitForMessageContains(Level.ERROR, "Still disconnected");
        storageManager.setHealth(true);
        testAppender.waitForMessageContains(Level.INFO, "Back online");
        testAppender.stop();
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
        testAppender.waitForMessageContains(Level.ERROR, "/api/v1.0/query/am/history failed:");
        testAppender.waitForMessageContains(Level.ERROR, "Still disconnected");
        storageManager.listen(context.asyncAssertSuccess());
        testAppender.waitForMessageContains(Level.INFO, "Back online");
        testAppender.stop();
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        persistenceProxy.close();
        storageManager.close(context.asyncAssertSuccess());
        vertx.close(context.asyncAssertSuccess());
    }
}
