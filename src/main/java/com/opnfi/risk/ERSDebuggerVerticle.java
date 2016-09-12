package com.opnfi.risk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by schojak on 19.8.16.
 */
public class ERSDebuggerVerticle extends AbstractVerticle {
    final static private Logger LOG = LoggerFactory.getLogger(ERSDebuggerVerticle.class);

    @Override
    public void start(Future<Void> fut) {
        LOG.info("Subscribing to ERS messages for debugging");
        EventBus eb = vertx.eventBus();

        eb.consumer("ers.TradingSessionStatus", message -> {
            LOG.trace("Received TSS message with body: " + message.body().toString());
        });

        eb.consumer("ers.MarginShortfallSurplus", message -> {
            LOG.trace("Received MSS message with body: " + message.body().toString());
        });

        eb.consumer("ers.MarginComponent", message -> {
            LOG.trace("Received MC message with body: " + message.body().toString());
        });

        eb.consumer("ers.TotalMarginRequirement", message -> {
            LOG.trace("Received TMR message with body: " + message.body().toString());
        });

        eb.consumer("ers.PositionReport", message -> {
            LOG.trace("Received PR message with body: " + message.body().toString());
        });

        eb.consumer("ers.RiskLimit", message -> {
            LOG.trace("Received RiskLimit message with body: " + message.body().toString());
        });

        fut.complete();
    }
}
