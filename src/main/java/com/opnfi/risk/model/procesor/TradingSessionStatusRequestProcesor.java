package com.opnfi.risk.model.procesor;

import com.opnfi.risk.model.jaxb.*;
import io.vertx.core.json.JsonObject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import javax.xml.bind.JAXBElement;
import java.util.Date;
import java.util.UUID;

public class TradingSessionStatusRequestProcesor extends AbstractProcessor implements Processor {
    private final String replyToAddress;

    public TradingSessionStatusRequestProcesor(String replyToAddress)
    {
        this.replyToAddress = replyToAddress;
    }

    private FIXML createRequest()
    {
        ObjectFactory of = new ObjectFactory();
        FIXML request = new FIXML();
        TradingSessionStatusRequestMessageT tssr = new TradingSessionStatusRequestMessageT();
        tssr.setReqID(UUID.randomUUID().toString());
        tssr.setSubReqTyp("0");
        request.setMessage(of.createTrdgSesStatReq(tssr));

        return request;
    }

   @Override
   public void process(Exchange exchange) {
       Message out = exchange.getOut();
       out.setBody(createRequest());
       out.setHeader("JMSReplyTo", replyToAddress);
    }
}
