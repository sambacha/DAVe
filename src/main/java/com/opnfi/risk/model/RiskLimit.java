package com.opnfi.risk.model;

import com.opnfi.risk.model.jaxb.*;

import javax.xml.bind.JAXBElement;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RiskLimit {
    public Long id;
    public String clearer;
    public String member;
    public String maintainer;
    public Date txnTm;
    public String reqId;
    public String rptId;
    public String reqRslt;
    public String txt;
    public String limitType;
    public BigDecimal utilization;
    public BigDecimal warningLevel = null;
    public BigDecimal throttleLevel = null;
    public BigDecimal rejectLevel = null;
    public Date received;

    public String functionalKey()
    {
        return clearer + "-" + member + "-" + maintainer + "-" + limitType;
    }

    public static List<RiskLimit> parseFromFIXML(FIXML fixml) {
        List<RiskLimit> riskLimits = new ArrayList<RiskLimit>();

        JAXBElement<? extends AbstractMessageT> msg = fixml.getMessage();
        PartyRiskLimitsReportMessageT rlMessage = (PartyRiskLimitsReportMessageT) msg.getValue();

        // Parse generic data which are common for all limit types
        Date received = new Date();
        String clearer = null;
        String member = null;
        String maintainer = null;
        Date txnTm = rlMessage.getTxnTm().toGregorianCalendar().getTime();
        String reqId = rlMessage.getReqID();
        String rptId = rlMessage.getRptID();
        String reqRslt = rlMessage.getReqRslt();
        String txt = rlMessage.getTxt();

        List<PartyDetailGrpBlockT> parties = rlMessage.getPtyRiskLmt().get(0).getPtyDetl();

        for (PartyDetailGrpBlockT party : parties)
        {
            if (party.getR().intValue() == 4)
            {
                clearer = party.getID();
            }
            else if (party.getR().intValue() == 1)
            {
                member = party.getID();
            }
            else if (party.getR().intValue() == 7)
            {
                maintainer = party.getID();
            }
        }

        // Parse the specific risk limits


        List<RiskLimitTypesGrpBlockT> limits = rlMessage.getPtyRiskLmt().get(0).getRiskLmt().get(0).getRiskLmtTyp();

        for (RiskLimitTypesGrpBlockT limit : limits)
        {
            RiskLimit rl = new RiskLimit();
            rl.setReceived(received);
            rl.setClearer(clearer);
            rl.setMember(member);
            rl.setMaintainer(maintainer);
            rl.setTxnTm(txnTm);
            rl.setReqId(reqId);
            rl.setRptId(rptId);
            rl.setReqRslt(reqRslt);
            rl.setTxt(txt);

            switch (limit.getTyp())
            {
                case "4001":
                    rl.setLimitType("TMR");
                    break;
                case "4002":
                    rl.setLimitType("CULI");
                    break;
                case "4003":
                    rl.setLimitType("CASH");
                    break;
                case "4004":
                    rl.setLimitType("NDM");
                    break;
            }

            rl.setUtilization(limit.getUtilztnAmt());

            for (RiskWarningLevelGrpBlockT level : limit.getWarnLvl())
            {
                switch (level.getActn().toString())
                {
                    case "4":
                        rl.setWarningLevel(new BigDecimal(level.getAmt()));
                        break;
                    case "0":
                        rl.setThrottleLevel(new BigDecimal(level.getAmt()));
                        break;
                    case "2":
                        rl.setRejectLevel(new BigDecimal(level.getAmt()));
                        break;
                }
            }

            riskLimits.add(rl);
        }

        return riskLimits;
    }

    public RiskLimit(Long id, String clearer, String member, String maintainer, Date txnTm, String reqId, String rptId, String reqRslt, String txt, String limitType, BigDecimal utilization, BigDecimal warningLevel, BigDecimal throttleLevel, BigDecimal rejectLevel, Date received) {
        this.id = id;
        this.clearer = clearer;
        this.member = member;
        this.maintainer = maintainer;
        this.txnTm = txnTm;
        this.reqId = reqId;
        this.rptId = rptId;
        this.reqRslt = reqRslt;
        this.txt = txt;
        this.limitType = limitType;
        this.utilization = utilization;
        this.warningLevel = warningLevel;
        this.throttleLevel = throttleLevel;
        this.rejectLevel = rejectLevel;
        this.received = received;
    }

    public RiskLimit() {
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

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public Date getTxnTm() {
        return txnTm;
    }

    public void setTxnTm(Date txnTm) {
        this.txnTm = txnTm;
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

    public String getReqRslt() {
        return reqRslt;
    }

    public void setReqRslt(String reqRslt) {
        this.reqRslt = reqRslt;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public String getLimitType() {
        return limitType;
    }

    public void setLimitType(String limitType) {
        this.limitType = limitType;
    }

    public BigDecimal getUtilization() {
        return utilization;
    }

    public void setUtilization(BigDecimal utilization) {
        this.utilization = utilization;
    }

    public BigDecimal getWarningLevel() {
        return warningLevel;
    }

    public void setWarningLevel(BigDecimal warningLevel) {
        this.warningLevel = warningLevel;
    }

    public BigDecimal getThrottleLevel() {
        return throttleLevel;
    }

    public void setThrottleLevel(BigDecimal throttleLevel) {
        this.throttleLevel = throttleLevel;
    }

    public BigDecimal getRejectLevel() {
        return rejectLevel;
    }

    public void setRejectLevel(BigDecimal rejectLevel) {
        this.rejectLevel = rejectLevel;
    }

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }
}
