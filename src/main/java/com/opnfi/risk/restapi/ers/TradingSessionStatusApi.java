package com.opnfi.risk.restapi.ers;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by schojak on 29.8.16.
 */
public class TradingSessionStatusApi {
    private static final Logger LOG = LoggerFactory.getLogger(TradingSessionStatusApi.class);
    private final EventBus eb;

    public TradingSessionStatusApi(EventBus eb) {
        this.eb = eb;
    }

    public void latestTradingSessionStatus(RoutingContext routingContext) {
        LOG.trace("Received latest/tss request");

        eb.send("query.latestTradingSessionStatus", null, ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response to latest/tss request");

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end((String)ar.result().body());
            } else {
                LOG.error("Failed to query the DB service", ar.cause());
            }
        });
    }

    public void historyTradingSessionStatus(RoutingContext routingContext) {
        LOG.trace("Received history/tss request");

        eb.send("query.historyTradingSessionStatus", null, ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response to history/tss request");

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end((String)ar.result().body());
            } else {
                LOG.error("Failed to query the DB service", ar.cause());
            }
        });
    }
}
