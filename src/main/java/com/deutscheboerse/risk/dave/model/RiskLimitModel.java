package com.deutscheboerse.risk.dave.model;

import io.vertx.core.json.JsonObject;

/**
 * Created by schojak on 15.9.16.
 */
public class RiskLimitModel extends AbstractModel {
    private static final String MONGO_COLLECTION = "ers.RiskLimit";
    private static final AbstractModel INSTANCE = new RiskLimitModel();

    protected RiskLimitModel() {
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
        group.put("_id", new JsonObject()
                .put("clearer", "$clearer").put("member", "$member").put("maintainer", "$maintainer")
                .put("limitType", "$limitType"));
        group.put("id", new JsonObject().put("$last", "$_id"));
        group.put("clearer", new JsonObject().put("$last", "$clearer"));
        group.put("member", new JsonObject().put("$last", "$member"));
        group.put("maintainer", new JsonObject().put("$last", "$maintainer"));
        group.put("reqId", new JsonObject().put("$last", "$reqId"));
        group.put("rptId", new JsonObject().put("$last", "$rptId"));
        group.put("txnTm", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$txnTm"))));
        group.put("reqRslt", new JsonObject().put("$last", "$reqRslt"));
        group.put("txt", new JsonObject().put("$last", "$txt"));
        group.put("limitType", new JsonObject().put("$last", "$limitType"));
        group.put("utilization", new JsonObject().put("$last", "$utilization"));
        group.put("warningLevel", new JsonObject().put("$last", "$warningLevel"));
        group.put("throttleLevel", new JsonObject().put("$last", "$throttleLevel"));
        group.put("rejectLevel", new JsonObject().put("$last", "$rejectLevel"));
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
