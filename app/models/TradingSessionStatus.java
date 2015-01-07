package models;

import ers.jaxb.AbstractMessageT;
import ers.jaxb.FIXML;
import ers.jaxb.TradingSessionStatusMessageT;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.xml.bind.JAXBElement;

import play.data.validation.Constraints;

@Entity
public class TradingSessionStatus extends Model {
    @Id
    public Long id;

    public String reqId;

    // 1 = Day
    @Constraints.Required
    public String sesId;

    // 1 = Halted
    // 2 = Open
    // 3 = Closed
    // 6 = Request Rejected
    @Constraints.Required
    public String stat;

    public String statRejRsn;

    public String txt;

    public static Finder<Long,TradingSessionStatus> find = new Finder<Long,TradingSessionStatus>(
            Long.class, TradingSessionStatus.class
    );

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
