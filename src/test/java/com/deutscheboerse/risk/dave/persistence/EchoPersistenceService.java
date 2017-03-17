package com.deutscheboerse.risk.dave.persistence;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class EchoPersistenceService implements PersistenceService {

    @Override
    public void initialize(Handler<AsyncResult<Void>> resultHandler) {
        resultHandler.handle(Future.succeededFuture());
    }

    @Override
    public void find(String collection, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse(collection, query)));
    }

    @Override
    public void insert(String collection, JsonObject document, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse(collection, document)));
    }

    @Override
    public void upsert(String collection, JsonObject query, JsonObject document, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse(collection, document)));
    }

    @Override
    public void close() {

    }

    private String echoResponse(String collection, JsonObject query) {
        return Json.encodePrettily(new JsonArray().add(
                new JsonObject().put("collection", collection).mergeIn(query)));
    }
}
