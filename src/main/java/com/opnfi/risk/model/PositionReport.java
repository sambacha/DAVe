package com.opnfi.risk.model;

import com.opnfi.risk.model.jaxb.AbstractMessageT;
import com.opnfi.risk.model.jaxb.FIXML;
import com.opnfi.risk.model.jaxb.InstrumentBlockT;
import com.opnfi.risk.model.jaxb.PartiesBlockT;
import com.opnfi.risk.model.jaxb.PositionReportMessageT;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.xml.bind.JAXBElement;

public class PositionReport {
    public Long id;
    public String clearer;
    public String member;
    public String account;
    public String reqId;
    public String rptId;
    public Date bizDt;
    public String lastReportRequested;
    public String settlSesId;
    public String symbol;
    public String putCall;
    public String maturityMonthYear;
    public String strikePrice;
    public String optAttribute;
    public BigDecimal crossMarginLongQty;
    public BigDecimal crossMarginShortQty;
    public BigDecimal optionExcerciseQty;
    public BigDecimal optionAssignmentQty;
    public BigDecimal allocationTradeQty;
    public BigDecimal deliveryNoticeQty;
    public Date received;

    public String functionalKey() {
        String putCallKey = (putCall != null) ? putCall : "";
        String strikePriceKey = (this.strikePrice != null) ? this.strikePrice : "";
        String optStringKey = (this.optAttribute != null) ? this.optAttribute : "";
        return clearer + "-" + member + "-" + account + "-" + symbol + "-" + putCallKey + "-" + strikePriceKey + "-" + optStringKey + "-" + maturityMonthYear;
    }

    public static PositionReport parseFromFIXML(FIXML fixml) {
        JAXBElement<? extends AbstractMessageT> msg = fixml.getMessage();
        PositionReportMessageT prMessage = (PositionReportMessageT) msg.getValue();

        PositionReport pr = new PositionReport();
        pr.received = new Date();
        pr.reqId = prMessage.getID();
        pr.rptId = prMessage.getRptID();
        pr.bizDt = prMessage.getBizDt().toGregorianCalendar().getTime();
        Optional.ofNullable(prMessage.getLastRptReqed()).ifPresent(lastReport -> pr.lastReportRequested = lastReport.value());
        pr.settlSesId = prMessage.getSetSesID().value();

        List<PartiesBlockT> parties = prMessage.getPty();

        for (PartiesBlockT party : parties) {
            if (party.getR().intValue() == 4) {
                pr.clearer = party.getID();
            } else if (party.getR().intValue() == 1) {
                pr.member = party.getID();
                party.getSub().stream()
                        .filter(account -> "26".equals(account.getTyp()))
                        .findFirst()
                        .ifPresent(account -> pr.account = account.getID());
            }
        }
        InstrumentBlockT instrument = prMessage.getInstrmt();
        pr.symbol = instrument.getSym().trim();
        if (instrument.getPutCall() != null) {
            if (instrument.getPutCall().equals(BigInteger.ZERO)) {
                pr.putCall = "P";
            } else if (instrument.getPutCall().equals(BigInteger.ONE)) {
                pr.putCall = "C";
            }
        }
        pr.maturityMonthYear = instrument.getMMY();
        if (instrument.getStrkPx() != null) {
            pr.strikePrice = instrument.getStrkPx().toString();
        }
        pr.optAttribute = instrument.getOptAt();

        prMessage.getQty().forEach(positionQty -> {
            switch (positionQty.getTyp()) {
                case XM:
                    pr.crossMarginLongQty = positionQty.getLong();
                    pr.crossMarginShortQty = positionQty.getShort();
                    break;
                case EX:
                    pr.optionExcerciseQty = positionQty.getLong();
                    break;
                case AS:
                    pr.optionAssignmentQty = positionQty.getShort();
                    break;
                case ALC:
                    pr.allocationTradeQty = positionQty.getLong();
                    break;
                case DN:
                    pr.deliveryNoticeQty = positionQty.getShort();
                    break;
            }
        });
        return pr;
    }

    public PositionReport(Long id, String clearer, String member, String account, String reqId, String rptId, Date bizDt, String lastReportRequested, String settlSesId, String symbol, String putCall, String maturityMonthYear, String strikePrice, String optAttribute, BigDecimal crossMarginLongQty, BigDecimal crossMarginShortQty, BigDecimal optionExcerciseQty, BigDecimal optionAssignmentQty, BigDecimal allocationTradeQty, BigDecimal deliveryNoticeQty, Date received) {
        this.id = id;
        this.clearer = clearer;
        this.member = member;
        this.account = account;
        this.reqId = reqId;
        this.rptId = rptId;
        this.bizDt = bizDt;
        this.lastReportRequested = lastReportRequested;
        this.settlSesId = settlSesId;
        this.symbol = symbol;
        this.putCall = putCall;
        this.maturityMonthYear = maturityMonthYear;
        this.strikePrice = strikePrice;
        this.optAttribute = optAttribute;
        this.crossMarginLongQty = crossMarginLongQty;
        this.crossMarginShortQty = crossMarginShortQty;
        this.optionExcerciseQty = optionExcerciseQty;
        this.optionAssignmentQty = optionAssignmentQty;
        this.allocationTradeQty = allocationTradeQty;
        this.deliveryNoticeQty = deliveryNoticeQty;
        this.received = received;
    }

    public PositionReport() {
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getClearer()
    {
        return clearer;
    }

    public void setClearer(String clearer)
    {
        this.clearer = clearer;
    }

    public String getMember()
    {
        return member;
    }

    public void setMember(String member)
    {
        this.member = member;
    }

    public String getAccount()
    {
        return account;
    }

    public void setAccount(String account)
    {
        this.account = account;
    }

    public String getReqId()
    {
        return reqId;
    }

    public void setReqId(String reqId)
    {
        this.reqId = reqId;
    }

    public String getRptId()
    {
        return rptId;
    }

    public void setRptId(String rptId)
    {
        this.rptId = rptId;
    }

    public Date getBizDt()
    {
        return bizDt;
    }

    public void setBizDt(Date bizDt)
    {
        this.bizDt = bizDt;
    }

    public String getLastReportRequested()
    {
        return lastReportRequested;
    }

    public void setLastReportRequested(String lastReportRequested)
    {
        this.lastReportRequested = lastReportRequested;
    }

    public String getSettlSesId()
    {
        return settlSesId;
    }

    public void setSettlSesId(String settlSesId)
    {
        this.settlSesId = settlSesId;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public void setSymbol(String symbol)
    {
        this.symbol = symbol;
    }

    public String getPutCall()
    {
        return putCall;
    }

    public void setPutCall(String putCall)
    {
        this.putCall = putCall;
    }

    public String getMaturityMonthYear()
    {
        return maturityMonthYear;
    }

    public void setMaturityMonthYear(String maturityMonthYear)
    {
        this.maturityMonthYear = maturityMonthYear;
    }

    public String getStrikePrice()
    {
        return strikePrice;
    }

    public void setStrikePrice(String strikePrice)
    {
        this.strikePrice = strikePrice;
    }

    public String getOptAttribute()
    {
        return optAttribute;
    }

    public void setOptAttribute(String optAttribute)
    {
        this.optAttribute = optAttribute;
    }

    public BigDecimal getCrossMarginLongQty()
    {
        return crossMarginLongQty;
    }

    public void setCrossMarginLongQty(BigDecimal crossMarginLongQty)
    {
        this.crossMarginLongQty = crossMarginLongQty;
    }

    public BigDecimal getCrossMarginShortQty()
    {
        return crossMarginShortQty;
    }

    public void setCrossMarginShortQty(BigDecimal crossMarginShortQty)
    {
        this.crossMarginShortQty = crossMarginShortQty;
    }

    public BigDecimal getOptionExcerciseQty()
    {
        return optionExcerciseQty;
    }

    public void setOptionExcerciseQty(BigDecimal optionExcerciseQty)
    {
        this.optionExcerciseQty = optionExcerciseQty;
    }

    public BigDecimal getOptionAssignmentQty()
    {
        return optionAssignmentQty;
    }

    public void setOptionAssignmentQty(BigDecimal optionAssignmentQty)
    {
        this.optionAssignmentQty = optionAssignmentQty;
    }

    public BigDecimal getAllocationTradeQty()
    {
        return allocationTradeQty;
    }

    public void setAllocationTradeQty(BigDecimal allocationTradeQty)
    {
        this.allocationTradeQty = allocationTradeQty;
    }

    public BigDecimal getDeliveryNoticeQty()
    {
        return deliveryNoticeQty;
    }

    public void setDeliveryNoticeQty(BigDecimal deliveryNoticeQty)
    {
        this.deliveryNoticeQty = deliveryNoticeQty;
    }

    public Date getReceived()
    {
        return received;
    }

    public void setReceived(Date received)
    {
        this.received = received;
    }

}
