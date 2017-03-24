package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.LiquiGroupSplitMarginModel;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import com.deutscheboerse.risk.dave.persistence.RequestType;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class LiquiGroupSplitMarginApi extends AbstractApi {
    public LiquiGroupSplitMarginApi(Vertx vertx) {
        super(vertx, new LiquiGroupSplitMarginModel());
    }

    @Override
    protected String getRequestName() {
        return "lgsm";
    }

    @Override
    protected void proxyFind(RoutingContext routingContext, RequestType requestType) {
        persistenceProxy.findLiquiGroupSplitMargin(requestType, this.createParamsFromContext(routingContext), responseHandler(routingContext));

    }

}
