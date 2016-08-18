package com.opnfi.risk.model;

import com.opnfi.risk.model.jaxb.*;
import javax.xml.bind.JAXBElement;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class TotalMarginRequirement {
    public Long id;
    public String clearer;
    public String pool;
    public String member;
    public String account;
    public String ccy;
    public Date txnTm;
    public Date bizDt;
    public String reqId;
    public String rptId;
    public String sesId;
    public BigDecimal unadjustedMargin;
    public BigDecimal adjustedMargin;
    public Date received;

    public String functionalKey()
    {
        return clearer + "-" + pool + "-" + member + "-" + account + "-" + ccy;
    }

    public static TotalMarginRequirement parseFromFIXML(FIXML fixml) {
        JAXBElement<? extends AbstractMessageT> msg = fixml.getMessage();
        MarginRequirementReportMessageT mcMessage = (MarginRequirementReportMessageT) msg.getValue();

        TotalMarginRequirement tmr = new TotalMarginRequirement();
        tmr.received = new Date();
        tmr.reqId = mcMessage.getID();
        tmr.sesId = mcMessage.getSetSesID().toString();
        tmr.rptId = mcMessage.getRptID();
        tmr.txnTm = mcMessage.getTxnTm().toGregorianCalendar().getTime();
        tmr.bizDt = mcMessage.getBizDt().toGregorianCalendar().getTime();

        List<PartiesBlockT> parties = mcMessage.getPty();

        for (PartiesBlockT party : parties)
        {
            if (party.getR().intValue() == 4)
            {
                tmr.clearer = party.getID();

                List<PtysSubGrpBlockT> pools = party.getSub();
                for (PtysSubGrpBlockT pool : pools)
                {
                    if ("4000".equals(pool.getTyp()))
                    {
                        tmr.pool = pool.getID();
                    }
                }
            }
            else if (party.getR().intValue() == 1)
            {
                tmr.member = party.getID();

                List<PtysSubGrpBlockT> accounts = party.getSub();
                for (PtysSubGrpBlockT account : accounts)
                {
                    if ("26".equals(account.getTyp()))
                    {
                        tmr.account = account.getID();
                    }
                }
            }
        }

        List<MarginAmountBlockT> margins = mcMessage.getMgnAmt();

        for (MarginAmountBlockT margin : margins)
        {
            switch (margin.getTyp())
            {
                case "2":
                    tmr.adjustedMargin = margin.getAmt();
                    break;
                case "3":
                    tmr.unadjustedMargin = margin.getAmt();
                    break;
            }

            tmr.ccy = margin.getCcy();
        }

        return tmr;
    }
}
