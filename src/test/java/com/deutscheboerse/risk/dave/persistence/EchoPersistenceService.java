package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.model.*;
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
    public void findAccountMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse(AccountMarginModel.class.getSimpleName(), type.name(), query)));
    }

    @Override
    public void findLiquiGroupMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse(LiquiGroupMarginModel.class.getSimpleName(), type.name(), query)));
    }

    @Override
    public void findLiquiGroupSplitMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse(LiquiGroupSplitMarginModel.class.getSimpleName(), type.name(), query)));
    }

    @Override
    public void findPoolMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse(PoolMarginModel.class.getSimpleName(), type.name(), query)));
    }

    @Override
    public void findPositionReport(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse(PositionReportModel.class.getSimpleName(), type.name(), query)));
    }

    @Override
    public void findRiskLimitUtilization(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse(RiskLimitUtilizationModel.class.getSimpleName(), type.name(), query)));
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


    private String echoResponse(String model, String requestType, JsonObject query) {
        return Json.encodePrettily(new JsonArray().add(
                new JsonObject()
                        .put("model", model)
                        .put("requestType", requestType)
                        .mergeIn(query)));
    }
}
