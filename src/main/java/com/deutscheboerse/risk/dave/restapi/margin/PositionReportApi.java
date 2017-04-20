package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.PositionReportModel;
import com.deutscheboerse.risk.dave.persistence.RequestType;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class PositionReportApi extends AbstractApi {
    public PositionReportApi(Vertx vertx) {
        super(vertx, new PositionReportModel());
    }

    @Override
    protected String getRequestName() {
        return "pr";
    }

    @Override
    protected void proxyFind(RoutingContext routingContext, RequestType requestType) {
        persistenceProxy.queryPositionReport(requestType, this.createParamsFromContext(routingContext), responseHandler(routingContext));
    }

}
