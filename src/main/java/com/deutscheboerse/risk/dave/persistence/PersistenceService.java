package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.model.AbstractModel.CollectionType;
import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@ProxyGen
public interface PersistenceService {
    String SERVICE_ADDRESS = "persistenceService";

    int INIT_ERROR = 2;
    int STORE_ERROR = 3;
    int QUERY_ERROR = 4;

    void initialize(Handler<AsyncResult<Void>> resultHandler);

    void queryMarginComponent(CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler);
    void queryMarginShortfallSurplus(CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler);
    void queryPositionReport(CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler);
    void queryRiskLimit(CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler);
    void queryTotalMarginRequirement(CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler);
    void queryTradingSessionStatus(CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler);

    @ProxyClose
    void close();
}
