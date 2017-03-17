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
    int STORE_ERROR = 3;
    int QUERY_ERROR = 4;

    void initialize(Handler<AsyncResult<Void>> resultHandler);

    void find(String collection, JsonObject query, Handler<AsyncResult<String>> resultHandler);
    void insert(String collection, JsonObject document, Handler<AsyncResult<String>> resultHandler);
    void upsert(String collection, JsonObject query, JsonObject document, Handler<AsyncResult<String>> resultHandler);

    @ProxyClose
    void close();
}
