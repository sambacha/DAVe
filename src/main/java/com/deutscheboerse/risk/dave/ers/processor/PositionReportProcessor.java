package com.deutscheboerse.risk.dave.ers.processor;

import com.deutscheboerse.risk.dave.ers.jaxb.InstrumentBlockT;
import com.deutscheboerse.risk.dave.ers.jaxb.AbstractMessageT;
import com.deutscheboerse.risk.dave.ers.jaxb.FIXML;
import com.deutscheboerse.risk.dave.ers.jaxb.PositionReportMessageT;
import io.vertx.core.json.JsonObject;
import java.math.BigInteger;
import java.util.Date;
import java.util.Optional;
import javax.xml.bind.JAXBElement;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class PositionReportProcessor extends AbstractProcessor implements Processor {

    private JsonObject parseFromFIXML(FIXML fixml) {
        JAXBElement<? extends AbstractMessageT> msg = fixml.getMessage();
        PositionReportMessageT prMessage = (PositionReportMessageT) msg.getValue();

        JsonObject pr = new JsonObject();
        pr.put("received", new JsonObject().put("$date", timestampFormatter.format(new Date())));
        pr.put("reqId", prMessage.getID());
        pr.put("rptId", prMessage.getRptID());
        pr.put("bizDt", new JsonObject().put("$date", timestampFormatter.format(prMessage.getBizDt().toGregorianCalendar().getTime())));
        Optional.ofNullable(prMessage.getLastRptReqed()).ifPresent(lastReport -> pr.put("lastReportRequested", lastReport.value()));
        pr.put("sesId", prMessage.getSetSesID().value());

        processParties(prMessage.getPty(), pr);

        InstrumentBlockT instrument = prMessage.getInstrmt();
        pr.put("symbol", instrument.getSym().trim());
        if (instrument.getPutCall() != null) {
            if (instrument.getPutCall().equals(BigInteger.ZERO)) {
                pr.put("putCall", "P");
            } else if (instrument.getPutCall().equals(BigInteger.ONE)) {
                pr.put("putCall", "C");
            }
        }
        pr.put("maturityMonthYear", instrument.getMMY());
        if (instrument.getStrkPx() != null) {
            pr.put("strikePrice", instrument.getStrkPx().toString());
        }
        pr.put("optAttribute", instrument.getOptAt());

        prMessage.getQty().forEach(positionQty -> {
            switch (positionQty.getTyp()) {
                case XM:
                    pr.put("crossMarginLongQty", positionQty.getLong().doubleValue());
                    pr.put("crossMarginShortQty", positionQty.getShort().doubleValue());
                    break;
                case EX:
                    pr.put("optionExcerciseQty", positionQty.getLong().doubleValue());
                    break;
                case AS:
                    pr.put("optionAssignmentQty", positionQty.getShort().doubleValue());
                    break;
                case ALC:
                    pr.put("allocationTradeQty", positionQty.getLong().doubleValue());
                    break;
                case DN:
                    pr.put("deliveryNoticeQty", positionQty.getShort().doubleValue());
                    break;
            }
        });
        return pr;
    }

   @Override
   public void process(Exchange exchange) {
        Message in = exchange.getIn();
        in.setBody(this.parseFromFIXML((FIXML)in.getBody()));
    }
}
