package com.deutscheboerse.risk.dave.ers.model;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * Created by schojak on 15.9.16.
 */
public class MarginComponentModel extends AbstractModel {
    private static final String MONGO_HISTORY_COLLECTION = "ers.MarginComponent";
    private static final String MONGO_LATEST_COLLECTION = "ers.MarginComponent.latest";

    public MarginComponentModel() {
        super(MONGO_HISTORY_COLLECTION, MONGO_LATEST_COLLECTION);
    }

    @Override
    protected JsonObject getProject() {
        JsonObject project = new JsonObject();
        project.put("_id", 0);
        project.put("id", "$_id");
        project.put("clearer", 1);
        project.put("member", 1);
        project.put("account", 1);
        project.put("clss", 1);
        project.put("ccy", 1);
        project.put("txnTm", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$txnTm")));
        project.put("bizDt", 1);
        project.put("reqId", 1);
        project.put("rptId", 1);
        project.put("sesId", 1);
        project.put("variationMargin", 1);
        project.put("premiumMargin", 1);
        project.put("liquiMargin", 1);
        project.put("spreadMargin", 1);
        project.put("additionalMargin", 1);
        project.put("received",new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received")));
        return project;
    }
}
