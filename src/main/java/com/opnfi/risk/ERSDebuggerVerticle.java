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
        EventBus eb = vertx.eventBus();

        eb.consumer("ers.TradingSessionStatus", message -> {
            //LOG.debug("Received TSS message: " + message);
            //LOG.info("TSS message body: " + message.body().getClass().getCanonicalName());
            //LOG.info("TSS message body: " + message.body().getClass().getSimpleName());
            LOG.info("Received TSS message with body: " + message.body().toString());
        });

        eb.consumer("ers.MarginShortfallSurplus", message -> {
            LOG.info("Received MSS message with body: " + message.body().toString());
        });

        eb.consumer("ers.MarginComponent", message -> {
            LOG.info("Received MC message with body: " + message.body().toString());
        });

        eb.consumer("ers.TotalMarginRequirement", message -> {
            LOG.info("Received TMR message with body: " + message.body().toString());
        });
    }
}
