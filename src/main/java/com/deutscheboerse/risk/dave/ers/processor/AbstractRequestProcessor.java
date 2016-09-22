package com.deutscheboerse.risk.dave.ers.processor;

import com.deutscheboerse.risk.dave.ers.jaxb.PartiesBlockT;
import com.deutscheboerse.risk.dave.ers.jaxb.PtysSubGrpBlockT;
import io.vertx.core.json.JsonObject;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class AbstractRequestProcessor {
    protected final DateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

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
}
