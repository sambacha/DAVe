package com.deutscheboerse.risk.dave.ers.model;

import io.vertx.core.json.JsonObject;

/**
 * Created by schojak on 15.9.16.
 */
public class PositionReportModel extends AbstractModel {
    private static final String MONGO_COLLECTION = "ers.PositionReport";
    private static final AbstractModel INSTANCE = new PositionReportModel();

    protected PositionReportModel() {
    }

    public static JsonObject getLatestCommand(JsonObject params) {
        return INSTANCE.getCommand(MONGO_COLLECTION, INSTANCE.getLatestPipeline(params));
    }

    public static JsonObject getHistoryCommand(JsonObject params)
    {
        return INSTANCE.getCommand(MONGO_COLLECTION, INSTANCE.getHistoryPipeline(params));
    }

    protected JsonObject getGroup()
    {
        JsonObject group = new JsonObject();
        group.put("_id", new JsonObject().put("clearer", "$clearer").put("member", "$member").put("account", "$account").put("clss", "$clss").put("symbol", "$symbol").put("putCall", "$putCall").put("strikePrice", "$strikePrice").put("optAttribute", "$optAttribute").put("maturityMonthYear", "$maturityMonthYear"));
        group.put("id", new JsonObject().put("$last", "$_id"));
        group.put("clearer", new JsonObject().put("$last", "$clearer"));
        group.put("member", new JsonObject().put("$last", "$member"));
        group.put("account", new JsonObject().put("$last", "$account"));
        group.put("clss", new JsonObject().put("$last", "$clss"));
        group.put("reqId", new JsonObject().put("$last", "$reqId"));
        group.put("rptId", new JsonObject().put("$last", "$rptId"));
        group.put("bizDt", new JsonObject().put("$last", "$bizDt"));
        group.put("lastReportRequested", new JsonObject().put("$last", "$lastReportRequested"));
        group.put("sesId", new JsonObject().put("$last", "$sesId"));
        group.put("symbol", new JsonObject().put("$last", "$symbol"));
        group.put("putCall", new JsonObject().put("$last", "$putCall"));
        group.put("maturityMonthYear", new JsonObject().put("$last", "$maturityMonthYear"));
        group.put("strikePrice", new JsonObject().put("$last", "$strikePrice"));
        group.put("optAttribute", new JsonObject().put("$last", "$optAttribute"));
        group.put("crossMarginLongQty", new JsonObject().put("$last", "$crossMarginLongQty"));
        group.put("crossMarginShortQty", new JsonObject().put("$last", "$crossMarginShortQty"));
        group.put("optionExcerciseQty", new JsonObject().put("$last", "$optionExcerciseQty"));
        group.put("optionAssignmentQty", new JsonObject().put("$last", "$optionAssignmentQty"));
        group.put("allocationTradeQty", new JsonObject().put("$last", "$allocationTradeQty"));
        group.put("deliveryNoticeQty", new JsonObject().put("$last", "$deliveryNoticeQty"));
        group.put("clearingCcy", new JsonObject().put("$last", "$clearingCcy"));
        group.put("mVar", new JsonObject().put("$last", "$mVar"));
        group.put("compVar", new JsonObject().put("$last", "$compVar"));
        group.put("compCorrelationBreak", new JsonObject().put("$last", "$compCorrelationBreak"));
        group.put("compCompressionError", new JsonObject().put("$last", "$compCompressionError"));
        group.put("compLiquidityAddOn", new JsonObject().put("$last", "$compLiquidityAddOn"));
        group.put("compLongOptionCredit", new JsonObject().put("$last", "$compLongOptionCredit"));
        group.put("productCcy", new JsonObject().put("$last", "$productCcy"));
        group.put("variationMarginPremiumPayment", new JsonObject().put("$last", "$variationMarginPremiumPayment"));
        group.put("premiumMargin", new JsonObject().put("$last", "$premiumMargin"));
        group.put("delta", new JsonObject().put("$last", "$delta"));
        group.put("gamma", new JsonObject().put("$last", "$gamma"));
        group.put("vega", new JsonObject().put("$last", "$vega"));
        group.put("rho", new JsonObject().put("$last", "$rho"));
        group.put("theta", new JsonObject().put("$last", "$theta"));
        group.put("received", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received"))));

        return group;
    }

    protected JsonObject getProject()
    {
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
        project.put("received", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received")));

        return project;
    }
}
