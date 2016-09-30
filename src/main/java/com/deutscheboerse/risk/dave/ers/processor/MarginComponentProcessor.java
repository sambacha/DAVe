package com.deutscheboerse.risk.dave.ers.processor;

import java.util.Collections;

import com.deutscheboerse.risk.dave.ers.jaxb.MarginAmountBlockT;
import com.deutscheboerse.risk.dave.ers.jaxb.MarginRequirementReportMessageT;
import com.deutscheboerse.risk.dave.ers.jaxb.AbstractMessageT;
import com.deutscheboerse.risk.dave.ers.jaxb.FIXML;
import io.vertx.core.json.JsonObject;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class MarginComponentProcessor extends AbstractProcessor implements Processor {

    private JsonObject parseFromFIXML(FIXML fixml) {
        JAXBElement<? extends AbstractMessageT> msg = fixml.getMessage();
        MarginRequirementReportMessageT mcMessage = (MarginRequirementReportMessageT) msg.getValue();

        JsonObject mc = new JsonObject();
        mc.put("received", new JsonObject().put("$date", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        mc.put("reqId", mcMessage.getID());
        mc.put("sesId", mcMessage.getSetSesID().toString());
        mc.put("rptId", mcMessage.getRptID());
        ZonedDateTime txnTmInFrankfurtZone = ZonedDateTime.ofInstant(mcMessage.getTxnTm().toGregorianCalendar().toInstant(), ZoneId.of("Europe/Paris"));
        mc.put("txnTm", new JsonObject().put("$date", txnTmInFrankfurtZone.withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        mc.put("bizDt", mcMessage.getBizDt().toGregorianCalendar().toZonedDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
        mc.put("clss", mcMessage.getClss());

        processParties(mcMessage.getPty(), mc);

        List<MarginAmountBlockT> margins = mcMessage.getMgnAmt();
        Set<String> typs = new HashSet<>();
        typs.add("1");
        typs.add("10");
        typs.add("12");
        typs.add("17");
        typs.add("23");
        processMarginBlocks(margins, Collections.unmodifiableSet(typs), mc);

        return mc;
    }

    @Override
    public void process(Exchange exchange) {
        Message in = exchange.getIn();
        in.setBody(this.parseFromFIXML((FIXML)in.getBody()));
    }
}
