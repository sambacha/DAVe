package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.model.AbstractModel.CollectionType;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import static com.deutscheboerse.risk.dave.model.AbstractModel.CollectionType.LATEST;

public class EchoPersistenceService implements PersistenceService {

    @Override
    public void initialize(Handler<AsyncResult<Void>> resultHandler) {
        resultHandler.handle(Future.succeededFuture());
    }

    @Override
    public void queryMarginComponent(CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse("MarginComponent", type, params)));
    }

    @Override
    public void queryMarginShortfallSurplus(CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse("MarginShortfallSurplus", type, params)));
    }

    @Override
    public void queryPositionReport(CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse("PositionReport", type, params)));
    }

    @Override
    public void queryRiskLimit(CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse("RiskLimit", type, params)));
    }

    @Override
    public void queryTotalMarginRequirement(CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse("TotalMarginRequirement", type, params)));
    }

    @Override
    public void queryTradingSessionStatus(CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse("TradingSessionStatus", type, params)));
    }

    @Override
    public void close() {

    }

    private String echoResponse(String methodName, CollectionType type, JsonObject params) {
        return Json.encodePrettily(new JsonArray().add(
                new JsonObject().put("method", (type == LATEST ? "latest" : "history") + methodName).mergeIn(params)));
    }
}
