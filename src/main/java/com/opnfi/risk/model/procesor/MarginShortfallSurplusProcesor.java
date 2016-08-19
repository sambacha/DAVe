package com.opnfi.risk.model.procesor;

import com.opnfi.risk.model.MarginComponent;
import com.opnfi.risk.model.MarginShortfallSurplus;
import com.opnfi.risk.model.jaxb.FIXML;
import io.vertx.core.json.Json;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

/**
 * Created by schojak on 19.8.16.
 */
public class MarginShortfallSurplusProcesor implements Processor {
   public void process(Exchange exchange) {
        Message in = exchange.getIn();
        in.setBody(Json.encodePrettily(MarginShortfallSurplus.parseFromFIXML((FIXML)in.getBody())));
    }
}
