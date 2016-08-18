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
public class MarginShortfallSurplus extends Model {
    @Id
    public Long id;

    @Constraints.Required
    public String clearer;

    @Constraints.Required
    public String pool;

    @Constraints.Required
    public String poolType;

    @Constraints.Required
    public String member;

    @Constraints.Required
    public String clearingCcy;

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
    public BigDecimal marginRequirement;

    @Constraints.Required
    public BigDecimal securityCollateral;

    @Constraints.Required
    public BigDecimal cashBalance;

    @Constraints.Required
    public BigDecimal shortfallSurplus;

    @Constraints.Required
    public BigDecimal marginCall;

    @Constraints.Required
    @Formats.DateTime(pattern="dd-MM-yyyy hh:mm:ss")
    public Date received;

    public static Finder<Long, MarginShortfallSurplus> find = new Finder<Long, MarginShortfallSurplus>(
            Long.class, MarginShortfallSurplus.class
    );

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
}
