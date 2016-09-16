package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.ers.ERSRouteBuilder;
import com.deutscheboerse.risk.dave.ers.processor.*;
import io.vertx.camel.CamelBridge;
import io.vertx.camel.CamelBridgeOptions;
import io.vertx.camel.InboundMapping;
import io.vertx.camel.OutboundMapping;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.url.URLSyntaxException;

import java.util.UUID;


/**
 * Created by schojak on 17.8.16.
 */
public class ERSConnectorVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(ERSConnectorVerticle.class);
    private static final String DEFAULT_BROKER_HOST = "localhost";
    private static final Integer DEFAULT_BROKER_PORT = 5672;
    private static final String DEFAULT_SSL_CERT_ALIAS = "alias";
    private static final String DEFAULT_TRUSTSTORE = "truststore";
    private static final String DEFAULT_TRUSTSTORE_PASSWORD = "123456";
    private static final String DEFAULT_KEYSTORE = "keystore";
    private static final String DEFAULT_KEYSTORE_PASSWORD = "123456";
    private static final String DEFAULT_MEMBER = "ABCFR";

    private CamelContext camelCtx;
    private CamelBridge camelBridge;

    @Override
    public void start(Future<Void> startFuture) {
        LOG.info("Starting {} with configuration: {}", ERSConnectorVerticle.class.getSimpleName(), config().encodePrettily());
        Future<Void> chainFuture = Future.future();
        startCamel()
                .compose(this::startCamelBridge)
                .compose(this::requestInitialData)
                .compose(chainFuture::complete, chainFuture);

        chainFuture.setHandler(ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(chainFuture.cause());
            }
        });
    }

    public Future<Void> requestInitialData(Void unused)
    {
        LOG.info("Requesting inital ERS data");

        LOG.trace("Requesting initial TradingSessionStatus");
        vertx.eventBus().publish("ers.TradingSessionStatusRequest", new JsonObject());

        return Future.succeededFuture();
    }

    public Future<Void> startCamel()
    {
        Future<Void> startCamelFuture = Future.future();
        camelCtx = new DefaultCamelContext();
        try {
            camelCtx.addComponent("amqp", new AMQPComponent(this.createAMQConnectionFactory()));
            camelCtx.addRoutes(this.createRouteBuilder());
            camelCtx.start();
            startCamelFuture.complete();
        } catch (URLSyntaxException e) {
            LOG.error("Failed to create AMQP Connection Factory", e);
            startCamelFuture.failed();
        } catch (Exception e) {
            LOG.error("Failed to start Camel", e);
            startCamelFuture.failed();
        }
        return startCamelFuture;
    }

    private AMQConnectionFactory createAMQConnectionFactory() throws URLSyntaxException {
        String connectionAddress = String.format("amqp://:@MyCamelApp/?brokerlist='%s:%d?tcp_nodelay='true'&ssl='true'&ssl_cert_alias='%s'&sasl_mechs='EXTERNAL'&trust_store='%s'&trust_store_password='%s'&key_store='%s'&key_store_password='%s'&ssl_verify_hostname='false''&sync_publish='all'",
                config().getString("brokerHost", ERSConnectorVerticle.DEFAULT_BROKER_HOST),
                config().getInteger("brokerPort", ERSConnectorVerticle.DEFAULT_BROKER_PORT),
                config().getString("sslCertAlias", ERSConnectorVerticle.DEFAULT_SSL_CERT_ALIAS),
                config().getString("truststore", ERSConnectorVerticle.DEFAULT_TRUSTSTORE),
                config().getString("truststorePassword", ERSConnectorVerticle.DEFAULT_TRUSTSTORE_PASSWORD),
                config().getString("keystore", ERSConnectorVerticle.DEFAULT_KEYSTORE),
                config().getString("keystorePassword", ERSConnectorVerticle.DEFAULT_KEYSTORE_PASSWORD));
        AMQConnectionFactory amqpFact = new AMQConnectionFactory(connectionAddress);
        return amqpFact;
    }

    private RouteBuilder createRouteBuilder() {
        return new ERSRouteBuilder(config().getString("member", ERSConnectorVerticle.DEFAULT_MEMBER));
    }

    public Future<Void> startCamelBridge(Void unused)
    {
        camelBridge = CamelBridge.create(vertx,
                new CamelBridgeOptions(camelCtx)
                        .addInboundMapping(InboundMapping.fromCamel("direct:tss").toVertx("ers.TradingSessionStatus").usePublish())
                        .addInboundMapping(InboundMapping.fromCamel("direct:mc").toVertx("ers.MarginComponent").usePublish())
                        .addInboundMapping(InboundMapping.fromCamel("direct:tmr").toVertx("ers.TotalMarginRequirement").usePublish())
                        .addInboundMapping(InboundMapping.fromCamel("direct:mss").toVertx("ers.MarginShortfallSurplus").usePublish())
                        .addInboundMapping(InboundMapping.fromCamel("direct:pr").toVertx("ers.PositionReport").usePublish())
                        .addInboundMapping(InboundMapping.fromCamel("direct:rl").toVertx("ers.RiskLimit").usePublish())
                        .addInboundMapping(InboundMapping.fromCamel("direct:tssResponse").toVertx("ers.TradingSessionStatus").usePublish())
                        .addOutboundMapping(OutboundMapping.fromVertx("ers.TradingSessionStatusRequest").toCamel("direct:tssRequest"))
        );

        camelBridge.start();
        return Future.succeededFuture();
    }

    @Override
    public void stop() throws Exception {
        camelBridge.stop();
        camelCtx.stop();
    }
}
