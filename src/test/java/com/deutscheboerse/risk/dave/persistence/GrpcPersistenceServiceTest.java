package com.deutscheboerse.risk.dave.persistence;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.deutscheboerse.risk.dave.log.TestAppender;
import com.deutscheboerse.risk.dave.model.Model;
import com.deutscheboerse.risk.dave.utils.DataHelper;
import com.deutscheboerse.risk.dave.utils.TestConfig;
import io.vertx.core.*;
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
import java.util.List;

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
        this.testQuery(context, "accountMargin", persistenceProxy::queryAccountMargin);
    }

    @Test
    public void testLiquiGroupMarginQuery(TestContext context) throws IOException {
        this.testQuery(context, "liquiGroupMargin", persistenceProxy::queryLiquiGroupMargin);
    }

    @Test
    public void testLiquiGroupSplitMarginQuery(TestContext context) throws IOException {
        this.testQuery(context, "liquiGroupSplitMargin", persistenceProxy::queryLiquiGroupSplitMargin);
    }

    @Test
    public void testPoolMarginQuery(TestContext context) throws IOException {
        this.testQuery(context, "poolMargin", persistenceProxy::queryPoolMargin);
    }

    @Test
    public void testPositionReportQuery(TestContext context) throws IOException {
        this.testQuery(context, "positionReport", persistenceProxy::queryPositionReport);
    }

    @Test
    public void testRiskLimitUtilizationQuery(TestContext context) throws IOException {
        this.testQuery(context, "riskLimitUtilization", persistenceProxy::queryRiskLimitUtilization);
    }

    @Test
    public void testQueryFailure(TestContext context) throws InterruptedException {
        storageManager.setHealth(false);
        testAppender.start();
        Async queryAsync = context.async();
        persistenceProxy.queryAccountMargin(RequestType.HISTORY, new JsonObject(),
                context.asyncAssertFailure(ar -> queryAsync.complete()));
        queryAsync.awaitSuccess();
        testAppender.waitForMessageContains(Level.ERROR, "INVALID_ARGUMENT");
        testAppender.stop();
        storageManager.setHealth(true);
    }

    private interface QueryFunction<T extends Model> {
        void query(RequestType type, JsonObject query, Handler<AsyncResult<List<T>>> resultHandler);
    }

    private <T extends Model>
    void testQuery(TestContext context,
                   String dataFolder,
                   QueryFunction<T> queryFunction) throws IOException {

        int firstMsgCount = DataHelper.getJsonObjectCount(dataFolder, 1);
        Async asyncQuery1 = context.async(firstMsgCount);
        DataHelper.readTTSaveFile(dataFolder, 1, (json) -> {
            queryFunction.query(RequestType.HISTORY, json, ar -> {
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
            queryFunction.query(RequestType.LATEST, json, ar -> {
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
        Future<Void> proxyClose = Future.future();
        Future<Void> storeClose = Future.future();

        persistenceProxy.close(proxyClose);
        storageManager.close(storeClose);

        CompositeFuture.all(proxyClose, storeClose).setHandler(context.asyncAssertSuccess(
                res -> vertx.close(context.asyncAssertSuccess())
        ));
    }
}
