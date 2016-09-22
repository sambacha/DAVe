package com.deutscheboerse.risk.dave.ers.processor;

import com.deutscheboerse.risk.dave.ers.jaxb.FIXML;
import io.vertx.core.json.JsonObject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class TotalMarginRequirementRequestProcessor extends AbstractMarginRequirementInquiryRequestProcessor implements Processor {
    private final String replyToAddress;

    public TotalMarginRequirementRequestProcessor(String replyToAddress)
    {
        this.replyToAddress = replyToAddress;
    }

    private FIXML createRequest(JsonObject request)
    {
        return createMarginRequirementInquiry(request, 0);
    }

   @Override
   public void process(Exchange exchange) {
       JsonObject request = (JsonObject)exchange.getIn().getBody();

       Message out = exchange.getOut();
       out.setBody(createRequest(request));
       out.setHeader("JMSReplyTo", replyToAddress);
    }
}
