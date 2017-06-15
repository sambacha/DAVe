package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import com.deutscheboerse.risk.dave.model.*;
import com.deutscheboerse.risk.dave.utils.ModelBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class EchoPersistenceService implements PersistenceService {

    private final HealthCheck healthCheck;

    @Inject
    public EchoPersistenceService(Vertx vertx) {
        this.healthCheck = new HealthCheck(vertx);
    }

    @Override
    public void initialize(Handler<AsyncResult<Void>> resultHandler) {
        healthCheck.setComponentReady(HealthCheck.Component.PERSISTENCE_SERVICE);
        resultHandler.handle(Future.succeededFuture());
    }

    @Override
    public void queryAccountMargin(RequestType type, JsonObject query, Handler<AsyncResult<List<AccountMarginModel>>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse(type, query, ModelBuilder::buildAccountMarginFromJson)));
    }

    @Override
    public void queryLiquiGroupMargin(RequestType type, JsonObject query, Handler<AsyncResult<List<LiquiGroupMarginModel>>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse(type, query, ModelBuilder::buildLiquiGroupMarginFromJson)));
    }

    @Override
    public void queryLiquiGroupSplitMargin(RequestType type, JsonObject query, Handler<AsyncResult<List<LiquiGroupSplitMarginModel>>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse(type, query, ModelBuilder::buildLiquiGroupSplitMarginFromJson)));
    }

    @Override
    public void queryPoolMargin(RequestType type, JsonObject query, Handler<AsyncResult<List<PoolMarginModel>>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse(type, query, ModelBuilder::buildPoolMarginFromJson)));
    }

    @Override
    public void queryPositionReport(RequestType type, JsonObject query, Handler<AsyncResult<List<PositionReportModel>>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse(type, query, ModelBuilder::buildPositionReportFromJson)));
    }

    @Override
    public void queryRiskLimitUtilization(RequestType type, JsonObject query, Handler<AsyncResult<List<RiskLimitUtilizationModel>>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(echoResponse(type, query, ModelBuilder::buildRiskLimitUtilizationFromJson)));
    }

    @Override
    public void close(Handler<AsyncResult<Void>> resultHandler) {
        resultHandler.handle(Future.succeededFuture());
    }

    private <T extends Model> List<T>
    echoResponse(RequestType requestType, JsonObject query,
                 Function<JsonObject, T> modelFactory) {
        List<T> response = new ArrayList<>();
        response.add(modelFactory.apply(new JsonObject().mergeIn(query)
                .put("snapshotID", requestType == RequestType.LATEST
                        ? ModelBuilder.LATEST_SNAPSHOT_ID
                        : ModelBuilder.HISTORY_SNAPSHOT_ID)
                .put("businessDate", ModelBuilder.BUSINESS_DATE)));
        return response;
    }
}
