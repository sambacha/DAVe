package com.deutscheboerse.risk.dave;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by schojak on 19.8.16.
 */
public class ERSDebuggerVerticle extends AbstractVerticle {
    final static private Logger LOG = LoggerFactory.getLogger(ERSDebuggerVerticle.class);

    private final List<MessageConsumer<?>> eventBusConsumers = new ArrayList<>();

    @Override
    public void start(Future<Void> fut) {
        LOG.info("Subscribing to ERS messages for debugging");

        this.registerConsumer("ers.TradingSessionStatus", message -> {
            LOG.trace("Received TSS message with body: " + message.body().toString());
        });

        this.registerConsumer("ers.MarginShortfallSurplus", message -> {
            LOG.trace("Received MSS message with body: " + message.body().toString());
        });

        this.registerConsumer("ers.MarginComponent", message -> {
            LOG.trace("Received MC message with body: " + message.body().toString());
        });

        this.registerConsumer("ers.TotalMarginRequirement", message -> {
            LOG.trace("Received TMR message with body: " + message.body().toString());
        });

        this.registerConsumer("ers.PositionReport", message -> {
            LOG.trace("Received PR message with body: " + message.body().toString());
        });

        this.registerConsumer("ers.RiskLimit", message -> {
            LOG.trace("Received RiskLimit message with body: " + message.body().toString());
        });

        fut.complete();
    }

    private <T> void registerConsumer(String address, Handler<Message<T>> handler) {
        EventBus eb = vertx.eventBus();
        this.eventBusConsumers.add(eb.consumer(address, handler));
    }

    @Override
    public void stop() throws Exception {
        this.eventBusConsumers.forEach(consumer -> consumer.unregister());
    }

}
