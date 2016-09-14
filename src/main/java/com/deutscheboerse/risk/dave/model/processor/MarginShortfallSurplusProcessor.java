package com.deutscheboerse.risk.dave.model.processor;

import com.deutscheboerse.risk.dave.model.jaxb.MarginRequirementReportMessageT;
import com.deutscheboerse.risk.dave.model.jaxb.AbstractMessageT;
import com.deutscheboerse.risk.dave.model.jaxb.FIXML;
import com.deutscheboerse.risk.dave.model.jaxb.MarginAmountBlockT;
import com.deutscheboerse.risk.dave.model.jaxb.PartiesBlockT;
import com.deutscheboerse.risk.dave.model.jaxb.PtysSubGrpBlockT;
import io.vertx.core.json.JsonObject;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.xml.bind.JAXBElement;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class MarginShortfallSurplusProcessor extends AbstractProcessor implements Processor {

    private JsonObject parseFromFIXML(FIXML fixml) {
        JAXBElement<? extends AbstractMessageT> msg = fixml.getMessage();
        MarginRequirementReportMessageT mrrMessage = (MarginRequirementReportMessageT) msg.getValue();

        JsonObject mss = new JsonObject();
        mss.put("received", new JsonObject().put("$date", timestampFormatter.format(new Date())));
        mss.put("reqId", mrrMessage.getID());
        mss.put("sesId", mrrMessage.getSetSesID().toString());
        mss.put("rptId", mrrMessage.getRptID());
        mss.put("txnTm", new JsonObject().put("$date", timestampFormatter.format(mrrMessage.getTxnTm().toGregorianCalendar().getTime())));
        mss.put("bizDt", new JsonObject().put("$date", timestampFormatter.format(mrrMessage.getBizDt().toGregorianCalendar().getTime())));
        mss.put("clearingCcy", mrrMessage.getCcy());

        List<PartiesBlockT> parties = mrrMessage.getPty();

        for (PartiesBlockT party : parties)
        {
            if (party.getR().intValue() == 4)
            {
                mss.put("clearer", party.getID());

                List<PtysSubGrpBlockT> pools = party.getSub();
                for (PtysSubGrpBlockT pool : pools)
                {
                    if ("4000".equals(pool.getTyp()))
                    {
                        mss.put("pool", pool.getID());
                    }
                    else if ("4001".equals(pool.getTyp()))
                    {
                        mss.put("poolType", pool.getID());
                    }
                }
            }
            else if (party.getR().intValue() == 1)
            {
                mss.put("member", party.getID());
            }
        }

        List<MarginAmountBlockT> margins = mrrMessage.getMgnAmt();

        for (MarginAmountBlockT margin : margins)
        {
            switch (margin.getTyp())
            {
                case "22":
                    mss.put("marginRequirement", margin.getAmt().doubleValue());
                    mss.put("ccy", margin.getCcy());
                    break;
                case "19":
                    mss.put("securityCollateral", margin.getAmt().doubleValue());
                    mss.put("ccy", margin.getCcy());
                    break;
                case "5":
                    mss.put("cashBalance", margin.getAmt().doubleValue());
                    mss.put("ccy", margin.getCcy());
                    break;
                case "14":
                    mss.put("shortfallSurplus", margin.getAmt().multiply(BigDecimal.valueOf(-1)).doubleValue());
                    mss.put("ccy", margin.getCcy());
                    break;
                case "15":
                    mss.put("shortfallSurplus", margin.getAmt().doubleValue());
                    mss.put("ccy", margin.getCcy());
                    break;
                case "13":
                    mss.put("marginCall", margin.getAmt().doubleValue());
                    break;
            }
        }

        return mss;
    }

   @Override
   public void process(Exchange exchange) {
        Message in = exchange.getIn();
        in.setBody(this.parseFromFIXML((FIXML)in.getBody()));
    }
}
