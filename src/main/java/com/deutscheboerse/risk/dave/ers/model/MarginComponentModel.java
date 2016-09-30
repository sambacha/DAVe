package com.deutscheboerse.risk.dave.ers.model;

import io.vertx.core.json.JsonObject;

/**
 * Created by schojak on 15.9.16.
 */
public class MarginComponentModel extends AbstractModel {
    private static final String MONGO_COLLECTION = "ers.MarginComponent";
    private static final AbstractModel INSTANCE = new MarginComponentModel();

    protected MarginComponentModel() {
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
        group.put("_id", new JsonObject().put("clearer", "$clearer").put("member", "$member").put("account", "$account").put("clss", "$clss").put("ccy", "$ccy"));
        group.put("id", new JsonObject().put("$last", "$_id"));
        group.put("clearer", new JsonObject().put("$last", "$clearer"));
        group.put("member", new JsonObject().put("$last", "$member"));
        group.put("account", new JsonObject().put("$last", "$account"));
        group.put("clss", new JsonObject().put("$last", "$clss"));
        group.put("ccy", new JsonObject().put("$last", "$ccy"));
        group.put("txnTm", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$txnTm"))));
        group.put("bizDt", new JsonObject().put("$last", "$bizDt"));
        group.put("reqId", new JsonObject().put("$last", "$reqId"));
        group.put("rptId", new JsonObject().put("$last", "$rptId"));
        group.put("sesId", new JsonObject().put("$last", "$sesId"));
        group.put("variationMargin", new JsonObject().put("$last", "$variationMargin"));
        group.put("premiumMargin", new JsonObject().put("$last", "$premiumMargin"));
        group.put("liquiMargin", new JsonObject().put("$last", "$liquiMargin"));
        group.put("spreadMargin", new JsonObject().put("$last", "$spreadMargin"));
        group.put("additionalMargin", new JsonObject().put("$last", "$additionalMargin"));
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
        project.put("clss", 1);
        project.put("ccy", 1);
        project.put("txnTm", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$txnTm")));
        project.put("bizDt", 1);
        project.put("reqId", 1);
        project.put("rptId", 1);
        project.put("sesId", 1);
        project.put("variationMargin", 1);
        project.put("premiumMargin", 1);
        project.put("liquiMargin", 1);
        project.put("spreadMargin", 1);
        project.put("additionalMargin", 1);
        project.put("received",new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received")));

        return project;
    }
}
