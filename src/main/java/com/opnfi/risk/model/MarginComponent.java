package com.opnfi.risk.model;

import com.opnfi.risk.model.jaxb.*;
import javax.xml.bind.JAXBElement;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class MarginComponent {
    public Long id;
    public String clearer;
    public String member;
    public String account;
    public String clss;
    public String ccy;
    public Date txnTm;
    public Date bizDt;
    public String reqId;
    public String rptId;
    public String sesId;
    public BigDecimal variationMargin;
    public BigDecimal premiumMargin;
    public BigDecimal liquiMargin;
    public BigDecimal spreadMargin;
    public BigDecimal additionalMargin;
    public Date received;

    public String functionalKey()
    {
        return clearer + "-" + member + "-" + account + "-" + clss + "-" + ccy;
    }

    public static MarginComponent parseFromFIXML(FIXML fixml) {
        JAXBElement<? extends AbstractMessageT> msg = fixml.getMessage();
        MarginRequirementReportMessageT mcMessage = (MarginRequirementReportMessageT) msg.getValue();

        MarginComponent mc = new MarginComponent();
        mc.received = new Date();
        mc.reqId = mcMessage.getID();
        mc.sesId = mcMessage.getSetSesID().toString();
        mc.rptId = mcMessage.getRptID();
        mc.txnTm = mcMessage.getTxnTm().toGregorianCalendar().getTime();
        mc.bizDt = mcMessage.getBizDt().toGregorianCalendar().getTime();
        mc.clss = mcMessage.getClss();

        List<PartiesBlockT> parties = mcMessage.getPty();

        for (PartiesBlockT party : parties)
        {
            if (party.getR().intValue() == 4)
            {
                mc.clearer = party.getID();
            }
            else if (party.getR().intValue() == 1)
            {
                mc.member = party.getID();

                List<PtysSubGrpBlockT> accounts = party.getSub();
                for (PtysSubGrpBlockT account : accounts)
                {
                    if ("26".equals(account.getTyp()))
                    {
                        mc.account = account.getID();
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
                    mc.variationMargin = margin.getAmt();
                    break;
                case "17":
                    mc.premiumMargin = margin.getAmt();
                    break;
                case "12":
                    mc.liquiMargin = margin.getAmt();
                    break;
                case "10":
                    mc.spreadMargin = margin.getAmt();
                    break;
                case "1":
                    mc.additionalMargin = margin.getAmt();
                    break;
            }

            mc.ccy = margin.getCcy();

        }

        return mc;
    }
}
