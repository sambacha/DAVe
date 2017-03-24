package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.RiskLimitUtilizationModel;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import com.deutscheboerse.risk.dave.persistence.RequestType;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class RiskLimitUtilizationApi extends AbstractApi {
    public RiskLimitUtilizationApi(Vertx vertx) {
        super(vertx, new RiskLimitUtilizationModel());
    }

    @Override
    protected String getRequestName() {
        return "rlu";
    }

    @Override
    protected void proxyFind(RoutingContext routingContext, RequestType requestType) {
        persistenceProxy.findRiskLimitUtilization(requestType, this.createParamsFromContext(routingContext), responseHandler(routingContext));

    }

}
