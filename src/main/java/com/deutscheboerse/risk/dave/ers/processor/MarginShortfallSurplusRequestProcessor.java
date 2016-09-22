package com.deutscheboerse.risk.dave.ers.processor;

import com.deutscheboerse.risk.dave.ers.jaxb.FIXML;
import io.vertx.core.json.JsonObject;
import org.apache.camel.Processor;

public class MarginShortfallSurplusRequestProcessor extends AbstractMarginRequirementInquiryRequestProcessor implements Processor {
    public MarginShortfallSurplusRequestProcessor(String replyToAddress)
    {
        super(replyToAddress);
    }

    protected FIXML createRequest(JsonObject request)
    {
        return createMarginRequirementInquiry(request, 2);
    }
}
