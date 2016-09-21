package com.deutscheboerse.risk.dave.ers.processor;

import com.deutscheboerse.risk.dave.ers.jaxb.*;
import io.vertx.core.json.JsonObject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import java.math.BigInteger;

public class RiskLimitRequestProcessor extends AbstractProcessor implements Processor {
    private final String replyToAddress;

    public RiskLimitRequestProcessor(String replyToAddress)
    {
        this.replyToAddress = replyToAddress;
    }

    private FIXML createRequest(JsonObject request)
    {
        ObjectFactory of = new ObjectFactory();
        FIXML fixml = new FIXML();
        PartyRiskLimitsRequestMessageT rlr = new PartyRiskLimitsRequestMessageT();
        rlr.setReqID(getRequestId());
        rlr.setSubReqTyp("3");

        rlr.getPty().add(getClearer(request));
        rlr.getPty().add(getMember(request));
        rlr.getPty().add(getMaintainer(request));

        fixml.setMessage(of.createPtyRiskLmtReq(rlr));

        return fixml;
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

    private PartiesBlockT getMaintainer(JsonObject request)
    {
        PartiesBlockT member = new PartiesBlockT();
        member.setID(request.getString("maintainer"));
        member.setR(BigInteger.valueOf(7));
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
