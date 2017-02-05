package com.deutscheboerse.risk.dave.ers.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by schojak on 15.9.16.
 */
public abstract class AbstractModel {
    protected final String mongoTimestampFormat = "%Y-%m-%dT%H:%M:%S.%LZ";
    private final String mongoHistoryCollection;
    private final String mongoLatestCollection;

    protected abstract JsonObject getProject();

    protected AbstractModel(String historyCollection, String latestCollection) {
        this.mongoHistoryCollection = historyCollection;
        this.mongoLatestCollection = latestCollection;
    }

    public String getHistoryCollection() {
        return this.mongoHistoryCollection;
    }

    public String getLatestCollection() {
        return this.mongoLatestCollection;
    }

    public JsonObject getHistoryCommand(JsonObject params) {
        JsonObject command = new JsonObject()
                .put("aggregate", getHistoryCollection())
                .put("pipeline", getHistoryPipeline(params))
                .put("allowDiskUse", true);

        return command;
    }

    protected JsonArray getHistoryPipeline(JsonObject params)
    {
        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$match", params));
        pipeline.add(new JsonObject().put("$project", getProject()));

        return pipeline;
    }
}
