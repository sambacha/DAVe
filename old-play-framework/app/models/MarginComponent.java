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
public class MarginComponent extends Model {
    @Id
    public Long id;

    @Constraints.Required
    public String clearer;

    @Constraints.Required
    public String member;

    @Constraints.Required
    public String account;

    @Constraints.Required
    public String clss;

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
    public BigDecimal variationMargin;

    @Constraints.Required
    public BigDecimal premiumMargin;

    @Constraints.Required
    public BigDecimal liquiMargin;

    @Constraints.Required
    public BigDecimal spreadMargin;

    @Constraints.Required
    public BigDecimal additionalMargin;

    @Constraints.Required
    @Formats.DateTime(pattern="dd-MM-yyyy hh:mm:ss")
    public Date received;

    public static Finder<Long, MarginComponent> find = new Finder<Long, MarginComponent>(
            Long.class, MarginComponent.class
    );

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
}
