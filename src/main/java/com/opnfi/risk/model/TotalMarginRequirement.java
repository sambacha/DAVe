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

    public TotalMarginRequirement(Long id, String clearer, String pool, String member, String account, String ccy, Date txnTm, Date bizDt, String reqId, String rptId, String sesId, BigDecimal unadjustedMargin, BigDecimal adjustedMargin, Date received) {
        this.id = id;
        this.clearer = clearer;
        this.pool = pool;
        this.member = member;
        this.account = account;
        this.ccy = ccy;
        this.txnTm = txnTm;
        this.bizDt = bizDt;
        this.reqId = reqId;
        this.rptId = rptId;
        this.sesId = sesId;
        this.unadjustedMargin = unadjustedMargin;
        this.adjustedMargin = adjustedMargin;
        this.received = received;
    }

    public TotalMarginRequirement() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClearer() {
        return clearer;
    }

    public void setClearer(String clearer) {
        this.clearer = clearer;
    }

    public String getPool() {
        return pool;
    }

    public void setPool(String pool) {
        this.pool = pool;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCcy() {
        return ccy;
    }

    public void setCcy(String ccy) {
        this.ccy = ccy;
    }

    public Date getTxnTm() {
        return txnTm;
    }

    public void setTxnTm(Date txnTm) {
        this.txnTm = txnTm;
    }

    public Date getBizDt() {
        return bizDt;
    }

    public void setBizDt(Date bizDt) {
        this.bizDt = bizDt;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public String getRptId() {
        return rptId;
    }

    public void setRptId(String rptId) {
        this.rptId = rptId;
    }

    public String getSesId() {
        return sesId;
    }

    public void setSesId(String sesId) {
        this.sesId = sesId;
    }

    public BigDecimal getUnadjustedMargin() {
        return unadjustedMargin;
    }

    public void setUnadjustedMargin(BigDecimal unadjustedMargin) {
        this.unadjustedMargin = unadjustedMargin;
    }

    public BigDecimal getAdjustedMargin() {
        return adjustedMargin;
    }

    public void setAdjustedMargin(BigDecimal adjustedMargin) {
        this.adjustedMargin = adjustedMargin;
    }

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }
}
