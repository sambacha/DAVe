package com.opnfi.risk;

import com.opnfi.risk.model.MarginComponent;
import com.opnfi.risk.model.MarginShortfallSurplus;
import com.opnfi.risk.model.TotalMarginRequirement;
import com.opnfi.risk.model.TradingSessionStatus;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by schojak on 19.8.16.
 */
public class MongoDBPersistenceVerticle extends AbstractVerticle {
    final static private Logger LOG = LoggerFactory.getLogger(MongoDBPersistenceVerticle.class);

    private static final String DEFAULT_DB_NAME = "OpnFi-Risk";
    private static final String DEFAULT_CONNECTION_STRING = "mongodb://localhost:27017";

    private MongoClient mongo;
    final DateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    final String mongoTimestampFormat = "%Y-%m-%dT%H:%M:%S.%L";
    final String mongoDayFormat = "%Y-%m-%d";

    @Override
    public void start(Future<Void> fut) throws Exception {
        LOG.info("Starting {} with configuration: {}", MongoDBPersistenceVerticle.class.getSimpleName(), config().encodePrettily());

        Future<String> chainFuture = Future.future();

        Future<Void> connectDbFuture = Future.future();
        connectDb(connectDbFuture.completer());

        connectDbFuture.compose(v -> {
            LOG.info("Connected to MongoDB");
            Future<Void> initDbFuture = Future.future();
            initDb(initDbFuture.completer());
            return initDbFuture;
        }).compose(v -> {
            LOG.info("Initialized MongoDB");
            Future<Void> startEventBusFuture = Future.future();
            startEventBus(startEventBusFuture.completer());
            return startEventBusFuture;
        }).compose(v -> {
            LOG.info("Event bus started and subscribed");
            chainFuture.complete();
        }, chainFuture);

        chainFuture.setHandler(ar -> {
            if (ar.succeeded()) {
                LOG.info("MongoDB verticle started");
                fut.complete();
            } else {
                LOG.error("MongoDB verticle failed to deploy", chainFuture.cause());
                fut.fail(chainFuture.cause());
            }
        });
    }

    private void connectDb(Handler<AsyncResult<Void>> completer) {
        JsonObject config = new JsonObject();
        config.put("db_name", config().getString("db_name", MongoDBPersistenceVerticle.DEFAULT_DB_NAME));
        config.put("useObjectId", true);
        config.put("connection_string", config().getString("connection_string", MongoDBPersistenceVerticle.DEFAULT_CONNECTION_STRING));

        mongo = MongoClient.createShared(vertx, config);
        completer.handle(Future.succeededFuture());
    }

    private void initDb(Handler<AsyncResult<Void>> completer) {
        mongo.getCollections(res -> {
            if (res.succeeded()) {
                List<String> mongoCollections = res.result();
                List<String> neededCollections = new ArrayList<>(Arrays.asList(
                        "ers.TradingSessionStatus",
                        "ers.MarginComponent",
                        "ers.TotalMarginRequirement",
                        "ers.MarginShortfallSurplus"
                ));

                List<Future> futs = new ArrayList<>();

                neededCollections.stream()
                        .filter(collection -> ! mongoCollections.contains(collection))
                        .forEach(collection -> {
                            LOG.info("Collection {} is missing and will be added", collection);
                            Future<Void> fut = Future.future();
                            mongo.createCollection(collection, fut.completer());
                            futs.add(fut);
                        });

                CompositeFuture.all(futs).setHandler(ar -> {
                    if (ar.succeeded())
                    {
                        LOG.info("Mongo has all needed collections for ERS");
                        completer.handle(Future.succeededFuture());
                    }
                    else
                    {
                        LOG.error("Failed to add all collections needed for ERS to Mongo", ar.cause());
                        completer.handle(Future.failedFuture(ar.cause()));
                    }
                });
            } else {
                LOG.error("Failed to get collection list", res.cause());
                completer.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    private void startEventBus(Handler<AsyncResult<Void>> completer)
    {
        EventBus eb = vertx.eventBus();

        // Camel consumers
        eb.consumer("ers.TradingSessionStatus", message -> storeTradingSessionStatus(message));
        eb.consumer("ers.MarginComponent", message -> storeMarginComponent(message));
        eb.consumer("ers.TotalMarginRequirement", message -> storeTotalMarginRequirement(message));
        eb.consumer("ers.MarginShortfallSurplus", message -> storeMarginShortfallSurplus(message));

        // Query endpoints
        eb.consumer("query.latestTradingSessionStatus", message -> queryLatestTradingSessionStatus(message));
        eb.consumer("query.historyTradingSessionStatus", message -> queryHistoryTradingSessionStatus(message));
        eb.consumer("query.latestMarginComponent", message -> queryLatestMarginComponent(message));
        eb.consumer("query.historyMarginComponent", message -> queryHistoryMarginComponent(message));
        eb.consumer("query.latestTotalMarginRequirement", message -> queryLatestTotalMarginRequirement(message));
        eb.consumer("query.historyTotalMarginRequirement", message -> queryHistoryTotalMarginRequirement(message));
        eb.consumer("query.latestMarginShortfallSurplus", message -> queryLatestMarginShortfallSurplus(message));
        eb.consumer("query.historyMarginShortfallSurplus", message -> queryHistoryMarginShortfallSurplus(message));

        completer.handle(Future.succeededFuture());
    }

    private void storeTradingSessionStatus(Message msg)
    {
        LOG.trace("Storing TradingSessionStatus message with body: " + msg.body().toString());
        TradingSessionStatus tss = Json.decodeValue(msg.body().toString(), TradingSessionStatus.class);

        JsonObject jsonTss = new JsonObject((String)msg.body());

        if (tss.getReceived() != null) {
            jsonTss.put("received", new JsonObject().put("$date", timestampFormatter.format(tss.getReceived())));
        }

        mongo.insert("ers.TradingSessionStatus", jsonTss, res -> {
           if (res.succeeded())
           {
               LOG.trace("Stored TradingSessionStatus into DB");
           }
           else
           {
               LOG.error("Failed to store TradingSessionStatus into DB " + res.cause());
           }
        });
    }

    private void storeMarginComponent(Message msg)
    {
        LOG.trace("Storing MC message with body: " + msg.body().toString());
        MarginComponent mc = Json.decodeValue(msg.body().toString(), MarginComponent.class);

        JsonObject jsonMc = new JsonObject((String)msg.body());

        if (mc.getBizDt() != null) {
            jsonMc.put("bizDt", new JsonObject().put("$date", timestampFormatter.format(mc.getBizDt())));
        }

        if (mc.getTxnTm() != null) {
            jsonMc.put("txnTm", new JsonObject().put("$date", timestampFormatter.format(mc.getTxnTm())));
        }

        if (mc.getReceived() != null) {
            jsonMc.put("received", new JsonObject().put("$date", timestampFormatter.format(mc.getReceived())));
        }

        mongo.insert("ers.MarginComponent", jsonMc, res -> {
            if (res.succeeded())
            {
                LOG.trace("Stored MarginComponent into DB");
            }
            else
            {
                LOG.error("Failed to store MarginComponent into DB " + res.cause());
            }
        });
    }

    private void storeTotalMarginRequirement(Message msg)
    {
        LOG.trace("Storing TMR message with body: " + msg.body().toString());
        TotalMarginRequirement tmr = Json.decodeValue(msg.body().toString(), TotalMarginRequirement.class);

        JsonObject jsonTmr = new JsonObject((String)msg.body());

        if (tmr.getBizDt() != null) {
            jsonTmr.put("bizDt", new JsonObject().put("$date", timestampFormatter.format(tmr.getBizDt())));
        }

        if (tmr.getTxnTm() != null) {
            jsonTmr.put("txnTm", new JsonObject().put("$date", timestampFormatter.format(tmr.getTxnTm())));
        }

        if (tmr.getReceived() != null) {
            jsonTmr.put("received", new JsonObject().put("$date", timestampFormatter.format(tmr.getReceived())));
        }

        mongo.insert("ers.TotalMarginRequirement", jsonTmr, res -> {
            if (res.succeeded())
            {
                LOG.trace("Stored TotalMarginRequirement into DB");
            }
            else
            {
                LOG.error("Failed to store TotalMarginRequirement into DB " + res.cause());
            }
        });
    }

    private void storeMarginShortfallSurplus(Message msg)
    {
        LOG.trace("Storing MarginShortfallSurplus message with body: " + msg.body().toString());
        MarginShortfallSurplus mss = Json.decodeValue(msg.body().toString(), MarginShortfallSurplus.class);

        JsonObject jsonMss = new JsonObject((String)msg.body());

        if (mss.getBizDt() != null) {
            jsonMss.put("bizDt", new JsonObject().put("$date", timestampFormatter.format(mss.getBizDt())));
        }

        if (mss.getTxnTm() != null) {
            jsonMss.put("txnTm", new JsonObject().put("$date", timestampFormatter.format(mss.getTxnTm())));
        }

        if (mss.getReceived() != null) {
            jsonMss.put("received", new JsonObject().put("$date", timestampFormatter.format(mss.getReceived())));
        }

        mongo.insert("ers.MarginShortfallSurplus", jsonMss, res -> {
            if (res.succeeded())
            {
                LOG.trace("Stored MarginShortfallSurplus into DB");
            }
            else
            {
                LOG.error("Failed to store MarginShortfallSurplus into DB " + res.cause());
            }
        });
    }

    private void queryLatestTradingSessionStatus(Message msg)
    {
        LOG.trace("Received latest/tss query");

        JsonObject sort = new JsonObject();
        sort.put("received", 1);

        JsonObject group = new JsonObject();
        group.put("_id", new JsonObject().put("sesId", "$sesId"));
        group.put("id", new JsonObject().put("$last", "$_id"));
        group.put("reqId", new JsonObject().put("$last", "$reqId"));
        group.put("sesId", new JsonObject().put("$last", "$sesId"));
        group.put("stat", new JsonObject().put("$last", "$stat"));
        group.put("statRejRsn", new JsonObject().put("$last", "$statRejRsn"));
        group.put("txt", new JsonObject().put("$last", "$txt"));
        group.put("received", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received"))));

        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$sort", sort));
        pipeline.add(new JsonObject().put("$group", group));

        JsonObject command = new JsonObject()
                .put("aggregate", "ers.TradingSessionStatus")
                .put("pipeline", pipeline);

        mongo.runCommand("aggregate", command, res -> {
            if (res.succeeded()) {
                if (res.result().getJsonArray("result").size() > 0) {
                    msg.reply(Json.encodePrettily(res.result().getJsonArray("result").getJsonObject(0)));
                }
                else
                {
                    msg.reply(Json.encodePrettily(new JsonObject()));
                }
            } else {
                LOG.error("latest/tss query failed", res.cause());
            }
        });
    }

    private void queryHistoryTradingSessionStatus(Message msg)
    {
        LOG.trace("Received history/tss query");

        JsonObject sort = new JsonObject();
        sort.put("received", 1);

        JsonObject project = new JsonObject();
        project.put("_id", 0);
        project.put("id", "$_id");
        project.put("reqId", 1);
        project.put("sesId", 1);
        project.put("stat", 1);
        project.put("statRejRsn", 1);
        project.put("txt", 1);
        project.put("received", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received")));

        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$sort", sort));
        pipeline.add(new JsonObject().put("$project", project));

        JsonObject command = new JsonObject()
                .put("aggregate", "ers.TradingSessionStatus")
                .put("pipeline", pipeline);

        mongo.runCommand("aggregate", command, res -> {
            if (res.succeeded()) {
                msg.reply(Json.encodePrettily(res.result().getJsonArray("result")));
            } else {
                LOG.error("history/tss query failed", res.cause());
            }
        });
    }

    private void queryLatestMarginComponent(Message msg)
    {
        JsonObject params = (JsonObject)msg.body();
        LOG.trace("Received latest/mc query with parameters " + params);

        JsonObject sort = new JsonObject();
        sort.put("received", 1);

        JsonObject group = new JsonObject();
        group.put("_id", new JsonObject().put("clearer", "$clearer").put("member", "$member").put("account", "$account").put("clss", "$clss").put("ccy", "$ccy"));
        group.put("id", new JsonObject().put("$last", "$_id"));
        group.put("clearer", new JsonObject().put("$last", "$clearer"));
        group.put("member", new JsonObject().put("$last", "$member"));
        group.put("account", new JsonObject().put("$last", "$account"));
        group.put("clss", new JsonObject().put("$last", "$clss"));
        group.put("ccy", new JsonObject().put("$last", "$ccy"));
        group.put("txnTm", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$txnTm"))));
        group.put("bizDt", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoDayFormat).put("date", "$bizDt"))));
        group.put("reqId", new JsonObject().put("$last", "$reqId"));
        group.put("rptId", new JsonObject().put("$last", "$rptId"));
        group.put("sesId", new JsonObject().put("$last", "$sesId"));
        group.put("variationMargin", new JsonObject().put("$last", "$variationMargin"));
        group.put("premiumMargin", new JsonObject().put("$last", "$premiumMargin"));
        group.put("liquiMargin", new JsonObject().put("$last", "$liquiMargin"));
        group.put("spreadMargin", new JsonObject().put("$last", "$spreadMargin"));
        group.put("additionalMargin", new JsonObject().put("$last", "$additionalMargin"));
        group.put("received", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received"))));

        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$sort", sort));
        pipeline.add(new JsonObject().put("$match", params));
        pipeline.add(new JsonObject().put("$group", group));

        JsonObject command = new JsonObject()
                .put("aggregate", "ers.MarginComponent")
                .put("pipeline", pipeline);

        mongo.runCommand("aggregate", command, res -> {
            if (res.succeeded()) {
                msg.reply(Json.encodePrettily(res.result().getJsonArray("result")));
            } else {
                LOG.error("latest/mc query failed", res.cause());
            }
        });
    }

    private void queryHistoryMarginComponent(Message msg)
    {
        JsonObject params = (JsonObject)msg.body();
        LOG.trace("Received history/mc query with parameters " + params);

        JsonObject sort = new JsonObject();
        sort.put("received", 1);

        JsonObject project = new JsonObject();
        project.put("_id", 0);
        project.put("id", "$_id");
        project.put("clearer", 1);
        project.put("member", 1);
        project.put("account", 1);
        project.put("clss", 1);
        project.put("ccy", 1);
        project.put("txnTm", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$txnTm")));
        project.put("bizDt", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoDayFormat).put("date", "$bizDt")));
        project.put("reqId", 1);
        project.put("rptId", 1);
        project.put("sesId", 1);
        project.put("variationMargin", 1);
        project.put("premiumMargin", 1);
        project.put("liquiMargin", 1);
        project.put("spreadMargin", 1);
        project.put("additionalMargin", 1);
        project.put("received",new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received")));

        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$sort", sort));
        pipeline.add(new JsonObject().put("$match", params));
        pipeline.add(new JsonObject().put("$project", project));

        JsonObject command = new JsonObject()
                .put("aggregate", "ers.MarginComponent")
                .put("pipeline", pipeline);

        mongo.runCommand("aggregate", command, res -> {
            if (res.succeeded()) {
                msg.reply(Json.encodePrettily(res.result().getJsonArray("result")));
            } else {
                LOG.error("history/mc query failed", res.cause());
            }
        });
    }

    private void queryLatestTotalMarginRequirement(Message msg)
    {
        JsonObject params = (JsonObject)msg.body();
        LOG.trace("Received latest/tmr query with parameters " + params);

        JsonObject sort = new JsonObject();
        sort.put("received", 1);

        JsonObject group = new JsonObject();
        group.put("_id", new JsonObject().put("clearer", "$clearer").put("pool", "$pool").put("member", "$member").put("account", "$account").put("ccy", "$ccy"));
        group.put("id", new JsonObject().put("$last", "$_id"));
        group.put("clearer", new JsonObject().put("$last", "$clearer"));
        group.put("pool", new JsonObject().put("$last", "$pool"));
        group.put("member", new JsonObject().put("$last", "$member"));
        group.put("account", new JsonObject().put("$last", "$account"));
        group.put("ccy", new JsonObject().put("$last", "$ccy"));
        group.put("txnTm", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$txnTm"))));
        group.put("bizDt", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoDayFormat).put("date", "$bizDt"))));
        group.put("reqId", new JsonObject().put("$last", "$reqId"));
        group.put("rptId", new JsonObject().put("$last", "$rptId"));
        group.put("sesId", new JsonObject().put("$last", "$sesId"));
        group.put("unadjustedMargin", new JsonObject().put("$last", "$unadjustedMargin"));
        group.put("adjustedMargin", new JsonObject().put("$last", "$adjustedMargin"));
        group.put("received", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received"))));

        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$sort", sort));
        pipeline.add(new JsonObject().put("$match", params));
        pipeline.add(new JsonObject().put("$group", group));

        JsonObject command = new JsonObject()
                .put("aggregate", "ers.TotalMarginRequirement")
                .put("pipeline", pipeline);

        mongo.runCommand("aggregate", command, res -> {
            if (res.succeeded()) {
                msg.reply(Json.encodePrettily(res.result().getJsonArray("result")));
            } else {
                LOG.error("latest/tmr query failed", res.cause());
            }
        });
    }

    private void queryHistoryTotalMarginRequirement(Message msg)
    {
        JsonObject params = (JsonObject)msg.body();
        LOG.trace("Received history/tmr query with parameters " + params);

        JsonObject sort = new JsonObject();
        sort.put("received", 1);

        JsonObject project = new JsonObject();
        project.put("_id", 0);
        project.put("id", "$_id");
        project.put("clearer", 1);
        project.put("pool", 1);
        project.put("member", 1);
        project.put("account", 1);
        project.put("ccy", 1);
        project.put("txnTm", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$txnTm")));
        project.put("bizDt", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoDayFormat).put("date", "$bizDt")));
        project.put("reqId", 1);
        project.put("rptId", 1);
        project.put("sesId", 1);
        project.put("unadjustedMargin", 1);
        project.put("adjustedMargin", 1);
        project.put("received", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received")));

        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$sort", sort));
        pipeline.add(new JsonObject().put("$match", params));
        pipeline.add(new JsonObject().put("$project", project));

        JsonObject command = new JsonObject()
                .put("aggregate", "ers.TotalMarginRequirement")
                .put("pipeline", pipeline);

        mongo.runCommand("aggregate", command, res -> {
            if (res.succeeded()) {
                msg.reply(Json.encodePrettily(res.result().getJsonArray("result")));
            } else {
                LOG.error("history/tmr query failed", res.cause());
            }
        });
    }

    private void queryLatestMarginShortfallSurplus(Message msg)
    {
        JsonObject params = (JsonObject)msg.body();
        LOG.trace("Received latest/mss query with parameters " + params);

        JsonObject sort = new JsonObject();
        sort.put("received", 1);

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

        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$sort", sort));
        pipeline.add(new JsonObject().put("$match", params));
        pipeline.add(new JsonObject().put("$group", group));

        JsonObject command = new JsonObject()
                .put("aggregate", "ers.MarginShortfallSurplus")
                .put("pipeline", pipeline);

        mongo.runCommand("aggregate", command, res -> {
            if (res.succeeded()) {
                msg.reply(Json.encodePrettily(res.result().getJsonArray("result")));
            } else {
                LOG.error("latest/mss query failed", res.cause());
            }
        });
    }

    private void queryHistoryMarginShortfallSurplus(Message msg)
    {
        JsonObject params = (JsonObject)msg.body();
        LOG.trace("Received history/mss query with parameters " + params);

        JsonObject sort = new JsonObject();
        sort.put("received", 1);

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

        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$sort", sort));
        pipeline.add(new JsonObject().put("$match", params));
        pipeline.add(new JsonObject().put("$project", project));

        JsonObject command = new JsonObject()
                .put("aggregate", "ers.MarginShortfallSurplus")
                .put("pipeline", pipeline);

        mongo.runCommand("aggregate", command, res -> {
            if (res.succeeded()) {
                msg.reply(Json.encodePrettily(res.result().getJsonArray("result")));
            } else {
                LOG.error("history/mss query failed", res.cause());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        LOG.info("MongoDBPersistenceVerticle is being stopped");
        mongo.close();
    }
}
