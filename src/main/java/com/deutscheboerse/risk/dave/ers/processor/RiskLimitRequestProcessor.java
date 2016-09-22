package com.deutscheboerse.risk.dave.ers.processor;

import com.deutscheboerse.risk.dave.ers.jaxb.FIXML;
import com.deutscheboerse.risk.dave.ers.jaxb.ObjectFactory;
import com.deutscheboerse.risk.dave.ers.jaxb.PartiesBlockT;
import com.deutscheboerse.risk.dave.ers.jaxb.PartyRiskLimitsRequestMessageT;
import io.vertx.core.json.JsonObject;
import org.apache.camel.Processor;

import java.math.BigInteger;

public class RiskLimitRequestProcessor extends AbstractRequestProcessor implements Processor {
    public RiskLimitRequestProcessor(String replyToAddress)
    {
        super(replyToAddress);
    }

    protected FIXML createRequest(JsonObject request)
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

    private PartiesBlockT getMaintainer(JsonObject request)
    {
        PartiesBlockT member = new PartiesBlockT();
        member.setID(request.getString("maintainer"));
        member.setR(BigInteger.valueOf(7));
        member.setSrc("D");

        return member;
    }
}
