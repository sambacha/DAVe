package com.deutscheboerse.risk.dave.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by schojak on 15.9.16.
 */
public class TradingSessionStatusModel extends AbstractModel {
    private final String mongoCollection = "ers.TradingSessionStatus";

    public JsonObject getLatestCommand(JsonObject params)
    {
        return getCommand(mongoCollection, getLatestPipeline());
    }

    public JsonObject getHistoryCommand(JsonObject params)
    {
        return getCommand(mongoCollection, getHistoryPipeline());
    }

    private JsonArray getLatestPipeline()
    {
        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$sort", getSort()));
        pipeline.add(new JsonObject().put("$group", getGroup()));

        return pipeline;
    }

    private JsonArray getHistoryPipeline()
    {
        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$sort", getSort()));
        pipeline.add(new JsonObject().put("$project", getProject()));

        return pipeline;
    }

    protected JsonObject getGroup()
    {
        JsonObject group = new JsonObject();
        group.put("_id", new JsonObject().put("sesId", "$sesId"));
        group.put("id", new JsonObject().put("$last", "$_id"));
        group.put("reqId", new JsonObject().put("$last", "$reqId"));
        group.put("sesId", new JsonObject().put("$last", "$sesId"));
        group.put("stat", new JsonObject().put("$last", "$stat"));
        group.put("statRejRsn", new JsonObject().put("$last", "$statRejRsn"));
        group.put("txt", new JsonObject().put("$last", "$txt"));
        group.put("received", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received"))));

        return group;
    }

    protected JsonObject getProject()
    {
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
