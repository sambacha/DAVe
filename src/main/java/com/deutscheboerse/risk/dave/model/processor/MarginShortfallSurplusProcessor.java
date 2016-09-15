package com.deutscheboerse.risk.dave.model.processor;

import com.deutscheboerse.risk.dave.model.jaxb.MarginRequirementReportMessageT;
import com.deutscheboerse.risk.dave.model.jaxb.AbstractMessageT;
import com.deutscheboerse.risk.dave.model.jaxb.FIXML;
import com.deutscheboerse.risk.dave.model.jaxb.MarginAmountBlockT;
import com.deutscheboerse.risk.dave.model.jaxb.PartiesBlockT;
import com.deutscheboerse.risk.dave.model.jaxb.PtysSubGrpBlockT;
import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

   @Override
   public void process(Exchange exchange) {
        Message in = exchange.getIn();
        in.setBody(this.parseFromFIXML((FIXML)in.getBody()));
    }
}
