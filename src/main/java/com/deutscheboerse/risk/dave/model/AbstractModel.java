package com.deutscheboerse.risk.dave.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by schojak on 15.9.16.
 */
public abstract class AbstractModel {
    final String mongoTimestampFormat = "%Y-%m-%dT%H:%M:%S.%L";
    final String mongoDayFormat = "%Y-%m-%d";

    protected abstract JsonObject getGroup();
    protected abstract JsonObject getProject();

    protected JsonObject getSort()
    {
        JsonObject sort = new JsonObject();
        sort.put("received", 1);

        return sort;
    }

    protected JsonObject getCommand(String mongoCollection, JsonArray pipeline)
    {
        JsonObject command = new JsonObject()
                .put("aggregate", mongoCollection)
                .put("pipeline", pipeline);

        return command;
    }

    protected JsonArray getLatestPipeline(JsonObject params)
    {
        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$sort", getSort()));
        pipeline.add(new JsonObject().put("$match", params));
        pipeline.add(new JsonObject().put("$group", getGroup()));

        return pipeline;
    }

    protected JsonArray getHistoryPipeline(JsonObject params)
    {
        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$sort", getSort()));
        pipeline.add(new JsonObject().put("$match", params));
        pipeline.add(new JsonObject().put("$project", getProject()));

        return pipeline;
    }
}
