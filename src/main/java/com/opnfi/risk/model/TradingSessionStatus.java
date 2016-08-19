package com.opnfi.risk.model;

import com.opnfi.risk.model.jaxb.AbstractMessageT;
import com.opnfi.risk.model.jaxb.FIXML;
import com.opnfi.risk.model.jaxb.TradingSessionStatusMessageT;
import javax.xml.bind.JAXBElement;

public class TradingSessionStatus {
    public Long id;
    public String reqId;
    public String sesId;
    public String stat;
    public String statRejRsn;
    public String txt;

    public static TradingSessionStatus parseFromFIXML(FIXML fixml) {
        JAXBElement<? extends AbstractMessageT> msg = fixml.getMessage();
        TradingSessionStatusMessageT tssMessage = (TradingSessionStatusMessageT) msg.getValue();

        TradingSessionStatus tss = new TradingSessionStatus();
        tss.reqId = tssMessage.getReqID();
        tss.sesId = tssMessage.getSesID();
        tss.stat = tssMessage.getStat();
        tss.statRejRsn = tssMessage.getStatRejRsn();
        tss.txt = tssMessage.getTxt();

        return tss;
    }

    public TradingSessionStatus(Long id, String reqId, String sesId, String stat, String statRejRsn, String txt) {
        this.id = id;
        this.reqId = reqId;
        this.sesId = sesId;
        this.stat = stat;
        this.statRejRsn = statRejRsn;
        this.txt = txt;
    }

    public TradingSessionStatus() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public String getSesId() {
        return sesId;
    }

    public void setSesId(String sesId) {
        this.sesId = sesId;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public String getStatRejRsn() {
        return statRejRsn;
    }

    public void setStatRejRsn(String statRejRsn) {
        this.statRejRsn = statRejRsn;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }
}
