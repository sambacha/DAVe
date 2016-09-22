package com.deutscheboerse.risk.dave.ers.processor;

import com.deutscheboerse.risk.dave.ers.jaxb.*;
import io.vertx.core.json.JsonObject;

import java.math.BigInteger;

public class AbstractMarginRequirementInquiryRequestProcessor extends AbstractRequestProcessor {
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
        MarginRequirementInquiryMessageT mssr = new MarginRequirementInquiryMessageT();
        mssr.setSetSesID(SettlSessIDEnumT.ITD);
        mssr.setID(getRequestId());
        mssr.getMgnReqmtInqQual().add(getQual(qualTyp));
        mssr.getPty().add(getClearer(request));
        mssr.getPty().add(getMember(request));

        fixml.setMessage(of.createMgnReqmtInq(mssr));

        return fixml;
    }
}
