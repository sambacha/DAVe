package com.opnfi.risk;

import com.opnfi.risk.model.procesor.*;
import io.vertx.camel.CamelBridge;
import io.vertx.camel.CamelBridgeOptions;
import io.vertx.camel.InboundMapping;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.util.UUID;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.url.URLSyntaxException;


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
        Future<Void> startCamelFuture = startCamel();
        startCamelFuture.compose(v -> {
            return startCamelBridge();
        }).compose(v -> {
            chainFuture.complete();
        }, chainFuture);

        chainFuture.setHandler(ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(chainFuture.cause());
            }
        });
    }

    public Future<Void> startCamel()
    {
        Future<Void> startCamelFuture = Future.future();
        camelCtx = new DefaultCamelContext();

        try {
            String connectionAddress = String.format("amqp://:@MyCamelApp/?brokerlist='%s:%d?tcp_nodelay='true'&ssl='true'&ssl_cert_alias='%s'&sasl_mechs='EXTERNAL'&trust_store='%s'&trust_store_password='%s'&key_store='%s'&key_store_password='%s'&ssl_verify_hostname='false''&sync_publish='all'",
                    config().getString("brokerHost", ERSConnectorVerticle.DEFAULT_BROKER_HOST),
                    config().getInteger("brokerPort", ERSConnectorVerticle.DEFAULT_BROKER_PORT),
                    config().getString("sslCertAlias", ERSConnectorVerticle.DEFAULT_SSL_CERT_ALIAS),
                    config().getString("truststore", ERSConnectorVerticle.DEFAULT_TRUSTSTORE),
                    config().getString("truststorePassword", ERSConnectorVerticle.DEFAULT_TRUSTSTORE_PASSWORD),
                    config().getString("keystore", ERSConnectorVerticle.DEFAULT_KEYSTORE),
                    config().getString("keystorePassword", ERSConnectorVerticle.DEFAULT_KEYSTORE_PASSWORD));
            AMQConnectionFactory amqpFact = new AMQConnectionFactory(connectionAddress);
            camelCtx.addComponent("amqp", new AMQPComponent(amqpFact));
        } catch (URLSyntaxException e) {
            LOG.error("Failed to create AMQP Connection Factory", e);
            startCamelFuture.failed();
        }

        try {
            camelCtx.addRoutes(new RouteBuilder() {
                @Override
                public void configure() {
                    final JaxbDataFormat ersDataModel = new JaxbDataFormat(true);
                    final UUID addressSuffix = UUID.randomUUID();
                    final String member = config().getString("member", ERSConnectorVerticle.DEFAULT_MEMBER);
                    ersDataModel.setContextPath("com.opnfi.risk.model.jaxb");

                    String tssBroadcastAddress = getBroadcastAddress("eurex.tmp." + member + ".opnfi_tss_" + addressSuffix,
                            "public.MessageType.TradingSessionStatus.#");
                    String mcBroadcastAddress = getBroadcastAddress("eurex.tmp." + member + ".opnfi_mc_" + addressSuffix,
                            member + ".MessageType.MarginComponents.#");
                    String tmrBroadcastAddress = getBroadcastAddress("eurex.tmp." + member + ".opnfi_tmr_" + addressSuffix,
                            member + ".MessageType.TotalMarginRequirement.#");
                    String mssBroadcastAddress = getBroadcastAddress("eurex.tmp." + member + ".opnfi_mss_" + addressSuffix,
                            member + ".MessageType.MarginShortfallSurplus.#");
                    String prBroadcastAddress = getBroadcastAddress("eurex.tmp." + member + ".opnfi_pr_" + addressSuffix,
                            member + ".MessageType.Position.#");
                    String rlBroadcastAddress = getBroadcastAddress("eurex.tmp." + member + ".opnfi_rl_" + addressSuffix,
                            member + ".MessageType.RiskLimits.#");

                    from("amqp:" + tssBroadcastAddress).unmarshal(ersDataModel).process(new TradingSessionStatusProcesor()).to("direct:tss");
                    from("amqp:" + mcBroadcastAddress).unmarshal(ersDataModel).process(new MarginComponentProcesor()).to("direct:mc");
                    from("amqp:" + tmrBroadcastAddress).unmarshal(ersDataModel).process(new TotalMarginRequirementProcessor()).to("direct:tmr");
                    from("amqp:" + mssBroadcastAddress).unmarshal(ersDataModel).process(new MarginShortfallSurplusProcesor()).to("direct:mss");
                    from("amqp:" + prBroadcastAddress).unmarshal(ersDataModel).process(new PositionReportProcessor()).to("direct:pr");
                    from("amqp:" + rlBroadcastAddress).unmarshal(ersDataModel).process(new RiskLimitProcessor()).to("direct:rl");
                }
            });
        }
        catch (Exception e)
        {
            LOG.error("Failed to add Camel routes", e);
            startCamelFuture.failed();
        }

        try {
            camelCtx.start();
            startCamelFuture.complete();
        }
        catch (Exception e)
        {
            LOG.error("Failed to start Camel", e);
            startCamelFuture.failed();
        }
        return startCamelFuture;
    }

    private String getBroadcastAddress(String name, String routingKey) {
        return String.format("%s; {create: receiver, assert: never, node: "
                + "{ type: queue, x-declare: { auto-delete: true, exclusive: false, arguments: "
                + "{ 'qpid.policy_type': ring, 'qpid.max_count': 1000, 'qpid.max_size': 1000000, "
                + "'qpid.auto_delete_timeout': 60 } }, x-bindings: [ { exchange: 'eurex.broadcast',"
                + "queue: '%s', key: '%s' } ] } }", name, name, routingKey);
    }

    public Future<Void> startCamelBridge()
    {
        camelBridge = CamelBridge.create(vertx,
                new CamelBridgeOptions(camelCtx)
                        .addInboundMapping(InboundMapping.fromCamel("direct:tss").toVertx("ers.TradingSessionStatus").usePublish())
                        .addInboundMapping(InboundMapping.fromCamel("direct:mc").toVertx("ers.MarginComponent").usePublish())
                        .addInboundMapping(InboundMapping.fromCamel("direct:tmr").toVertx("ers.TotalMarginRequirement").usePublish())
                        .addInboundMapping(InboundMapping.fromCamel("direct:mss").toVertx("ers.MarginShortfallSurplus").usePublish())
                        .addInboundMapping(InboundMapping.fromCamel("direct:pr").toVertx("ers.PositionReport").usePublish())
                        .addInboundMapping(InboundMapping.fromCamel("direct:rl").toVertx("ers.RiskLimit").usePublish())
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
