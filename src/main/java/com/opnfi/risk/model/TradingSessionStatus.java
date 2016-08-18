package com.opnfi.risk.model;

import com.opnfi.risk.model.jaxb.AbstractMessageT;
import com.opnfi.risk.model.jaxb.FIXML;
import com.opnfi.risk.model.jaxb.TradingSessionStatusMessageT;
import javax.xml.bind.JAXBElement;

public class TradingSessionStatus {
    public Long id;
    public String reqId;
    public String sesId;

    // 1 = Halted
    // 2 = Open
    // 3 = Closed
    // 6 = Request Rejected
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
}
