package com.deutscheboerse.risk.dave.ers.processor;

import com.deutscheboerse.risk.dave.ers.jaxb.*;
import io.vertx.core.json.JsonObject;
import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javax.xml.bind.JAXBElement;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class PositionReportProcessor extends AbstractProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(PositionReportProcessor.class);

    private JsonObject parseFromFIXML(FIXML fixml) throws Exception {
        JAXBElement<? extends AbstractMessageT> msg = fixml.getMessage();

        if (msg.getValue() instanceof PositionReportMessageT) {
            return processPositionReport((PositionReportMessageT) msg.getValue());
        } else {
            processMarginRequirementInqAck((MarginRequirementInquiryAckMessageT) msg.getValue());
            throw new Exception("Something went wrong");
        }
    }

    private JsonObject processPositionReport(PositionReportMessageT prMessage)
    {
        JsonObject pr = new JsonObject();
        pr.put("received", new JsonObject().put("$date", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        pr.put("reqId", prMessage.getID());
        pr.put("rptId", prMessage.getRptID());
        pr.put("bizDt", prMessage.getBizDt().toGregorianCalendar().toZonedDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
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

    private void processMarginRequirementInqAck(MarginRequirementInquiryAckMessageT ackMessage) throws Exception {
        String result = ackMessage.getRslt();
        String error = ackMessage.getTxt();

        switch (result)
        {
            case "304":
                // Unknown product -> E.g. running PROD data in TEST?
                LOG.debug("Received MarginRequirementInquiryAcknowledgement with result {} and error message '{}'", result, error);
                break;
            case "801":
                // No positions -> this is not really an error
                LOG.trace("Received MarginRequirementInquiryAcknowledgement with result {} and error message '{}'", result, error);
                break;
            default:
                LOG.error("Received MarginRequirementInquiryAcknowledgement with result {} and error message '{}'", result, error);
                throw new Exception();
        }
    }

   @Override
   public void process(Exchange exchange) {
       Message in = exchange.getIn();

       try {
           in.setBody(parseFromFIXML((FIXML) in.getBody()));
       }
       catch (Exception e)
       {
           // Stop the exchange
           exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
       }
    }
}
