package com.opnfi.risk.model.procesor;

import com.opnfi.risk.model.jaxb.AbstractMessageT;
import com.opnfi.risk.model.jaxb.FIXML;
import com.opnfi.risk.model.jaxb.MarginAmountBlockT;
import com.opnfi.risk.model.jaxb.MarginRequirementReportMessageT;
import com.opnfi.risk.model.jaxb.PartiesBlockT;
import com.opnfi.risk.model.jaxb.PtysSubGrpBlockT;
import io.vertx.core.json.JsonObject;
import java.util.Date;
import java.util.List;
import javax.xml.bind.JAXBElement;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class MarginComponentProcesor extends AbstractProcessor implements Processor {

    private JsonObject parseFromFIXML(FIXML fixml) {
        JAXBElement<? extends AbstractMessageT> msg = fixml.getMessage();
        MarginRequirementReportMessageT mcMessage = (MarginRequirementReportMessageT) msg.getValue();

        JsonObject mc = new JsonObject();
        mc.put("received", new JsonObject().put("$date", AbstractProcessor.timestampFormatter.format(new Date())));
        mc.put("reqId", mcMessage.getID());
        mc.put("sesId", mcMessage.getSetSesID().toString());
        mc.put("rptId", mcMessage.getRptID());
        mc.put("txnTm", new JsonObject().put("$date", AbstractProcessor.timestampFormatter.format(mcMessage.getTxnTm().toGregorianCalendar().getTime())));
        mc.put("bizDt", new JsonObject().put("$date", AbstractProcessor.timestampFormatter.format(mcMessage.getBizDt().toGregorianCalendar().getTime())));
        mc.put("clss", mcMessage.getClss());

        List<PartiesBlockT> parties = mcMessage.getPty();

        for (PartiesBlockT party : parties)
        {
            if (party.getR().intValue() == 4)
            {
                mc.put("clearer", party.getID());
            }
            else if (party.getR().intValue() == 1)
            {
                mc.put("member", party.getID());

                List<PtysSubGrpBlockT> accounts = party.getSub();
                for (PtysSubGrpBlockT account : accounts)
                {
                    if ("26".equals(account.getTyp()))
                    {
                        mc.put("account", account.getID());
                    }
                }
            }
        }

        List<MarginAmountBlockT> margins = mcMessage.getMgnAmt();

        for (MarginAmountBlockT margin : margins)
        {
            switch (margin.getTyp())
            {
                case "23":
                    mc.put("variationMargin", margin.getAmt().doubleValue());
                    break;
                case "17":
                    mc.put("premiumMargin", margin.getAmt().doubleValue());
                    break;
                case "12":
                    mc.put("liquiMargin", margin.getAmt().doubleValue());
                    break;
                case "10":
                    mc.put("spreadMargin", margin.getAmt().doubleValue());
                    break;
                case "1":
                    mc.put("additionalMargin", margin.getAmt().doubleValue());
                    break;
            }
            mc.put("ccy", margin.getCcy());
        }
        return mc;
    }

    @Override
    public void process(Exchange exchange) {
        Message in = exchange.getIn();
        in.setBody(this.parseFromFIXML((FIXML)in.getBody()));
    }
}
