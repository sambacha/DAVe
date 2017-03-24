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

    void findAccountMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler);
    void findLiquiGroupMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler);
    void findLiquiGroupSplitMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler);
    void findPoolMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler);
    void findPositionReport(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler);
    void findRiskLimitUtilization(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler);

    @ProxyClose
    void close();
}
