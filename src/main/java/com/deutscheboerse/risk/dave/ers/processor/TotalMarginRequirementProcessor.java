package com.deutscheboerse.risk.dave.ers.processor;

import java.util.Collections;

import com.deutscheboerse.risk.dave.ers.jaxb.AbstractMessageT;
import com.deutscheboerse.risk.dave.ers.jaxb.FIXML;
import com.deutscheboerse.risk.dave.ers.jaxb.MarginAmountBlockT;
import com.deutscheboerse.risk.dave.ers.jaxb.MarginRequirementReportMessageT;
import io.vertx.core.json.JsonObject;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class TotalMarginRequirementProcessor extends AbstractProcessor implements Processor {

    private JsonObject parseFromFIXML(FIXML fixml) {
        JAXBElement<? extends AbstractMessageT> msg = fixml.getMessage();
        MarginRequirementReportMessageT tmrMessage = (MarginRequirementReportMessageT) msg.getValue();

        JsonObject tmr = new JsonObject();
        tmr.put("received", new JsonObject().put("$date", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        tmr.put("reqId", tmrMessage.getID());
        tmr.put("sesId", tmrMessage.getSetSesID().toString());
        tmr.put("rptId", tmrMessage.getRptID());
        GregorianCalendar txnTmInFrankfurtZone = tmrMessage.getTxnTm().toGregorianCalendar();
        txnTmInFrankfurtZone.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        tmr.put("txnTm", new JsonObject().put("$date", txnTmInFrankfurtZone.toZonedDateTime().withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        tmr.put("bizDt", tmrMessage.getBizDt().toGregorianCalendar().toZonedDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE));

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
