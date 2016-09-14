package com.deutscheboerse.risk.dave.model.processor;

import com.deutscheboerse.risk.dave.model.jaxb.AbstractMessageT;
import com.deutscheboerse.risk.dave.model.jaxb.FIXML;
import com.deutscheboerse.risk.dave.model.jaxb.MarginAmountBlockT;
import com.deutscheboerse.risk.dave.model.jaxb.MarginRequirementReportMessageT;
import com.deutscheboerse.risk.dave.model.jaxb.PartiesBlockT;
import com.deutscheboerse.risk.dave.model.jaxb.PtysSubGrpBlockT;
import io.vertx.core.json.JsonObject;
import java.util.Date;
import java.util.List;
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

        List<PartiesBlockT> parties = tmrMessage.getPty();

        for (PartiesBlockT party : parties)
        {
            if (party.getR().intValue() == 4)
            {
                tmr.put("clearer", party.getID());

                List<PtysSubGrpBlockT> pools = party.getSub();
                for (PtysSubGrpBlockT pool : pools)
                {
                    if ("4000".equals(pool.getTyp()))
                    {
                        tmr.put("pool", pool.getID());
                    }
                }
            }
            else if (party.getR().intValue() == 1)
            {
                tmr.put("member", party.getID());

                List<PtysSubGrpBlockT> accounts = party.getSub();
                for (PtysSubGrpBlockT account : accounts)
                {
                    if ("26".equals(account.getTyp()))
                    {
                        tmr.put("account", account.getID());
                    }
                }
            }
        }

        List<MarginAmountBlockT> margins = tmrMessage.getMgnAmt();

        for (MarginAmountBlockT margin : margins)
        {
            switch (margin.getTyp())
            {
                case "2":
                    tmr.put("adjustedMargin", margin.getAmt().doubleValue());
                    break;
                case "3":
                    tmr.put("unadjustedMargin", margin.getAmt().doubleValue());
                    break;
            }

            tmr.put("ccy", margin.getCcy());
        }

        return tmr;
    }

   @Override
   public void process(Exchange exchange) {
        Message in = exchange.getIn();
        in.setBody(this.parseFromFIXML((FIXML)in.getBody()));
    }
}
