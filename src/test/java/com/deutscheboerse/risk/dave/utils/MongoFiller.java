package com.deutscheboerse.risk.dave.utils;

import com.deutscheboerse.risk.dave.model.*;
import com.deutscheboerse.risk.dave.persistence.MongoPersistenceService;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class MongoFiller {
    private final TestContext context;
    private final MongoClient mongoClient;

    private AbstractModel lastModel;
    private static final Map<Class<? extends AbstractModel>, String> latestCollectionsForModel = new HashMap<>();
    private static final Map<Class<? extends AbstractModel>, String> historyCollectionsForModel = new HashMap<>();

    static {
        latestCollectionsForModel.put(AccountMarginModel.class, MongoPersistenceService.ACCOUNT_MARGIN_LATEST_COLLECTION);
        latestCollectionsForModel.put(LiquiGroupMarginModel.class, MongoPersistenceService.LIQUI_GROUP_MARGIN_LATEST_COLLECTION);
        latestCollectionsForModel.put(LiquiGroupSplitMarginModel.class, MongoPersistenceService.LIQUI_GROUP_SPLIT_MARGIN_LATEST_COLLECTION);
        latestCollectionsForModel.put(PoolMarginModel.class, MongoPersistenceService.POOL_MARGIN_LATEST_COLLECTION);
        latestCollectionsForModel.put(PositionReportModel.class, MongoPersistenceService.POSITION_REPORT_LATEST_COLLECTION);
        latestCollectionsForModel.put(RiskLimitUtilizationModel.class, MongoPersistenceService.RISK_LIMIT_UTILIZATION_LATEST_COLLECTION);

        historyCollectionsForModel.put(AccountMarginModel.class, MongoPersistenceService.ACCOUNT_MARGIN_HISTORY_COLLECTION);
        historyCollectionsForModel.put(LiquiGroupMarginModel.class, MongoPersistenceService.LIQUI_GROUP_MARGIN_HISTORY_COLLECTION);
        historyCollectionsForModel.put(LiquiGroupSplitMarginModel.class, MongoPersistenceService.LIQUI_GROUP_SPLIT_MARGIN_HISTORY_COLLECTION);
        historyCollectionsForModel.put(PoolMarginModel.class, MongoPersistenceService.POOL_MARGIN_HISTORY_COLLECTION);
        historyCollectionsForModel.put(PositionReportModel.class, MongoPersistenceService.POSITION_REPORT_HISTORY_COLLECTION);
        historyCollectionsForModel.put(RiskLimitUtilizationModel.class, MongoPersistenceService.RISK_LIMIT_UTILIZATION_HISTORY_COLLECTION);
    }

    public MongoFiller(TestContext context, MongoClient mongoClient) {
        this.context = context;
        this.mongoClient = mongoClient;
    }

    public int feedAccountMarginCollection(int ttsaveNo, int timeoutMillis) {
        return doFeed("accountMargin", ttsaveNo, timeoutMillis, AccountMarginModel::new);
    }

    public int feedLiquiGroupMarginCollection(int ttsaveNo, int timeoutMillis) {
        return doFeed("liquiGroupMargin", ttsaveNo, timeoutMillis, LiquiGroupMarginModel::new);
    }

    public int feedLiquiGroupSplitMarginCollection(int ttsaveNo, int timeoutMillis) {
        return doFeed("liquiGroupSplitMargin", ttsaveNo, timeoutMillis, LiquiGroupSplitMarginModel::new);
    }

    public int feedPoolMarginCollection(int ttsaveNo, int timeoutMillis) {
        return doFeed("poolMargin", ttsaveNo, timeoutMillis, PoolMarginModel::new);
    }

    public int feedPositionReportCollection(int ttsaveNo, int timeoutMillis) {
        return doFeed("positionReport", ttsaveNo, timeoutMillis, PositionReportModel::new);
    }

    public int feedRiskLimitUtilizationCollection(int ttsaveNo, int timeoutMillis) {
        return doFeed("riskLimitUtilization", ttsaveNo, timeoutMillis, RiskLimitUtilizationModel::new);
    }

    public Optional<AbstractModel> getLastModel() {
        return Optional.ofNullable(lastModel);
    }

    private int doFeed(String folder, int ttsaveNo, long timeoutMillis, Function<JsonObject, AbstractModel> mapper) {
        int msgCount = DataHelper.getJsonObjectCount(folder, ttsaveNo);
        Async asyncStore = context.async(msgCount);

        DataHelper.readTTSaveFile(folder, ttsaveNo).forEach(json ->
                this.insertModel(mapper.apply(json), ar -> {
                    if (ar.succeeded()) {
                        asyncStore.countDown();
                    } else {
                        context.fail(ar.cause());
                    }
                })
        );

        asyncStore.awaitSuccess(timeoutMillis);

        return msgCount;
    }

    private void insertModel(AbstractModel model, Handler<AsyncResult<String>> handler) {
        Future<String> insertFuture = Future.future();
        Future<MongoClientUpdateResult> upsertFuture = Future.future();
        mongoClient.insert(MongoFiller.getHistoryCollectionForModel(model), DataHelper.getMongoDocument(model), insertFuture);
        UpdateOptions updateOptions = new UpdateOptions().setUpsert(true);
        mongoClient.replaceDocumentsWithOptions(MongoFiller.getLatestCollectionForModel(model), DataHelper.getQueryParams(model), DataHelper.getMongoDocument(model), updateOptions, upsertFuture);
        CompositeFuture.all(insertFuture, upsertFuture).setHandler(res -> {
            if (res.succeeded()) {
                lastModel = model;
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    public static String getLatestCollectionForModel(AbstractModel model) {
        return MongoFiller.latestCollectionsForModel.get(model.getClass());
    }

    public static String getHistoryCollectionForModel(AbstractModel model) {
        return MongoFiller.historyCollectionsForModel.get(model.getClass());
    }

}
