package com.opnfi.risk.restapi.ers;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

public abstract class AbstractErsApi {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractErsApi.class);

    private final EventBus eb;
    private final String latestEbAddress;
    private final String historyEbAddress;
    private final String requestName;

    public AbstractErsApi(EventBus eb, String latestAddress, String historyAddress, String requestName) {
        this.eb = eb;
        this.latestEbAddress = latestAddress;
        this.historyEbAddress = historyAddress;
        this.requestName = requestName;
    }

    protected abstract JsonObject createParamsFromContext(RoutingContext routingContext);

    protected void restCall(RoutingContext routingContext, String ebAddress, String requestName) {
        LOG.trace("Received {} request", requestName);

        eb.send(ebAddress, this.createParamsFromContext(routingContext), ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response {} request", requestName);
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end((String)ar.result().body());
            } else {
                LOG.error("Failed to query the DB service", ar.cause());
            }
        });
    }

    public void latestCall(RoutingContext routingContext) {
        restCall(routingContext, latestEbAddress, "latest/" + requestName);
    }

    public void historyCall(RoutingContext routingContext) {
        restCall(routingContext, historyEbAddress, "history/" + requestName);
    }
}
