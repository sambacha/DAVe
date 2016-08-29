package com.opnfi.risk.restapi.ers;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by schojak on 29.8.16.
 */
public class MarginComponentApi {
    private static final Logger LOG = LoggerFactory.getLogger(MarginComponentApi.class);
    private final EventBus eb;

    public MarginComponentApi(EventBus eb) {
        this.eb = eb;
    }

    public void latestMarginComponent(RoutingContext routingContext) {
        LOG.trace("Received latest/mc request");

        JsonObject params = new JsonObject();

        if (routingContext.request().getParam("clearer") != null && !"*".equals(routingContext.request().getParam("clearer")))
        {
            params.put("clearer", routingContext.request().getParam("clearer"));
        }

        if (routingContext.request().getParam("member") != null && !"*".equals(routingContext.request().getParam("member")))
        {
            params.put("member", routingContext.request().getParam("member"));
        }

        if (routingContext.request().getParam("account") != null && !"*".equals(routingContext.request().getParam("account")))
        {
            params.put("account", routingContext.request().getParam("account"));
        }

        if (routingContext.request().getParam("clss") != null && !"*".equals(routingContext.request().getParam("clss")))
        {
            params.put("clss", routingContext.request().getParam("clss"));
        }

        if (routingContext.request().getParam("ccy") != null && !"*".equals(routingContext.request().getParam("ccy")))
        {
            params.put("ccy", routingContext.request().getParam("ccy"));
        }

        eb.send("query.latestMarginComponent", params, ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response latest/mc request");

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end((String)ar.result().body());
            }
            else
            {
                LOG.error("Failed to query the DB service", ar.cause());
            }
        });
    }

    public void historyMarginComponent(RoutingContext routingContext) {
        LOG.trace("Received history/mc request");

        JsonObject params = new JsonObject();

        if (routingContext.request().getParam("clearer") != null && !"*".equals(routingContext.request().getParam("clearer")))
        {
            params.put("clearer", routingContext.request().getParam("clearer"));
        }

        if (routingContext.request().getParam("member") != null && !"*".equals(routingContext.request().getParam("member")))
        {
            params.put("member", routingContext.request().getParam("member"));
        }

        if (routingContext.request().getParam("account") != null && !"*".equals(routingContext.request().getParam("account")))
        {
            params.put("account", routingContext.request().getParam("account"));
        }

        if (routingContext.request().getParam("clss") != null && !"*".equals(routingContext.request().getParam("clss")))
        {
            params.put("clss", routingContext.request().getParam("clss"));
        }

        if (routingContext.request().getParam("ccy") != null && !"*".equals(routingContext.request().getParam("ccy")))
        {
            params.put("ccy", routingContext.request().getParam("ccy"));
        }

        eb.send("query.historyMarginComponent", params, ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response history/mc request");

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end((String)ar.result().body());
            }
            else
            {
                LOG.error("Failed to query the DB service", ar.cause());
            }
        });
    }
}
