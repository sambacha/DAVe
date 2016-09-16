package com.deutscheboerse.risk.dave.ers.model;

import io.vertx.core.json.JsonObject;

/**
 * Created by schojak on 15.9.16.
 */
public class MarginShortfallSurplusModel extends AbstractModel {
    private static final String MONGO_COLLECTION = "ers.MarginShortfallSurplus";
    private static final AbstractModel INSTANCE = new MarginShortfallSurplusModel();

    protected MarginShortfallSurplusModel() {
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
        group.put("_id", new JsonObject().put("clearer", "$clearer").put("pool", "$pool").put("member", "$member").put("clearingCcy", "$clearingCcy").put("ccy", "$ccy"));
        group.put("id", new JsonObject().put("$last", "$_id"));
        group.put("clearer", new JsonObject().put("$last", "$clearer"));
        group.put("pool", new JsonObject().put("$last", "$pool"));
        group.put("poolType", new JsonObject().put("$last", "$poolType"));
        group.put("member", new JsonObject().put("$last", "$member"));
        group.put("clearingCcy", new JsonObject().put("$last", "$clearingCcy"));
        group.put("ccy", new JsonObject().put("$last", "$ccy"));
        group.put("txnTm", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$txnTm"))));
        group.put("bizDt", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoDayFormat).put("date", "$bizDt"))));
        group.put("reqId", new JsonObject().put("$last", "$reqId"));
        group.put("rptId", new JsonObject().put("$last", "$rptId"));
        group.put("sesId", new JsonObject().put("$last", "$sesId"));
        group.put("marginRequirement", new JsonObject().put("$last", "$marginRequirement"));
        group.put("securityCollateral", new JsonObject().put("$last", "$securityCollateral"));
        group.put("cashBalance", new JsonObject().put("$last", "$cashBalance"));
        group.put("shortfallSurplus", new JsonObject().put("$last", "$shortfallSurplus"));
        group.put("marginCall", new JsonObject().put("$last", "$marginCall"));
        group.put("received", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received"))));

        return group;
    }

    protected JsonObject getProject()
    {
        JsonObject project = new JsonObject();
        project.put("_id", 0);
        project.put("id", "$_id");
        project.put("clearer", 1);
        project.put("pool", 1);
        project.put("poolType", 1);
        project.put("member", 1);
        project.put("clearingCcy", 1);
        project.put("ccy", 1);
        project.put("txnTm", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$txnTm")));
        project.put("bizDt", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoDayFormat).put("date", "$bizDt")));
        project.put("reqId", 1);
        project.put("rptId", 1);
        project.put("sesId", 1);
        project.put("marginRequirement", 1);
        project.put("securityCollateral", 1);
        project.put("cashBalance", 1);
        project.put("shortfallSurplus", 1);
        project.put("marginCall", 1);
        project.put("received", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received")));

        return project;
    }
}
