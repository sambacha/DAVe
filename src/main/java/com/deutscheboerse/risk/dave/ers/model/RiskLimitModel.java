package com.deutscheboerse.risk.dave.ers.model;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * Created by schojak on 15.9.16.
 */
public class RiskLimitModel extends AbstractModel {
    private static final String MONGO_HISTORY_COLLECTION = "ers.RiskLimit";
    private static final String MONGO_LATEST_COLLECTION = "ers.RiskLimit.latest";

    public RiskLimitModel() {
        super(MONGO_HISTORY_COLLECTION, MONGO_LATEST_COLLECTION);
    }

    @Override
    public JsonObject queryLatestDocument(Message<?> msg) {
        JsonObject message = (JsonObject)msg.body();
        JsonObject query = new JsonObject();
        query.put("clearer", message.getValue("clearer"));
        query.put("member", message.getValue("member"));
        query.put("maintainer", message.getValue("maintainer"));
        query.put("limitType", message.getValue("limitType"));
        return query;
    }

    @Override
    public JsonObject makeLatestDocument(Message<?> msg) {
        JsonObject message = (JsonObject)msg.body();
        JsonObject document = new JsonObject();
        document.put("clearer", message.getValue("clearer"));
        document.put("member", message.getValue("member"));
        document.put("maintainer", message.getValue("maintainer"));
        document.put("reqId", message.getValue("reqId"));
        document.put("rptId", message.getValue("rptId"));
        document.put("txnTm", message.getJsonObject("txnTm").getString("$date"));
        document.put("reqRslt", message.getValue("reqRslt"));
        document.put("txt", message.getValue("txt"));
        document.put("limitType", message.getValue("limitType"));
        document.put("utilization", message.getValue("utilization"));
        document.put("warningLevel", message.getValue("warningLevel"));
        document.put("throttleLevel", message.getValue("throttleLevel"));
        document.put("rejectLevel", message.getValue("rejectLevel"));
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
        project.put("maintainer", 1);
        project.put("reqId", 1);
        project.put("rptId", 1);
        project.put("txnTm", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$txnTm")));
        project.put("reqRslt", 1);
        project.put("txt", 1);
        project.put("limitType", 1);
        project.put("utilization", 1);
        project.put("warningLevel", 1);
        project.put("throttleLevel", 1);
        project.put("rejectLevel", 1);
        project.put("received", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received")));
        return project;
    }
}
