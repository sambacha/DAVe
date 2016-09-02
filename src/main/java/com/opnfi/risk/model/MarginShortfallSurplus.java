package com.opnfi.risk.model;

import com.opnfi.risk.model.jaxb.*;
import javax.xml.bind.JAXBElement;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class MarginShortfallSurplus {
    public Long id;
    public String clearer;
    public String pool;
    public String poolType;
    public String member;
    public String clearingCcy;
    public String ccy;
    public Date txnTm;
    public Date bizDt;
    public String reqId;
    public String rptId;
    public String sesId;
    public BigDecimal marginRequirement;
    public BigDecimal securityCollateral;
    public BigDecimal cashBalance;
    public BigDecimal shortfallSurplus;
    public BigDecimal marginCall;
    public Date received;

    public String functionalKey()
    {
        return clearer + "-" + pool + "-" + member + "-" + clearingCcy + "-" + ccy;
    }

    public static MarginShortfallSurplus parseFromFIXML(FIXML fixml) {
        JAXBElement<? extends AbstractMessageT> msg = fixml.getMessage();
        MarginRequirementReportMessageT mcMessage = (MarginRequirementReportMessageT) msg.getValue();

        MarginShortfallSurplus mss = new MarginShortfallSurplus();
        mss.received = new Date();
        mss.reqId = mcMessage.getID();
        mss.sesId = mcMessage.getSetSesID().toString();
        mss.rptId = mcMessage.getRptID();
        mss.txnTm = mcMessage.getTxnTm().toGregorianCalendar().getTime();
        mss.bizDt = mcMessage.getBizDt().toGregorianCalendar().getTime();
        mss.clearingCcy = mcMessage.getCcy();

        List<PartiesBlockT> parties = mcMessage.getPty();

        for (PartiesBlockT party : parties)
        {
            if (party.getR().intValue() == 4)
            {
                mss.clearer = party.getID();

                List<PtysSubGrpBlockT> pools = party.getSub();
                for (PtysSubGrpBlockT pool : pools)
                {
                    if ("4000".equals(pool.getTyp()))
                    {
                        mss.pool = pool.getID();
                    }
                    else if ("4001".equals(pool.getTyp()))
                    {
                        mss.poolType = pool.getID();
                    }
                }
            }
            else if (party.getR().intValue() == 1)
            {
                mss.member = party.getID();
            }
        }

        List<MarginAmountBlockT> margins = mcMessage.getMgnAmt();

        for (MarginAmountBlockT margin : margins)
        {
            switch (margin.getTyp())
            {
                case "22":
                    mss.marginRequirement = margin.getAmt();
                    break;
                case "19":
                    mss.securityCollateral = margin.getAmt();
                    break;
                case "5":
                    mss.cashBalance = margin.getAmt();
                    break;
                case "14":
                    mss.shortfallSurplus = margin.getAmt().multiply(BigDecimal.valueOf(-1));
                    break;
                case "15":
                    mss.shortfallSurplus = margin.getAmt();
                    break;
                case "13":
                    mss.marginCall = margin.getAmt();
                    break;
            }

            mss.ccy = margin.getCcy();
        }

        return mss;
    }

    public MarginShortfallSurplus() {
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

    public String getPoolType() {
        return poolType;
    }

    public void setPoolType(String poolType) {
        this.poolType = poolType;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public String getClearingCcy() {
        return clearingCcy;
    }

    public void setClearingCcy(String clearingCcy) {
        this.clearingCcy = clearingCcy;
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

    public BigDecimal getMarginRequirement() {
        return marginRequirement;
    }

    public void setMarginRequirement(BigDecimal marginRequirement) {
        this.marginRequirement = marginRequirement;
    }

    public BigDecimal getSecurityCollateral() {
        return securityCollateral;
    }

    public void setSecurityCollateral(BigDecimal securityCollateral) {
        this.securityCollateral = securityCollateral;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public BigDecimal getShortfallSurplus() {
        return shortfallSurplus;
    }

    public void setShortfallSurplus(BigDecimal shortfallSurplus) {
        this.shortfallSurplus = shortfallSurplus;
    }

    public BigDecimal getMarginCall() {
        return marginCall;
    }

    public void setMarginCall(BigDecimal marginCall) {
        this.marginCall = marginCall;
    }

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }
}
