package com.deutscheboerse.risk.dave.ers.processor;

import com.deutscheboerse.risk.dave.ers.jaxb.*;
import io.vertx.core.json.JsonObject;

import java.math.BigInteger;

abstract class AbstractMarginRequirementInquiryRequestProcessor extends AbstractRequestProcessor {
    public AbstractMarginRequirementInquiryRequestProcessor(String replyToAddress)
    {
        super(replyToAddress);
    }

    protected MarginReqmtInqQualGrpBlockT getQual(int qualTyp)
    {
        MarginReqmtInqQualGrpBlockT qual = new MarginReqmtInqQualGrpBlockT();
        qual.setQual(BigInteger.valueOf(qualTyp));

        return qual;
    }

    protected FIXML createMarginRequirementInquiry(JsonObject request, int qualTyp)
    {
        ObjectFactory of = new ObjectFactory();
        FIXML fixml = new FIXML();
        MarginRequirementInquiryMessageT mrim = new MarginRequirementInquiryMessageT();
        mrim.setSetSesID(SettlSessIDEnumT.ITD);
        mrim.setID(getRequestId());
        mrim.getMgnReqmtInqQual().add(getQual(qualTyp));
        mrim.getPty().add(getClearer(request));
        mrim.getPty().add(getMember(request));

        if (request.getString("product") != null)
        {
            InstrumentBlockT instrument = new InstrumentBlockT();
            instrument.setSym(request.getString("product"));
            mrim.setInstrmt(instrument);
        }

        fixml.setMessage(of.createMgnReqmtInq(mrim));

        return fixml;
    }
}
