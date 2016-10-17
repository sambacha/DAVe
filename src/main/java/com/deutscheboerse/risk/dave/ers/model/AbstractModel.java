package com.deutscheboerse.risk.dave.ers.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by schojak on 15.9.16.
 */
public abstract class AbstractModel {

    private static final Integer DEFAULT_PAGE_SIZE = 10;
    final String mongoTimestampFormat = "%Y-%m-%dT%H:%M:%S.%LZ";

    protected abstract JsonObject getGroup();
    protected abstract JsonObject getProject();

    protected JsonObject getCommand(String mongoCollection, JsonArray pipeline)
    {
        JsonObject command = new JsonObject()
                .put("aggregate", mongoCollection)
                .put("pipeline", pipeline)
                .put("allowDiskUse", true);

        return command;
    }

    protected JsonArray getLatestPipeline(JsonObject params)
    {
        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$match", params.getJsonObject("match")));
        pipeline.add(new JsonObject().put("$match", params.getJsonObject("filter")));
        pipeline.add(new JsonObject().put("$group", getGroup()));
        pipeline.add(new JsonObject().put("$sort", params.getJsonObject("sort")));
        if (params.getBoolean("countOnly", Boolean.FALSE)) {
            pipeline.add(new JsonObject().put("$group", new JsonObject().put("_id", "null").put("count", new JsonObject().put("$sum", 1))));
        }
        pipeline.add(new JsonObject().put("$skip", params.getInteger("pageSize", AbstractModel.DEFAULT_PAGE_SIZE) * (params.getInteger("page", 1) - 1)));
        pipeline.add(new JsonObject().put("$limit", params.getInteger("pageSize", AbstractModel.DEFAULT_PAGE_SIZE)));

        return pipeline;
    }

    protected JsonArray getHistoryPipeline(JsonObject params)
    {
        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$match", params.getJsonObject("match")));
        pipeline.add(new JsonObject().put("$project", getProject()));

        return pipeline;
    }
}
