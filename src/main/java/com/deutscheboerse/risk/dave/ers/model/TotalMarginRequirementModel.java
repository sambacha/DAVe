package com.deutscheboerse.risk.dave.ers.model;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * Created by schojak on 15.9.16.
 */
public class TotalMarginRequirementModel extends AbstractModel {
    private static final String MONGO_HISTORY_COLLECTION = "ers.TotalMarginRequirement";
    private static final String MONGO_LATEST_COLLECTION = "ers.TotalMarginRequirement.latest";

    public TotalMarginRequirementModel() {
        super(MONGO_HISTORY_COLLECTION, MONGO_LATEST_COLLECTION);
    }

    @Override
    public JsonObject queryLatestDocument(Message<?> msg) {
        JsonObject message = (JsonObject)msg.body();
        JsonObject query = new JsonObject();
        query.put("clearer", message.getString("clearer"));
        query.put("pool", message.getString("pool"));
        query.put("member", message.getString("member"));
        query.put("account", message.getString("account"));
        query.put("ccy", message.getString("ccy"));
        return query;
    }

    @Override
    public JsonObject makeLatestDocument(Message<?> msg) {
        JsonObject message = (JsonObject)msg.body();
        JsonObject document = new JsonObject();
        document.put("clearer", message.getValue("clearer"));
        document.put("pool", message.getValue("pool"));
        document.put("member", message.getValue("member"));
        document.put("account", message.getValue("account"));
        document.put("ccy", message.getValue("ccy"));
        document.put("txnTm", message.getJsonObject("txnTm").getString("$date"));
        document.put("bizDt", message.getValue("bizDt"));
        document.put("reqId", message.getValue("reqId"));
        document.put("rptId", message.getValue("rptId"));
        document.put("sesId", message.getValue("sesId"));
        document.put("unadjustedMargin", message.getValue("unadjustedMargin"));
        document.put("adjustedMargin", message.getValue("adjustedMargin"));
        document.put("received", message.getJsonObject("received").getString("$date"));
        return document;
    }

    @Override
    protected JsonObject getProject() {
        JsonObject project = new JsonObject();
        project.put("_id", 0);
        project.put("id", "$_id");
        project.put("clearer", 1);
        project.put("pool", 1);
        project.put("member", 1);
        project.put("account", 1);
        project.put("ccy", 1);
        project.put("txnTm", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$txnTm")));
        project.put("bizDt", 1);
        project.put("reqId", 1);
        project.put("rptId", 1);
        project.put("sesId", 1);
        project.put("unadjustedMargin", 1);
        project.put("adjustedMargin", 1);
        project.put("received", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received")));
        return project;
    }
}
