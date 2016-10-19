package com.deutscheboerse.risk.dave.ers.model;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
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
    public JsonObject queryLatestDocument(Message<?> msg)
    {
        JsonObject message = (JsonObject)msg.body();
        JsonObject query = new JsonObject();
        query.put("sesId", message.getValue("sesId"));
        return query;
    }

    @Override
    public JsonObject makeLatestDocument(Message<?> msg)
    {
        JsonObject message = (JsonObject)msg.body();
        JsonObject document = new JsonObject();
        document.put("reqId", message.getValue("reqId"));
        document.put("sesId", message.getValue("sesId"));
        document.put("stat", message.getValue("stat"));
        document.put("statRejRsn", message.getValue("statRejRsn"));
        document.put("txt", message.getValue("txt"));
        document.put("received", message.getJsonObject("received").getString("$date"));
        return document;
    }

    private JsonArray getHistoryPipeline()
    {
        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$project", getProject()));

        return pipeline;
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
