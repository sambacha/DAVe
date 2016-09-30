package com.deutscheboerse.risk.dave.ers.processor;

import com.deutscheboerse.risk.dave.ers.jaxb.FIXML;
import com.deutscheboerse.risk.dave.ers.jaxb.PartiesBlockT;
import com.deutscheboerse.risk.dave.ers.jaxb.PtysSubGrpBlockT;
import io.vertx.core.json.JsonObject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;

import java.math.BigInteger;
import java.util.UUID;

abstract class AbstractRequestProcessor {
    protected final String replyToAddress;

    public AbstractRequestProcessor(String replyToAddress)
    {
        this.replyToAddress = replyToAddress;
    }

    protected String getRequestId()
    {
        return UUID.randomUUID().toString();
    }

    protected PartiesBlockT getClearer(JsonObject request)
    {
        PartiesBlockT clearer = new PartiesBlockT();
        clearer.setID(request.getString("clearer"));
        clearer.setR(BigInteger.valueOf(4));
        clearer.setSrc("D");

        if (request.getString("pool") != null) {
            clearer.getSub().add(getPool(request));
        }

        return clearer;
    }

    protected PartiesBlockT getMember(JsonObject request)
    {
        PartiesBlockT member = new PartiesBlockT();
        member.setID(request.getString("member"));
        member.setR(BigInteger.valueOf(1));
        member.setSrc("D");

        if (request.getString("account") != null) {
            member.getSub().add(getAccount(request));
        }

        return member;
    }

    protected PtysSubGrpBlockT getAccount(JsonObject request)
    {
        PtysSubGrpBlockT account = new PtysSubGrpBlockT();
        account.setTyp("26");
        account.setID(request.getString("account"));

        return account;
    }

    protected PtysSubGrpBlockT getPool(JsonObject request)
    {
        PtysSubGrpBlockT account = new PtysSubGrpBlockT();
        account.setTyp("4000");
        account.setID(request.getString("poolId", ""));

        return account;
    }

    abstract FIXML createRequest(JsonObject request);

    public void process(Exchange exchange) {
        JsonObject request = (JsonObject)exchange.getIn().getBody();

        Message out = exchange.getOut();
        out.setBody(createRequest(request));
        out.setHeader("JMSReplyTo", replyToAddress);
    }
}
