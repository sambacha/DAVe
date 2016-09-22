package com.deutscheboerse.risk.dave.ers.processor;

import com.deutscheboerse.risk.dave.ers.jaxb.FIXML;
import com.deutscheboerse.risk.dave.ers.jaxb.ObjectFactory;
import com.deutscheboerse.risk.dave.ers.jaxb.TradingSessionStatusRequestMessageT;
import io.vertx.core.json.JsonObject;
import org.apache.camel.Processor;

public class TradingSessionStatusRequestProcessor extends AbstractRequestProcessor implements Processor {
    public TradingSessionStatusRequestProcessor(String replyToAddress)
    {
        super(replyToAddress);
    }

    protected FIXML createRequest(JsonObject request)
    {
        ObjectFactory of = new ObjectFactory();
        FIXML fixml = new FIXML();
        TradingSessionStatusRequestMessageT tssr = new TradingSessionStatusRequestMessageT();
        tssr.setReqID(getRequestId());
        tssr.setSubReqTyp("0");
        fixml.setMessage(of.createTrdgSesStatReq(tssr));

        return fixml;
    }
}
