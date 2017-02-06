package com.deutscheboerse.risk.dave.model;

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
