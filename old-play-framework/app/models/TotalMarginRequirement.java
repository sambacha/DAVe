package models;

import ers.jaxb.*;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.xml.bind.JAXBElement;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
public class TotalMarginRequirement extends Model {
    @Id
    public Long id;

    @Constraints.Required
    public String clearer;

    @Constraints.Required
    public String pool;

    @Constraints.Required
    public String member;

    @Constraints.Required
    public String account;

    @Constraints.Required
    public String ccy;

    @Constraints.Required
    @Formats.DateTime(pattern="dd-MM-yyyy hh:mm:ss")
    public Date txnTm;

    @Constraints.Required
    @Formats.DateTime(pattern="dd-MM-yyyy")
    public Date bizDt;

    public String reqId;

    @Constraints.Required
    public String rptId;

    // ITD or EOD
    @Constraints.Required
    public String sesId;

    @Constraints.Required
    public BigDecimal unadjustedMargin;

    @Constraints.Required
    public BigDecimal adjustedMargin;

    @Constraints.Required
    @Formats.DateTime(pattern="dd-MM-yyyy hh:mm:ss")
    public Date received;

    public static Finder<Long, TotalMarginRequirement> find = new Finder<Long, TotalMarginRequirement>(
            Long.class, TotalMarginRequirement.class
    );

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
