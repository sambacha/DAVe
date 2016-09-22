package com.deutscheboerse.risk.dave.ers.processor;

import com.deutscheboerse.risk.dave.ers.jaxb.FIXML;
import com.deutscheboerse.risk.dave.ers.jaxb.ObjectFactory;
import com.deutscheboerse.risk.dave.ers.jaxb.TradingSessionStatusRequestMessageT;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class TradingSessionStatusRequestProcessor extends AbstractRequestProcessor implements Processor {
    private final String replyToAddress;

    public TradingSessionStatusRequestProcessor(String replyToAddress)
    {
        this.replyToAddress = replyToAddress;
    }

    private FIXML createRequest()
    {
        ObjectFactory of = new ObjectFactory();
        FIXML fixml = new FIXML();
        TradingSessionStatusRequestMessageT tssr = new TradingSessionStatusRequestMessageT();
        tssr.setReqID(getRequestId());
        tssr.setSubReqTyp("0");
        fixml.setMessage(of.createTrdgSesStatReq(tssr));

        return fixml;
    }

   @Override
   public void process(Exchange exchange) {
       Message out = exchange.getOut();
       out.setBody(createRequest());
       out.setHeader("JMSReplyTo", replyToAddress);
    }
}
