package com.deutscheboerse.risk.dave.ers.processor;

import com.deutscheboerse.risk.dave.ers.jaxb.*;
import io.vertx.core.json.JsonObject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import java.math.BigInteger;

public class MarginShortfallSurplusRequestProcessor extends AbstractProcessor implements Processor {
    private final String replyToAddress;

    public MarginShortfallSurplusRequestProcessor(String replyToAddress)
    {
        this.replyToAddress = replyToAddress;
    }

    private FIXML createRequest(JsonObject request)
    {
        ObjectFactory of = new ObjectFactory();
        FIXML fixml = new FIXML();
        MarginRequirementInquiryMessageT mssr = new MarginRequirementInquiryMessageT();
        mssr.setSetSesID(SettlSessIDEnumT.ITD);
        mssr.setID(getRequestId());
        mssr.getMgnReqmtInqQual().add(getQual());
        mssr.getPty().add(getClearer(request));
        mssr.getPty().add(getMember(request));

        fixml.setMessage(of.createMgnReqmtInq(mssr));

        return fixml;
    }

    private MarginReqmtInqQualGrpBlockT getQual()
    {
        MarginReqmtInqQualGrpBlockT qual = new MarginReqmtInqQualGrpBlockT();
        qual.setQual(BigInteger.valueOf(2));

        return qual;
    }

    private PartiesBlockT getClearer(JsonObject request)
    {
        PartiesBlockT clearer = new PartiesBlockT();
        clearer.setID(request.getString("clearer"));
        clearer.setR(BigInteger.valueOf(4));
        clearer.setSrc("D");

        return clearer;
    }

    private PartiesBlockT getMember(JsonObject request)
    {
        PartiesBlockT member = new PartiesBlockT();
        member.setID(request.getString("member"));
        member.setR(BigInteger.valueOf(1));
        member.setSrc("D");

        return member;
    }

   @Override
   public void process(Exchange exchange) {
       JsonObject request = (JsonObject)exchange.getIn().getBody();

       Message out = exchange.getOut();
       out.setBody(createRequest(request));
       out.setHeader("JMSReplyTo", replyToAddress);
    }
}
