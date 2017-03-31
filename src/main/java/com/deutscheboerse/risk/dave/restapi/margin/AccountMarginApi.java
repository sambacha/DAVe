package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.AccountMarginModel;
import com.deutscheboerse.risk.dave.persistence.RequestType;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class AccountMarginApi extends AbstractApi {
    public AccountMarginApi(Vertx vertx) {
        super(vertx, new AccountMarginModel());
    }

    @Override
    protected String getRequestName() {
        return "am";
    }

    @Override
    protected void proxyFind(RoutingContext routingContext, RequestType requestType) {
        persistenceProxy.findAccountMargin(requestType, this.createParamsFromContext(routingContext), responseHandler(routingContext));
    }
}
