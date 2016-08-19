package com.opnfi.risk;

import com.opnfi.risk.model.procesor.TradingSessionStatusProcesor;
import io.vertx.camel.CamelBridge;
import io.vertx.camel.CamelBridgeOptions;
import io.vertx.camel.InboundMapping;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.url.URLSyntaxException;

/**
 * Created by schojak on 19.8.16.
 */
public class MainVerticle extends AbstractVerticle {
    final static private Logger LOG = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> fut) {
        vertx.deployVerticle("com.opnfi.risk.ERSConnectorVerticle", res -> {
            if (res.succeeded()) {
                LOG.info("Deployed ERSConnectorVerticle");
            } else {
                LOG.info("Deployment of ERSConnectorVerticle failed!");
            }
        });

        vertx.deployVerticle("com.opnfi.risk.ERSDebuggerVerticle", res -> {
            if (res.succeeded()) {
                LOG.info("Deployed ERSDebuggerVerticle");
            } else {
                LOG.info("Deployment of ERSDebuggerVerticle failed!");
            }
        });
    }

    @Override
    public void stop() throws Exception {
        vertx.undeploy("com.opnfi.risk.ERSConnectorVerticle", res -> {
            if (res.succeeded()) {
                LOG.info("Undeployed ERSConnectorVerticle");
            } else {
                LOG.info("Undeploy of ERSConnectorVerticle failed!");
            }
        });

        vertx.undeploy("com.opnfi.risk.ERSDebuggerVerticle", res -> {
            if (res.succeeded()) {
                LOG.info("Undeployed ERSDebuggerVerticle");
            } else {
                LOG.info("Undeploy of ERSDebuggerVerticle failed!");
            }
        });
    }
}