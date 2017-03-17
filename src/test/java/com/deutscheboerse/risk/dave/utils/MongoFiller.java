package com.deutscheboerse.risk.dave.utils;

import com.deutscheboerse.risk.dave.model.*;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import java.util.Optional;
import java.util.function.Function;

public class MongoFiller {
    private final TestContext context;
    private final PersistenceService proxy;

    private AbstractModel lastModel;

    public MongoFiller(TestContext context, PersistenceService proxy) {
        this.context = context;
        this.proxy = proxy;
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
        proxy.insert(model.getHistoryCollection(), model.getMongoDocument(), res ->
             proxy.upsert(model.getLatestCollection(), model.getQueryParams(), model.getMongoDocument(), handler));

        lastModel = model;
    }
}
