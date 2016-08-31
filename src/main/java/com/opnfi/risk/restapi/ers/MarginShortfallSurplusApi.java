package com.opnfi.risk.restapi.ers;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by schojak on 29.8.16.
 */
public class MarginShortfallSurplusApi extends AbstractErsApi {
    
    public MarginShortfallSurplusApi(EventBus eb) {
        super(eb, "query.latestMarginShortfallSurplus", "query.historyMarginShortfallSurplus", "mss");
    }

    @Override
    protected JsonObject createParamsFromContext(RoutingContext routingContext) {
        final JsonObject params = new JsonObject();

        if (routingContext.request().getParam("clearer") != null && !"*".equals(routingContext.request().getParam("clearer"))) {
            params.put("clearer", routingContext.request().getParam("clearer"));
        }

        if (routingContext.request().getParam("pool") != null && !"*".equals(routingContext.request().getParam("pool"))) {
            params.put("pool", routingContext.request().getParam("pool"));
        }

        if (routingContext.request().getParam("member") != null && !"*".equals(routingContext.request().getParam("member"))) {
            params.put("member", routingContext.request().getParam("member"));
        }

        if (routingContext.request().getParam("clearingCcy") != null && !"*".equals(routingContext.request().getParam("clearingCcy"))) {
            params.put("clearingCcy", routingContext.request().getParam("clearingCcy"));
        }

        if (routingContext.request().getParam("ccy") != null && !"*".equals(routingContext.request().getParam("ccy"))) {
            params.put("ccy", routingContext.request().getParam("ccy"));
        }
        return params;
    }
}
