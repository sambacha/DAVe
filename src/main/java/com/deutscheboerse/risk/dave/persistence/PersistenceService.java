package com.deutscheboerse.risk.dave.persistence;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@ProxyGen
public interface PersistenceService {
    String SERVICE_ADDRESS = "persistenceService";

    int INIT_ERROR = 2;
    int QUERY_ERROR = 3;

    void initialize(Handler<AsyncResult<Void>> resultHandler);

    void queryAccountMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler);
    void queryLiquiGroupMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler);
    void queryLiquiGroupSplitMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler);
    void queryPoolMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler);
    void queryPositionReport(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler);
    void queryRiskLimitUtilization(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler);

    @ProxyClose
    void close();
}
