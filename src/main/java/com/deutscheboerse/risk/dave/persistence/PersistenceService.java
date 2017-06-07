package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.model.*;
import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;

@ProxyGen
public interface PersistenceService {
    String SERVICE_ADDRESS = "persistenceService";

    int INIT_ERROR = 2;
    int QUERY_ERROR = 3;

    void initialize(Handler<AsyncResult<Void>> resultHandler);

    void queryAccountMargin(RequestType type, JsonObject query, Handler<AsyncResult<List<AccountMarginModel>>> resultHandler);
    void queryLiquiGroupMargin(RequestType type, JsonObject query, Handler<AsyncResult<List<LiquiGroupMarginModel>>> resultHandler);
    void queryLiquiGroupSplitMargin(RequestType type, JsonObject query, Handler<AsyncResult<List<LiquiGroupSplitMarginModel>>> resultHandler);
    void queryPoolMargin(RequestType type, JsonObject query, Handler<AsyncResult<List<PoolMarginModel>>> resultHandler);
    void queryPositionReport(RequestType type, JsonObject query, Handler<AsyncResult<List<PositionReportModel>>> resultHandler);
    void queryRiskLimitUtilization(RequestType type, JsonObject query, Handler<AsyncResult<List<RiskLimitUtilizationModel>>> resultHandler);

    @ProxyClose
    void close(Handler<AsyncResult<Void>> resultHandler);
}
