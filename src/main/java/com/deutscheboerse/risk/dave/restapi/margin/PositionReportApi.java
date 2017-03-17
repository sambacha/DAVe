package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.PositionReportModel;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import io.vertx.core.Vertx;

public class PositionReportApi extends AbstractApi {
    public PositionReportApi(Vertx vertx, PersistenceService persistenceProxy) {
        super(vertx, persistenceProxy, new PositionReportModel(), "pr");
    }
}
