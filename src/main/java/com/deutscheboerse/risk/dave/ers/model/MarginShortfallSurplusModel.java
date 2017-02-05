package com.deutscheboerse.risk.dave.ers.model;

import io.vertx.core.json.JsonObject;

/**
 * Created by schojak on 15.9.16.
 */
public class MarginShortfallSurplusModel extends AbstractModel {
    private static final String MONGO_HISTORY_COLLECTION = "ers.MarginShortfallSurplus";
    private static final String MONGO_LATEST_COLLECTION = "ers.MarginShortfallSurplus.latest";

    public MarginShortfallSurplusModel() {
        super(MONGO_HISTORY_COLLECTION, MONGO_LATEST_COLLECTION);
    }

    @Override
    protected JsonObject getProject() {
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
        project.put("bizDt", 1);
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
