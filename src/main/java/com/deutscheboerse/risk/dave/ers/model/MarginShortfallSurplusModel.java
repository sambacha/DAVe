package com.deutscheboerse.risk.dave.ers.model;

import io.vertx.core.eventbus.Message;
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
    public JsonObject queryLatestDocument(Message<?> msg) {
        JsonObject message = (JsonObject)msg.body();
        JsonObject query = new JsonObject();
        query.put("clearer", message.getValue("clearer"));
        query.put("pool", message.getValue("pool"));
        query.put("member", message.getValue("member"));
        query.put("clearingCcy", message.getValue("clearingCcy"));
        query.put("ccy", message.getValue("ccy"));
        return query;
    }

    @Override
    public JsonObject makeLatestDocument(Message<?> msg) {
        JsonObject message = (JsonObject)msg.body();
        JsonObject document = new JsonObject();
        document.put("clearer", message.getValue("clearer"));
        document.put("pool", message.getValue("pool"));
        document.put("poolType", message.getValue("poolType"));
        document.put("member", message.getValue("member"));
        document.put("clearingCcy", message.getValue("clearingCcy"));
        document.put("ccy", message.getValue("ccy"));
        document.put("txnTm", message.getJsonObject("txnTm").getString("$date"));
        document.put("bizDt", message.getValue("bizDt"));
        document.put("reqId", message.getValue("reqId"));
        document.put("rptId", message.getValue("rptId"));
        document.put("sesId", message.getValue("sesId"));
        document.put("marginRequirement", message.getValue("marginRequirement"));
        document.put("securityCollateral", message.getValue("securityCollateral"));
        document.put("cashBalance", message.getValue("cashBalance"));
        document.put("shortfallSurplus", message.getValue("shortfallSurplus"));
        document.put("marginCall", message.getValue("marginCall"));
        document.put("received", message.getJsonObject("received").getString("$date"));
        return document;
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
