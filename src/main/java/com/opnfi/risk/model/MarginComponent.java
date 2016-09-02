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

    public MarginComponent() {
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

    public String getClss() {
        return clss;
    }

    public void setClss(String clss) {
        this.clss = clss;
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

    public BigDecimal getVariationMargin() {
        return variationMargin;
    }

    public void setVariationMargin(BigDecimal variationMargin) {
        this.variationMargin = variationMargin;
    }

    public BigDecimal getPremiumMargin() {
        return premiumMargin;
    }

    public void setPremiumMargin(BigDecimal premiumMargin) {
        this.premiumMargin = premiumMargin;
    }

    public BigDecimal getLiquiMargin() {
        return liquiMargin;
    }

    public void setLiquiMargin(BigDecimal liquiMargin) {
        this.liquiMargin = liquiMargin;
    }

    public BigDecimal getSpreadMargin() {
        return spreadMargin;
    }

    public void setSpreadMargin(BigDecimal spreadMargin) {
        this.spreadMargin = spreadMargin;
    }

    public BigDecimal getAdditionalMargin() {
        return additionalMargin;
    }

    public void setAdditionalMargin(BigDecimal additionalMargin) {
        this.additionalMargin = additionalMargin;
    }

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }
}
