package com.opnfi.risk;

import com.opnfi.risk.model.*;
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
                        "ers.MarginShortfallSurplus",
                        "ers.PositionReport",
                        "ers.RiskLimit"
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
        eb.consumer("ers.PositionReport", message -> storePositionReport(message));
        eb.consumer("ers.RiskLimit", message -> storeRiskLimit(message));

        // Query endpoints
        eb.consumer("query.latestTradingSessionStatus", message -> queryLatestTradingSessionStatus(message));
        eb.consumer("query.historyTradingSessionStatus", message -> queryHistoryTradingSessionStatus(message));
        eb.consumer("query.latestMarginComponent", message -> queryLatestMarginComponent(message));
        eb.consumer("query.historyMarginComponent", message -> queryHistoryMarginComponent(message));
        eb.consumer("query.latestTotalMarginRequirement", message -> queryLatestTotalMarginRequirement(message));
        eb.consumer("query.historyTotalMarginRequirement", message -> queryHistoryTotalMarginRequirement(message));
        eb.consumer("query.latestMarginShortfallSurplus", message -> queryLatestMarginShortfallSurplus(message));
        eb.consumer("query.historyMarginShortfallSurplus", message -> queryHistoryMarginShortfallSurplus(message));
        eb.consumer("query.latestPositionReport", message -> queryLatestPositionReport(message));
        eb.consumer("query.historyPositionReport", message -> queryHistoryPositionReport(message));
        eb.consumer("query.latestRiskLimit", message -> queryLatestRiskLimit(message));
        eb.consumer("query.historyRiskLimit", message -> queryHistoryRiskLimit(message));

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

    private void storePositionReport(Message msg)
    {
        LOG.trace("Storing PositionReport message with body: " + msg.body().toString());
        PositionReport pr = Json.decodeValue(msg.body().toString(), PositionReport.class);

        JsonObject jsonPr = new JsonObject((String)msg.body());

        if (pr.getBizDt() != null) {
            jsonPr.put("bizDt", new JsonObject().put("$date", timestampFormatter.format(pr.getBizDt())));
        }

        if (pr.getReceived() != null) {
            jsonPr.put("received", new JsonObject().put("$date", timestampFormatter.format(pr.getReceived())));
        }

        mongo.insert("ers.PositionReport", jsonPr, res -> {
            if (res.succeeded()) {
                LOG.trace("Stored PositionReport into DB");
            } else {
                LOG.error("Failed to store PositionReport into DB " + res.cause());
            }
        });
    }

    private void storeRiskLimit(Message msg)
    {
        LOG.trace("Storing RL message with body: " + msg.body().toString());

        List<Future> storeTasks = new ArrayList<>();
        JsonArray jsonMsg = new JsonArray((String)msg.body());

        for (int i = 0; i < jsonMsg.size(); i++)
        {
            Future storeTask = Future.future();
            storeTasks.add(storeTask);

            JsonObject jsonRl = jsonMsg.getJsonObject(i);
            RiskLimit rl = Json.decodeValue(jsonRl.encodePrettily(), RiskLimit.class);

            if (rl.getTxnTm() != null) {
                jsonRl.put("txnTm", new JsonObject().put("$date", timestampFormatter.format(rl.getTxnTm())));
            }

            if (rl.getReceived() != null) {
                jsonRl.put("received", new JsonObject().put("$date", timestampFormatter.format(rl.getReceived())));
            }

            mongo.insert("ers.RiskLimit", jsonRl, res -> {
                if (res.succeeded())
                {
                    LOG.trace("Stored RiskLimit into DB {}", jsonRl);
                    storeTask.complete();
                }
                else
                {
                    LOG.error("Failed to store RiskLimit {} into DB", res.cause(), jsonRl);
                    storeTask.fail(res.cause());
                }
            });
        }

        CompositeFuture.all(storeTasks).setHandler(ar -> {
            if (ar.succeeded())
            {
                LOG.trace("Complete RiskLimit message stored in DB");
            }
            else
            {
                LOG.trace("Failed to store complete RiskLimit message into DB");
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

    private void queryLatestPositionReport(Message msg)
    {
        JsonObject params = (JsonObject)msg.body();
        LOG.trace("Received latest/pr query with parameters " + params);

        JsonObject sort = new JsonObject();
        sort.put("received", 1);

        JsonObject group = new JsonObject();
        group.put("_id", new JsonObject()
                .put("clearer", "$clearer").put("member", "$member").put("account", "$account")
                .put("symbol", "$symbol").put("putCall", "$putCall").put("strikePrice", "$strikePrice")
                .put("optAttribute", "$optAttribute").put("maturityMonthYear", "$maturityMonthYear"));
        group.put("id", new JsonObject().put("$last", "$_id"));
        group.put("clearer", new JsonObject().put("$last", "$clearer"));
        group.put("member", new JsonObject().put("$last", "$member"));
        group.put("account", new JsonObject().put("$last", "$account"));
        group.put("reqId", new JsonObject().put("$last", "$reqId"));
        group.put("rptId", new JsonObject().put("$last", "$rptId"));
        group.put("bizDt", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoDayFormat).put("date", "$bizDt"))));
        group.put("lastReportRequested", new JsonObject().put("$last", "$lastReportRequested"));
        group.put("settlSesId", new JsonObject().put("$last", "$settlSesId"));
        group.put("symbol", new JsonObject().put("$last", "$symbol"));
        group.put("putCall", new JsonObject().put("$last", "$putCall"));
        group.put("maturityMonthYear", new JsonObject().put("$last", "$maturityMonthYear"));
        group.put("strikePrice", new JsonObject().put("$last", "$strikePrice"));
        group.put("optAttribute", new JsonObject().put("$last", "$optAttribute"));
        group.put("crossMarginLongQty", new JsonObject().put("$last", "$crossMarginLongQty"));
        group.put("crossMarginShortQty", new JsonObject().put("$last", "$crossMarginShortQty"));
        group.put("optionExcerciseQty", new JsonObject().put("$last", "$optionExcerciseQty"));
        group.put("optionAssignmentQty", new JsonObject().put("$last", "$optionAssignmentQty"));
        group.put("allocationTradeQty", new JsonObject().put("$last", "$allocationTradeQty"));
        group.put("deliveryNoticeQty", new JsonObject().put("$last", "$deliveryNoticeQty"));
        group.put("received", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received"))));

        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$sort", sort));
        pipeline.add(new JsonObject().put("$match", params));
        pipeline.add(new JsonObject().put("$group", group));

        JsonObject command = new JsonObject()
                .put("aggregate", "ers.PositionReport")
                .put("pipeline", pipeline);

        mongo.runCommand("aggregate", command, res -> {
            if (res.succeeded()) {
                msg.reply(Json.encodePrettily(res.result().getJsonArray("result")));
            } else {
                LOG.error("latest/pr query failed", res.cause());
            }
        });
    }

    private void queryHistoryPositionReport(Message msg)
    {
        JsonObject params = (JsonObject)msg.body();
        LOG.trace("Received history/pr query with parameters " + params);

        JsonObject sort = new JsonObject();
        sort.put("received", 1);

        JsonObject project = new JsonObject();
        project.put("_id", 0);
        project.put("id", "$_id");
        project.put("clearer", 1);
        project.put("member", 1);
        project.put("account", 1);
        project.put("reqId", 1);
        project.put("rptId", 1);
        project.put("bizDt", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoDayFormat).put("date", "$bizDt")));
        project.put("lastReportRequested", 1);
        project.put("settlSesId", 1);
        project.put("symbol", 1);
        project.put("putCall", 1);
        project.put("maturityMonthYear", 1);
        project.put("strikePrice", 1);
        project.put("optAttribute", 1);
        project.put("crossMarginLongQty", 1);
        project.put("crossMarginShortQty", 1);
        project.put("optionExcerciseQty", 1);
        project.put("optionAssignmentQty", 1);
        project.put("allocationTradeQty", 1);
        project.put("deliveryNoticeQty", 1);
        project.put("received", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received")));

        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$sort", sort));
        pipeline.add(new JsonObject().put("$match", params));
        pipeline.add(new JsonObject().put("$project", project));

        JsonObject command = new JsonObject()
                .put("aggregate", "ers.PositionReport")
                .put("pipeline", pipeline);

        mongo.runCommand("aggregate", command, res -> {
            if (res.succeeded()) {
                msg.reply(Json.encodePrettily(res.result().getJsonArray("result")));
            } else {
                LOG.error("history/pr query failed", res.cause());
            }
        });
    }

    private void queryLatestRiskLimit(Message msg)
    {
        JsonObject params = (JsonObject)msg.body();
        LOG.trace("Received latest/rl query with parameters " + params);

        JsonObject sort = new JsonObject();
        sort.put("received", 1);

        JsonObject group = new JsonObject();
        group.put("_id", new JsonObject()
                .put("clearer", "$clearer").put("member", "$member").put("maintainer", "$maintainer")
                .put("limitType", "$limitType"));
        group.put("id", new JsonObject().put("$last", "$_id"));
        group.put("clearer", new JsonObject().put("$last", "$clearer"));
        group.put("member", new JsonObject().put("$last", "$member"));
        group.put("maintainer", new JsonObject().put("$last", "$maintainer"));
        group.put("reqId", new JsonObject().put("$last", "$reqId"));
        group.put("rptId", new JsonObject().put("$last", "$rptId"));
        group.put("txnTm", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$txnTm"))));
        group.put("reqRslt", new JsonObject().put("$last", "$reqRslt"));
        group.put("txt", new JsonObject().put("$last", "$txt"));
        group.put("limitType", new JsonObject().put("$last", "$limitType"));
        group.put("utilization", new JsonObject().put("$last", "$utilization"));
        group.put("warningLevel", new JsonObject().put("$last", "$warningLevel"));
        group.put("throttleLevel", new JsonObject().put("$last", "$throttleLevel"));
        group.put("rejectLevel", new JsonObject().put("$last", "$rejectLevel"));
        group.put("received", new JsonObject().put("$last", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received"))));

        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$sort", sort));
        pipeline.add(new JsonObject().put("$match", params));
        pipeline.add(new JsonObject().put("$group", group));

        JsonObject command = new JsonObject()
                .put("aggregate", "ers.RiskLimit")
                .put("pipeline", pipeline);

        mongo.runCommand("aggregate", command, res -> {
            if (res.succeeded()) {
                msg.reply(Json.encodePrettily(res.result().getJsonArray("result")));
            } else {
                LOG.error("latest/rl query failed", res.cause());
            }
        });
    }

    private void queryHistoryRiskLimit(Message msg)
    {
        JsonObject params = (JsonObject)msg.body();
        LOG.trace("Received history/rl query with parameters " + params);

        JsonObject sort = new JsonObject();
        sort.put("received", 1);

        JsonObject project = new JsonObject();
        project.put("_id", 0);
        project.put("id", "$_id");
        project.put("clearer", 1);
        project.put("member", 1);
        project.put("maintainer", 1);
        project.put("reqId", 1);
        project.put("rptId", 1);
        project.put("txnTm", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$txnTm")));
        project.put("reqRslt", 1);
        project.put("txt", 1);
        project.put("limitType", 1);
        project.put("utilization", 1);
        project.put("warningLevel", 1);
        project.put("throttleLevel", 1);
        project.put("rejectLevel", 1);
        project.put("received", new JsonObject().put("$dateToString", new JsonObject().put("format", mongoTimestampFormat).put("date", "$received")));

        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$sort", sort));
        pipeline.add(new JsonObject().put("$match", params));
        pipeline.add(new JsonObject().put("$project", project));

        JsonObject command = new JsonObject()
                .put("aggregate", "ers.RiskLimit")
                .put("pipeline", pipeline);

        mongo.runCommand("aggregate", command, res -> {
            if (res.succeeded()) {
                msg.reply(Json.encodePrettily(res.result().getJsonArray("result")));
            } else {
                LOG.error("history/rl query failed", res.cause());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        LOG.info("MongoDBPersistenceVerticle is being stopped");
        mongo.close();
    }
}
