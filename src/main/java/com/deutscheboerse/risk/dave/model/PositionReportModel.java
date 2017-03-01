package com.deutscheboerse.risk.dave.model;

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
