package com.opnfi.risk;

import io.vertx.camel.CamelBridge;
import io.vertx.camel.CamelBridgeOptions;
import io.vertx.camel.InboundMapping;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.camel.model.dataformat.XmlJsonDataFormat;
import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.url.URLSyntaxException;


/**
 * Created by schojak on 17.8.16.
 */
public class ERSConnectorVerticle extends AbstractVerticle {
    final static private Logger LOG = LoggerFactory.getLogger(ERSConnectorVerticle.class);

    CamelContext camelCtx;
    CamelBridge camelBridge;

    @Override
    public void start(Future<Void> fut) {
        camelCtx = new DefaultCamelContext();

        try {
            AMQConnectionFactory amqpFact = new AMQConnectionFactory("amqp://:@MyCamelApp/?brokerlist='tcp://rgd003.xeop.de:15160?tcp_nodelay='true'&ssl='true'&ssl_cert_alias='cbkfr'&sasl_mechs='EXTERNAL'&trust_store='/home/schojak/amqp/idea-projects/Risk-Vertx-Camel/src/main/resources/truststore'&trust_store_password='123456'&key_store='/home/schojak/amqp/idea-projects/Risk-Vertx-Camel/src/main/resources/cbkfr.keystore'&key_store_password='123456'&ssl_verify_hostname='false''&sync_publish='all'");
            camelCtx.addComponent("amqp", new AMQPComponent(amqpFact));
        }
        catch (URLSyntaxException e)
        {
            LOG.error("Failed to create AMQP Connection Factory", e);
            fut.fail(e);
        }

        try {
            camelCtx.addRoutes(new RouteBuilder() {
                @Override
                public void configure() {
                    final JaxbDataFormat ersDataModel = new JaxbDataFormat(true);
                    ersDataModel.setContextPath("com.opnfi.risk.model.jaxb");

                    String tssBroadcastAddress = "eurex.tmp.CBKFR.broadcast_tss; { create: receiver, assert: never, node: { type: queue, x-declare: { auto-delete: true, exclusive: false, arguments: { 'qpid.policy_type': ring, 'qpid.max_count': 1000, 'qpid.max_size': 1000000, 'qpid.auto_delete_timeout': 60 } }, x-bindings: [ { exchange: 'eurex.broadcast', queue: 'eurex.tmp.CBKFR.broadcast_tss', key: 'public.MessageType.TradingSessionStatus.#' } ] } }";
                    String mcBroadcastAddress = "eurex.tmp.CBKFR.broadcast_mc; { create: receiver, assert: never, node: { type: queue, x-declare: { auto-delete: true, exclusive: false, arguments: { 'qpid.policy_type': ring, 'qpid.max_count': 1000, 'qpid.max_size': 1000000, 'qpid.auto_delete_timeout': 60 } }, x-bindings: [ { exchange: 'eurex.broadcast', queue: 'eurex.tmp.CBKFR.broadcast_mc', key: 'CBKFR.MessageType.MarginComponents.#' } ] } }";
                    String tmrBroadcastAddress = "eurex.tmp.CBKFR.broadcast_tmr; { create: receiver, assert: never, node: { type: queue, x-declare: { auto-delete: true, exclusive: false, arguments: { 'qpid.policy_type': ring, 'qpid.max_count': 1000, 'qpid.max_size': 1000000, 'qpid.auto_delete_timeout': 60 } }, x-bindings: [ { exchange: 'eurex.broadcast', queue: 'eurex.tmp.CBKFR.broadcast_tmr', key: 'CBKFR.MessageType.TotalMarginRequirement.#' } ] } }";
                    String mssBroadcastAddress = "eurex.tmp.CBKFR.broadcast_mss; { create: receiver, assert: never, node: { type: queue, x-declare: { auto-delete: true, exclusive: false, arguments: { 'qpid.policy_type': ring, 'qpid.max_count': 1000, 'qpid.max_size': 1000000, 'qpid.auto_delete_timeout': 60 } }, x-bindings: [ { exchange: 'eurex.broadcast', queue: 'eurex.tmp.CBKFR.broadcast_mss', key: 'CBKFR.MessageType.MarginShortfallSurplus.#' } ] } }";

                    from("amqp:" + tssBroadcastAddress).unmarshal(ersDataModel).to("direct:tss");
                    from("amqp:" + mcBroadcastAddress).unmarshal(ersDataModel).to("direct:mc");
                    from("amqp:" + tmrBroadcastAddress).unmarshal(ersDataModel).to("direct:tmr");
                    from("amqp:" + mssBroadcastAddress).unmarshal(ersDataModel).to("direct:mss");
                }
            });
        }
        catch (Exception e)
        {
            LOG.error("Failed to add Camel routes", e);
            fut.fail(e);
        }

        camelBridge = CamelBridge.create(vertx,
                new CamelBridgeOptions(camelCtx)
                        .addInboundMapping(InboundMapping.fromCamel("direct:tss").toVertx("ers-tss").usePublish().withBodyType(String.class))
                        .addInboundMapping(InboundMapping.fromCamel("direct:mc").toVertx("ers-mc").usePublish().withBodyType(String.class))
                        .addInboundMapping(InboundMapping.fromCamel("direct:tmr").toVertx("ers-tmr").usePublish().withBodyType(String.class))
                        .addInboundMapping(InboundMapping.fromCamel("direct:mss").toVertx("ers-mss").usePublish().withBodyType(String.class))
        );

        try {
            camelCtx.start();
        }
        catch (Exception e)
        {
            LOG.error("Failed to start Camel", e);
            fut.fail(e);
        }

        camelBridge.start();

        // Create event bus consumer
        EventBus eb = vertx.eventBus();
        eb.consumer("ers-tss", message -> {
            LOG.info("Received TSS message: " + message);
            LOG.info("TSS message body: " + message.body().getClass().getCanonicalName());
            LOG.info("TSS message body: " + message.body().getClass().getSimpleName());
            LOG.info("TSS message body: " + message.body().toString());
        });
    }

    @Override
    public void stop() throws Exception {
        camelBridge.stop();
        camelCtx.stop();
    }
}
