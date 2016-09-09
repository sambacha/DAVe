package com.opnfi.risk.model.procesor;

import com.opnfi.risk.model.jaxb.AbstractMessageT;
import com.opnfi.risk.model.jaxb.FIXML;
import com.opnfi.risk.model.jaxb.MarginAmountBlockT;
import com.opnfi.risk.model.jaxb.MarginRequirementReportMessageT;
import com.opnfi.risk.model.jaxb.PartiesBlockT;
import com.opnfi.risk.model.jaxb.PtysSubGrpBlockT;
import io.vertx.core.json.JsonObject;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.xml.bind.JAXBElement;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class MarginShortfallSurplusProcesor extends AbstractProcessor implements Processor {

    private JsonObject parseFromFIXML(FIXML fixml) {
        JAXBElement<? extends AbstractMessageT> msg = fixml.getMessage();
        MarginRequirementReportMessageT mrrMessage = (MarginRequirementReportMessageT) msg.getValue();

        JsonObject tss = new JsonObject();
        tss.put("received", new JsonObject().put("$date", timestampFormatter.format(new Date())));
        tss.put("reqId", mrrMessage.getID());
        tss.put("sesId", mrrMessage.getSetSesID().toString());
        tss.put("rptId", mrrMessage.getRptID());
        tss.put("txnTm", new JsonObject().put("$date", timestampFormatter.format(mrrMessage.getTxnTm().toGregorianCalendar().getTime())));
        tss.put("bizDt", new JsonObject().put("$date", timestampFormatter.format(mrrMessage.getBizDt().toGregorianCalendar().getTime())));
        tss.put("clearingCcy", mrrMessage.getCcy());

        List<PartiesBlockT> parties = mrrMessage.getPty();

        for (PartiesBlockT party : parties)
        {
            if (party.getR().intValue() == 4)
            {
                tss.put("clearer", party.getID());

                List<PtysSubGrpBlockT> pools = party.getSub();
                for (PtysSubGrpBlockT pool : pools)
                {
                    if ("4000".equals(pool.getTyp()))
                    {
                        tss.put("pool", pool.getID());
                    }
                    else if ("4001".equals(pool.getTyp()))
                    {
                        tss.put("poolType", pool.getID());
                    }
                }
            }
            else if (party.getR().intValue() == 1)
            {
                tss.put("member", party.getID());
            }
        }

        List<MarginAmountBlockT> margins = mrrMessage.getMgnAmt();

        for (MarginAmountBlockT margin : margins)
        {
            switch (margin.getTyp())
            {
                case "22":
                    tss.put("marginRequirement", margin.getAmt().doubleValue());
                    break;
                case "19":
                    tss.put("securityCollateral", margin.getAmt().doubleValue());
                    break;
                case "5":
                    tss.put("cashBalance", margin.getAmt().doubleValue());
                    break;
                case "14":
                    tss.put("shortfallSurplus", margin.getAmt().multiply(BigDecimal.valueOf(-1)).doubleValue());
                    break;
                case "15":
                    tss.put("shortfallSurplus", margin.getAmt().doubleValue());
                    break;
                case "13":
                    tss.put("marginCall", margin.getAmt().doubleValue());
                    break;
            }

            tss.put("ccy", margin.getCcy());
        }

        return tss;
    }

   @Override
   public void process(Exchange exchange) {
        Message in = exchange.getIn();
        in.setBody(this.parseFromFIXML((FIXML)in.getBody()));
    }
}
