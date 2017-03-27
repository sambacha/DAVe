package com.deutscheboerse.risk.dave.utils;

import com.deutscheboerse.risk.dave.model.*;
import com.deutscheboerse.risk.dave.persistence.MongoPersistenceService;
import io.vertx.core.AsyncResult;
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
    private static final Map<Class<? extends AbstractModel>, String> collectionsForModel = new HashMap<>();

    static {
        collectionsForModel.put(AccountMarginModel.class, MongoPersistenceService.ACCOUNT_MARGIN_COLLECTION);
        collectionsForModel.put(LiquiGroupMarginModel.class, MongoPersistenceService.LIQUI_GROUP_MARGIN_COLLECTION);
        collectionsForModel.put(LiquiGroupSplitMarginModel.class, MongoPersistenceService.LIQUI_GROUP_SPLIT_MARGIN_COLLECTION);
        collectionsForModel.put(PoolMarginModel.class, MongoPersistenceService.POOL_MARGIN_COLLECTION);
        collectionsForModel.put(PositionReportModel.class, MongoPersistenceService.POSITION_REPORT_COLLECTION);
        collectionsForModel.put(RiskLimitUtilizationModel.class, MongoPersistenceService.RISK_LIMIT_UTILIZATION_COLLECTION);
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

        DataHelper.readTTSaveFile(folder, ttsaveNo).forEach(json -> {
            AbstractModel model = mapper.apply(json);
            this.insertModel(model, ar -> {
                if (ar.succeeded()) {
                    lastModel = model;
                    asyncStore.countDown();
                } else {
                    context.fail(ar.cause());
                }
            });
        });

        asyncStore.awaitSuccess(timeoutMillis);

        return msgCount;
    }

    private void insertModel(AbstractModel model, Handler<AsyncResult<MongoClientUpdateResult>> handler) {
        mongoClient.updateCollectionWithOptions(
                MongoFiller.getCollectionForModel(model),
                DataHelper.getQueryParams(model),
                DataHelper.getStoreDocument(model),
                new UpdateOptions().setUpsert(true),
                handler);
    }

    private static String getCollectionForModel(AbstractModel model) {
        return MongoFiller.collectionsForModel.get(model.getClass());
    }
}
