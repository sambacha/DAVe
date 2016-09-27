package com.deutscheboerse.risk.dave.ers;

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
        final JaxbDataFormat ersDataModel = getDataModel();

        configureBroadcasts(ersDataModel);
        configureRequests(ersDataModel);
        configureResponses(ersDataModel);
    }

    private JaxbDataFormat getDataModel()
    {
        final JaxbDataFormat ersDataModel = new JaxbDataFormat(true);
        ersDataModel.setContextPath("com.deutscheboerse.risk.dave.ers.jaxb");

        return ersDataModel;
    }

    private void configureBroadcasts(JaxbDataFormat dataModel)
    {
        String tssBroadcastAddress = getBroadcastAddress("tss", "public.MessageType.TradingSessionStatus.#");
        String mcBroadcastAddress = getBroadcastAddress("mc", member + ".MessageType.MarginComponents.#");
        String tmrBroadcastAddress = getBroadcastAddress("tmr", member + ".MessageType.TotalMarginRequirement.#");
        String mssBroadcastAddress = getBroadcastAddress("mss", member + ".MessageType.MarginShortfallSurplus.#");
        String prBroadcastAddress = getBroadcastAddress("pr", member + ".MessageType.Position.#");
        String rlBroadcastAddress = getBroadcastAddress("rl", member + ".MessageType.RiskLimits.#");

        from("amqp:" + tssBroadcastAddress).unmarshal(dataModel).process(new TradingSessionStatusProcessor()).to("direct:tss");
        from("amqp:" + mcBroadcastAddress).unmarshal(dataModel).process(new MarginComponentProcessor()).to("direct:mc");
        from("amqp:" + tmrBroadcastAddress).unmarshal(dataModel).process(new TotalMarginRequirementProcessor()).to("direct:tmr");
        from("amqp:" + mssBroadcastAddress).unmarshal(dataModel).process(new MarginShortfallSurplusProcessor()).to("direct:mss");
        from("amqp:" + prBroadcastAddress).unmarshal(dataModel).process(new PositionReportProcessor()).to("direct:pr");
        from("amqp:" + rlBroadcastAddress).unmarshal(dataModel).process(new RiskLimitProcessor()).split(body()).to("direct:rl");
    }

    private void configureRequests(JaxbDataFormat dataModel)
    {
        String requestAddress = getRequestAddress();
        String tssReplyAddress = getReplyAddress(member + ".TradingSessionStatus");
        String prReplyAddress = getReplyAddress(member + ".PositionReport");
        String tmrReplyAddress = getReplyAddress(member + ".TotalMarginRequirement");
        String mssReplyAddress = getReplyAddress(member + ".MarginShortfallSurplus");
        String rlReplyAddress = getReplyAddress(member + ".RiskLimits");

        from("direct:tssRequest").process(new TradingSessionStatusRequestProcessor(tssReplyAddress)).marshal(dataModel).to("amqp:" + requestAddress + "?preserveMessageQos=true");
        from("direct:prRequest").process(new PositionReportRequestProcessor(prReplyAddress)).marshal(dataModel).to("amqp:" + requestAddress + "?preserveMessageQos=true");
        from("direct:tmrRequest").process(new TotalMarginRequirementRequestProcessor(tmrReplyAddress)).marshal(dataModel).to("amqp:" + requestAddress + "?preserveMessageQos=true");
        from("direct:mssRequest").process(new MarginShortfallSurplusRequestProcessor(mssReplyAddress)).marshal(dataModel).to("amqp:" + requestAddress + "?preserveMessageQos=true");
        from("direct:rlRequest").process(new RiskLimitRequestProcessor(rlReplyAddress)).marshal(dataModel).to("amqp:" + requestAddress + "?preserveMessageQos=true");
    }

    private void configureResponses(JaxbDataFormat dataModel)
    {
        String tssResponseAddress = getResponseAddress("tss", member + ".TradingSessionStatus");
        String prResponseAddress = getResponseAddress("pr", member + ".PositionReport");
        String tmrResponseAddress = getResponseAddress("tmr", member + ".TotalMarginRequirement");
        String mssResponseAddress = getResponseAddress("mss", member + ".MarginShortfallSurplus");
        String rlResponseAddress = getResponseAddress("rl", member + ".RiskLimits");

        from("amqp:" + tssResponseAddress).unmarshal(dataModel).process(new TradingSessionStatusProcessor()).to("direct:tssResponse");
        from("amqp:" + prResponseAddress).unmarshal(dataModel).process(new PositionReportProcessor()).to("direct:prResponse");
        from("amqp:" + tmrResponseAddress).unmarshal(dataModel).process(new TotalMarginRequirementProcessor()).to("direct:tmrResponse");
        from("amqp:" + mssResponseAddress).unmarshal(dataModel).process(new MarginShortfallSurplusProcessor()).to("direct:mssResponse");
        from("amqp:" + rlResponseAddress).unmarshal(dataModel).process(new RiskLimitProcessor()).split(body()).to("direct:rlResponse");
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
