package com.deutscheboerse.risk.dave.ers.processor;

import com.deutscheboerse.risk.dave.ers.jaxb.TradingSessionStatusMessageT;
import com.deutscheboerse.risk.dave.ers.jaxb.AbstractMessageT;
import com.deutscheboerse.risk.dave.ers.jaxb.FIXML;
import io.vertx.core.json.JsonObject;
import java.util.Date;
import javax.xml.bind.JAXBElement;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class TradingSessionStatusProcessor extends AbstractProcessor implements Processor {

    private JsonObject parseFromFIXML(FIXML fixml) {
        JAXBElement<? extends AbstractMessageT> msg = fixml.getMessage();
        TradingSessionStatusMessageT tssMessage = (TradingSessionStatusMessageT) msg.getValue();

        JsonObject tss = new JsonObject();
        tss.put("received", new JsonObject().put("$date", timestampFormatter.format(new Date())));
        tss.put("reqId", tssMessage.getReqID());
        tss.put("sesId", tssMessage.getSesID());
        tss.put("stat", tssMessage.getStat());
        tss.put("statRejRsn", tssMessage.getStatRejRsn());
        tss.put("txt", tssMessage.getTxt());

        return tss;
    }

   @Override
   public void process(Exchange exchange) {
        Message in = exchange.getIn();
        in.setBody(this.parseFromFIXML((FIXML)in.getBody()));
    }
}
