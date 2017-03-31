package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.LiquiGroupMarginModel;
import com.deutscheboerse.risk.dave.persistence.RequestType;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class LiquiGroupMarginApi extends AbstractApi {
    public LiquiGroupMarginApi(Vertx vertx) {
        super(vertx, new LiquiGroupMarginModel());
    }

    @Override
    protected String getRequestName() {
        return "lgm";
    }

    @Override
    protected void proxyFind(RoutingContext routingContext, RequestType requestType) {
        persistenceProxy.findLiquiGroupMargin(requestType, this.createParamsFromContext(routingContext), responseHandler(routingContext));
    }

}
