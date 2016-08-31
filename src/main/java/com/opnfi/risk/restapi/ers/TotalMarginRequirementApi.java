package com.opnfi.risk.restapi.ers;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by schojak on 29.8.16.
 */
public class TotalMarginRequirementApi {
    private static final Logger LOG = LoggerFactory.getLogger(TotalMarginRequirementApi.class);
    private final EventBus eb;

    public TotalMarginRequirementApi(EventBus eb) {
        this.eb = eb;
    }

    private JsonObject createParamsFromContext(RoutingContext routingContext) {
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

        if (routingContext.request().getParam("account") != null && !"*".equals(routingContext.request().getParam("account"))) {
            params.put("account", routingContext.request().getParam("account"));
        }

        if (routingContext.request().getParam("ccy") != null && !"*".equals(routingContext.request().getParam("ccy"))) {
            params.put("ccy", routingContext.request().getParam("ccy"));
        }
        return params;
    }

    public void latestTotalMarginRequirement(RoutingContext routingContext) {
        LOG.trace("Received latest/tmr request");

        eb.send("query.latestTotalMarginRequirement", this.createParamsFromContext(routingContext), ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response latest/tmr request");

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end((String)ar.result().body());
            } else {
                LOG.error("Failed to query the DB service", ar.cause());
            }
        });
    }

    public void historyTotalMarginRequirement(RoutingContext routingContext) {
        LOG.trace("Received history/tmr request");

        eb.send("query.historyTotalMarginRequirement", this.createParamsFromContext(routingContext), ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response history/tmr request");

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end((String)ar.result().body());
            } else {
                LOG.error("Failed to query the DB service", ar.cause());
            }
        });
    }
}
