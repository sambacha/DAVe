package com.deutscheboerse.risk.dave.ers;

import com.deutscheboerse.risk.dave.ERSConnectorVerticle;
import com.deutscheboerse.risk.dave.ers.processor.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;

import java.util.UUID;

/**
 * Created by schojak on 16.9.16.
 */
public class ERSRouteBuilder extends RouteBuilder {
    final UUID addressSuffix = UUID.randomUUID();
    final String member;

    public ERSRouteBuilder(String member) {
        this.member = member;
    }

    @Override
    public void configure() {
        final JaxbDataFormat ersDataModel = new JaxbDataFormat(true);
        ersDataModel.setContextPath("com.deutscheboerse.risk.dave.ers.jaxb");

        String tssBroadcastAddress = getBroadcastAddress("tss", "public.MessageType.TradingSessionStatus.#");
        String mcBroadcastAddress = getBroadcastAddress("mc", member + ".MessageType.MarginComponents.#");
        String tmrBroadcastAddress = getBroadcastAddress("tmr", member + ".MessageType.TotalMarginRequirement.#");
        String mssBroadcastAddress = getBroadcastAddress("mss", member + ".MessageType.MarginShortfallSurplus.#");
        String prBroadcastAddress = getBroadcastAddress("pr", member + ".MessageType.Position.#");
        String rlBroadcastAddress = getBroadcastAddress("rl", member + ".MessageType.RiskLimits.#");

        String tssResponseAddress = getResponseAddress("tss", member + ".TradingSessionStatus");
        String tssReplyAddress = getReplyAddress(member + ".TradingSessionStatus");
        String tssRequestAddress = getRequestAddress();

        from("amqp:" + tssBroadcastAddress).unmarshal(ersDataModel).process(new TradingSessionStatusProcessor()).to("direct:tss");
        from("amqp:" + mcBroadcastAddress).unmarshal(ersDataModel).process(new MarginComponentProcessor()).to("direct:mc");
        from("amqp:" + tmrBroadcastAddress).unmarshal(ersDataModel).process(new TotalMarginRequirementProcessor()).to("direct:tmr");
        from("amqp:" + mssBroadcastAddress).unmarshal(ersDataModel).process(new MarginShortfallSurplusProcessor()).to("direct:mss");
        from("amqp:" + prBroadcastAddress).unmarshal(ersDataModel).process(new PositionReportProcessor()).to("direct:pr");
        from("amqp:" + rlBroadcastAddress).unmarshal(ersDataModel).process(new RiskLimitProcessor()).split(body()).to("direct:rl");

        from("amqp:" + tssResponseAddress).unmarshal(ersDataModel).process(new TradingSessionStatusProcessor()).to("direct:tssResponse");
        from("direct:tssRequest").process(new TradingSessionStatusRequestProcessor(tssReplyAddress)).marshal(ersDataModel).to("amqp:" + tssRequestAddress + "?preserveMessageQos=true");
    }

    private String getBroadcastAddress(String type, String routingKey) {
        String queueName = String.format("eurex.tmp.%s.dave_%s_%s", member, type, addressSuffix);

        return String.format("%s; {create: receiver, assert: never, node: "
                + "{ type: queue, x-declare: { auto-delete: true, exclusive: false, arguments: "
                + "{ 'qpid.policy_type': ring, 'qpid.max_count': 1000, 'qpid.max_size': 1000000, "
                + "'qpid.auto_delete_timeout': 60 } }, x-bindings: [ { exchange: 'eurex.broadcast',"
                + "queue: '%s', key: '%s' } ] } }", queueName, queueName, routingKey);
    }

    private String getResponseAddress(String type, String routingKey) {
        String queueName = String.format("eurex.tmp.%s.dave_resp_%s_%s", member, type, addressSuffix);

        return String.format("%s; {create: receiver, assert: never, node: "
                + "{ type: queue, x-declare: { auto-delete: true, exclusive: false, arguments: "
                + "{ 'qpid.policy_type': ring, 'qpid.max_count': 1000, 'qpid.max_size': 1000000, "
                + "'qpid.auto_delete_timeout': 60 } }, x-bindings: [ { exchange: 'eurex.response',"
                + "queue: '%s', key: '%s' } ] } }", queueName, queueName, routingKey);
    }

    private String getReplyAddress(String routingKey)
    {
        return String.format("eurex.response/%s; { node: { type: topic }, assert: never, create: never}", routingKey);
    }

    private String getRequestAddress()
    {
        return String.format("eurex.%s/%s.ERS; { node: { type: topic }, assert: never, create: never}", member, member);
    }
}
