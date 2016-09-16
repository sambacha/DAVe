package com.deutscheboerse.risk.dave.ers.processor;

import java.util.Collections;

import com.deutscheboerse.risk.dave.ers.jaxb.AbstractMessageT;
import com.deutscheboerse.risk.dave.ers.jaxb.FIXML;
import com.deutscheboerse.risk.dave.ers.jaxb.MarginAmountBlockT;
import com.deutscheboerse.risk.dave.ers.jaxb.MarginRequirementReportMessageT;
import io.vertx.core.json.JsonObject;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class TotalMarginRequirementProcessor extends AbstractProcessor implements Processor {

    private JsonObject parseFromFIXML(FIXML fixml) {
        JAXBElement<? extends AbstractMessageT> msg = fixml.getMessage();
        MarginRequirementReportMessageT tmrMessage = (MarginRequirementReportMessageT) msg.getValue();

        JsonObject tmr = new JsonObject();
        tmr.put("received", new JsonObject().put("$date", timestampFormatter.format(new Date())));
        tmr.put("reqId", tmrMessage.getID());
        tmr.put("sesId", tmrMessage.getSetSesID().toString());
        tmr.put("rptId", tmrMessage.getRptID());
        tmr.put("txnTm", new JsonObject().put("$date", timestampFormatter.format(tmrMessage.getTxnTm().toGregorianCalendar().getTime())));
        tmr.put("bizDt", new JsonObject().put("$date", timestampFormatter.format(tmrMessage.getBizDt().toGregorianCalendar().getTime())));

        processParties(tmrMessage.getPty(), tmr);

        List<MarginAmountBlockT> margins = tmrMessage.getMgnAmt();
        Set<String> typs = new HashSet<>();
        typs.add("2");
        typs.add("3");
        processMarginBlocks(margins, Collections.unmodifiableSet(typs), tmr);

        return tmr;
    }

   @Override
   public void process(Exchange exchange) {
        Message in = exchange.getIn();
        in.setBody(this.parseFromFIXML((FIXML)in.getBody()));
    }
}
