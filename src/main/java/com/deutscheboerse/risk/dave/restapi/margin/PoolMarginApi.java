package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.PoolMarginModel;
import com.deutscheboerse.risk.dave.persistence.RequestType;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class PoolMarginApi extends AbstractApi {
    public PoolMarginApi(Vertx vertx) {
        super(vertx, new PoolMarginModel());
    }

    @Override
    protected String getRequestName() {
        return "pm";
    }

    @Override
    protected void proxyFind(RoutingContext routingContext, RequestType requestType) {
        persistenceProxy.queryPoolMargin(requestType, this.createParamsFromContext(routingContext), responseHandler(routingContext));
    }

}
