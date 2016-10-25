package com.deutscheboerse.risk.dave.ers.model;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * Created by schojak on 15.9.16.
 */
public class PositionReportModel extends AbstractModel {
    private static final String MONGO_HISTORY_COLLECTION = "ers.PositionReport";
    private static final String MONGO_LATEST_COLLECTION = "ers.PositionReport.latest";

    public PositionReportModel() {
        super(MONGO_HISTORY_COLLECTION, MONGO_LATEST_COLLECTION);
    }

    @Override
    public JsonObject queryLatestDocument(Message<?> msg) {
        JsonObject message = (JsonObject)msg.body();
        JsonObject query = new JsonObject();
        query.put("clearer", message.getValue("clearer"));
        query.put("member", message.getValue("member"));
        query.put("account", message.getValue("account"));
        query.put("clss", message.getValue("clss"));
        query.put("symbol", message.getValue("symbol"));
        query.put("putCall", message.getValue("putCall"));
        query.put("strikePrice", message.getValue("strikePrice"));
        query.put("optAttribute", message.getValue("optAttribute"));
        query.put("maturityMonthYear", message.getValue("maturityMonthYear"));
        return query;
    }

    @Override
    public JsonObject makeLatestDocument(Message<?> msg) {
        JsonObject message = (JsonObject)msg.body();
        JsonObject document = new JsonObject();
        document.put("clearer", message.getValue("clearer"));
        document.put("member", message.getValue("member"));
        document.put("account", message.getValue("account"));
        document.put("clss", message.getValue("clss"));
        document.put("reqId", message.getValue("reqId"));
        document.put("rptId", message.getValue("rptId"));
        document.put("bizDt", message.getValue("bizDt"));
        document.put("lastReportRequested", message.getValue("lastReportRequested"));
        document.put("sesId", message.getValue("sesId"));
        document.put("symbol", message.getValue("symbol"));
        document.put("putCall", message.getValue("putCall"));
        document.put("maturityMonthYear", message.getValue("maturityMonthYear"));
        document.put("strikePrice", message.getValue("strikePrice"));
        document.put("optAttribute", message.getValue("optAttribute"));
        document.put("crossMarginLongQty", message.getValue("crossMarginLongQty"));
        document.put("crossMarginShortQty", message.getValue("crossMarginShortQty"));
        document.put("optionExcerciseQty", message.getValue("optionExcerciseQty"));
        document.put("optionAssignmentQty", message.getValue("optionAssignmentQty"));
        document.put("allocationTradeQty", message.getValue("allocationTradeQty"));
        document.put("deliveryNoticeQty", message.getValue("deliveryNoticeQty"));
        document.put("clearingCcy", message.getValue("clearingCcy"));
        document.put("mVar", message.getValue("mVar"));
        document.put("compVar", message.getValue("compVar"));
        document.put("compCorrelationBreak", message.getValue("compCorrelationBreak"));
        document.put("compCompressionError", message.getValue("compCompressionError"));
        document.put("compLiquidityAddOn", message.getValue("compLiquidityAddOn"));
        document.put("compLongOptionCredit", message.getValue("compLongOptionCredit"));
        document.put("productCcy", message.getValue("productCcy"));
        document.put("variationMarginPremiumPayment", message.getValue("variationMarginPremiumPayment"));
        document.put("premiumMargin", message.getValue("premiumMargin"));
        document.put("delta", message.getValue("delta"));
        document.put("gamma", message.getValue("gamma"));
        document.put("vega", message.getValue("vega"));
        document.put("rho", message.getValue("rho"));
        document.put("theta", message.getValue("theta"));
        document.put("underlying", message.getValue("underlying"));
        document.put("received", message.getJsonObject("received").getString("$date"));
        return document;
    }

    @Override
    protected JsonObject getProject() {
        JsonObject project = new JsonObject();
        project.put("_id", 0);
        project.put("id", "$_id");
        project.put("clearer", 1);
        project.put("member", 1);
        project.put("account", 1);
        project.put("reqId", 1);
        project.put("rptId", 1);
        project.put("bizDt", 1);
        project.put("lastReportRequested", 1);
        project.put("sesId", 1);
        project.put("clss", 1);
        project.put("symbol", 1);
        project.put("putCall", 1);
        project.put("maturityMonthYear", 1);
        project.put("strikePrice", 1);
        project.put("optAttribute", 1);
        project.put("crossMarginLongQty", 1);
        project.put("crossMarginShortQty", 1);
        project.put("optionExcerciseQty", 1);
        project.put("optionAssignmentQty", 1);
        project.put("allocationTradeQty", 1);
        project.put("deliveryNoticeQty", 1);
        project.put("clearingCcy", 1);
        project.put("mVar", 1);
        project.put("compVar", 1);
        project.put("compCorrelationBreak", 1);
        project.put("compCompressionError", 1);
        project.put("compLiquidityAddOn", 1);
        project.put("compLongOptionCredit", 1);
        project.put("productCcy", 1);
        project.put("variationMarginPremiumPayment", 1);
        project.put("premiumMargin", 1);
        project.put("delta", 1);
        project.put("gamma", 1);
        project.put("vega", 1);
        project.put("rho", 1);
        project.put("theta", 1);
        project.put("underlying", 1);
        project.put("received", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received")));
        return project;
    }
}
