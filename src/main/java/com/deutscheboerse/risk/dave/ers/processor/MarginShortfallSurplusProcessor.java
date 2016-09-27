package com.deutscheboerse.risk.dave.ers.processor;

import com.deutscheboerse.risk.dave.ers.jaxb.*;
import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class MarginShortfallSurplusProcessor extends AbstractProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(MarginShortfallSurplusProcessor.class);

    private JsonObject parseFromFIXML(FIXML fixml) throws Exception {
        JAXBElement<? extends AbstractMessageT> msg = fixml.getMessage();

        if (msg.getValue() instanceof MarginRequirementReportMessageT) {
            return processMarginRequirementReport((MarginRequirementReportMessageT)msg.getValue());
        }
        else
        {
            processMarginRequirementInqAck((MarginRequirementInquiryAckMessageT)msg.getValue());
            throw new Exception("Something went wrong");
        }
    }

    private JsonObject processMarginRequirementReport(MarginRequirementReportMessageT mrrMessage)
    {
        JsonObject mss = new JsonObject();
        mss.put("received", new JsonObject().put("$date", timestampFormatter.format(new Date())));
        mss.put("reqId", mrrMessage.getID());
        mss.put("sesId", mrrMessage.getSetSesID().toString());
        mss.put("rptId", mrrMessage.getRptID());
        mss.put("txnTm", new JsonObject().put("$date", timestampFormatter.format(mrrMessage.getTxnTm().toGregorianCalendar().getTime())));
        mss.put("bizDt", new JsonObject().put("$date", timestampFormatter.format(mrrMessage.getBizDt().toGregorianCalendar().getTime())));
        mss.put("clearingCcy", mrrMessage.getCcy());

        processParties(mrrMessage.getPty(), mss);

        List<MarginAmountBlockT> margins = mrrMessage.getMgnAmt();
        Set<String> typs = new HashSet<>();
        typs.add("5");
        typs.add("13");
        typs.add("14");
        typs.add("15");
        typs.add("19");
        typs.add("22");
        processMarginBlocks(margins, Collections.unmodifiableSet(typs), mss);

        return mss;
    }

    private void processMarginRequirementInqAck(MarginRequirementInquiryAckMessageT ackMessage) throws Exception {
        String result = ackMessage.getRslt();
        String error = ackMessage.getTxt();

        switch (result)
        {
            case "307":
                LOG.info("Received MarginRequirementInquiryAcknowledgement with result {} and error message '{}'", result, error);
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
