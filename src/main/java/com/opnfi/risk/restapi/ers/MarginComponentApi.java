package com.opnfi.risk.restapi.ers;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by schojak on 29.8.16.
 */
public class MarginComponentApi extends AbstractErsApi {
    
    public MarginComponentApi(EventBus eb) {
        super(eb, "query.latestMarginComponent", "query.historyMarginComponent", "mc");
    }

    @Override
    protected JsonObject createParamsFromContext(RoutingContext routingContext) {
        final JsonObject params = new JsonObject();

        if (routingContext.request().getParam("clearer") != null && !"*".equals(routingContext.request().getParam("clearer"))) {
            params.put("clearer", routingContext.request().getParam("clearer"));
        }

        if (routingContext.request().getParam("member") != null && !"*".equals(routingContext.request().getParam("member"))) {
            params.put("member", routingContext.request().getParam("member"));
        }

        if (routingContext.request().getParam("account") != null && !"*".equals(routingContext.request().getParam("account"))) {
            params.put("account", routingContext.request().getParam("account"));
        }

        if (routingContext.request().getParam("clss") != null && !"*".equals(routingContext.request().getParam("clss"))) {
            params.put("clss", routingContext.request().getParam("clss"));
        }

        if (routingContext.request().getParam("ccy") != null && !"*".equals(routingContext.request().getParam("ccy"))) {
            params.put("ccy", routingContext.request().getParam("ccy"));
        }
        return params;
    }
}
