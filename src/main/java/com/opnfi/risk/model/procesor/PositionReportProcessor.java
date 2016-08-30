package com.opnfi.risk.model.procesor;

import com.opnfi.risk.model.PositionReport;
import com.opnfi.risk.model.jaxb.FIXML;
import io.vertx.core.json.Json;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class PositionReportProcessor implements Processor {
   @Override
   public void process(Exchange exchange) {
        Message in = exchange.getIn();
        in.setBody(Json.encodePrettily(PositionReport.parseFromFIXML((FIXML)in.getBody())));
    }
}
