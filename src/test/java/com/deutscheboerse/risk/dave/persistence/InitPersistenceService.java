package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.model.AbstractModel;
import com.deutscheboerse.risk.dave.model.PositionReportModel;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;

public class InitPersistenceService implements PersistenceService {
    private final boolean succeeds;
    private boolean initialized = false;

    public InitPersistenceService(boolean succeeds) {
        this.succeeds = succeeds;
    }

    @Override
    public void initialize(Handler<AsyncResult<Void>> resultHandler) {
        if (this.succeeds) {
            this.initialized = true;
            resultHandler.handle(Future.succeededFuture());
        } else {
            this.initialized = false;
            resultHandler.handle(ServiceException.fail(INIT_ERROR, "Init failed"));
        }
    }

    @Override
    public void queryMarginComponent(AbstractModel.CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(ServiceException.fail(QUERY_ERROR, "queryMarginComponent not implemented"));
    }

    @Override
    public void queryMarginShortfallSurplus(AbstractModel.CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(ServiceException.fail(QUERY_ERROR, "queryMarginShortfallSurplus not implemented"));
    }

    @Override
    public void queryPositionReport(AbstractModel.CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(ServiceException.fail(QUERY_ERROR, "queryPositionReport not implemented"));
    }

    @Override
    public void queryRiskLimit(AbstractModel.CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(ServiceException.fail(QUERY_ERROR, "queryRiskLimit not implemented"));
    }

    @Override
    public void queryTotalMarginRequirement(AbstractModel.CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(ServiceException.fail(QUERY_ERROR, "queryTotalMarginRequirement not implemented"));
    }

    @Override
    public void queryTradingSessionStatus(AbstractModel.CollectionType type, JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(ServiceException.fail(QUERY_ERROR, "queryTradingSessionStatus not implemented"));
    }

    @Override
    public void close() {
    }

    public boolean isInitialized() {
        return this.initialized;
    }
}
