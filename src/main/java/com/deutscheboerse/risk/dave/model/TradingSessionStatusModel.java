package com.deutscheboerse.risk.dave.model;

import io.vertx.core.json.JsonObject;

/**
 * Created by schojak on 15.9.16.
 */
public class TradingSessionStatusModel extends AbstractModel {
    private static final String MONGO_HISTORY_COLLECTION = "ers.TradingSessionStatus";
    private static final String MONGO_LATEST_COLLECTION = "ers.TradingSessionStatus.latest";

    public TradingSessionStatusModel() {
        super(MONGO_HISTORY_COLLECTION, MONGO_LATEST_COLLECTION);
    }

    @Override
    protected JsonObject getProject() {
        JsonObject project = new JsonObject();
        project.put("_id", 0);
        project.put("id", "$_id");
        project.put("reqId", 1);
        project.put("sesId", 1);
        project.put("stat", 1);
        project.put("statRejRsn", 1);
        project.put("txt", 1);
        project.put("received", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received")));
        return project;
    }
}
