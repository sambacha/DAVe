package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.model.*;
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

    private static final String DEFAULT_DB_NAME = "DAVe";
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
            Future<Void> startStoreHandlersFuture = Future.future();
            startStoreHandlers(startStoreHandlersFuture.completer());
            return startStoreHandlersFuture;
        }).compose(v -> {
            LOG.info("Event bus store handlers subscribed");
            Future<Void> startQueryHandlersFuture = Future.future();
            startQueryHandlers(startQueryHandlersFuture.completer());
            return startQueryHandlersFuture;
        }).compose(v -> {
            LOG.info("Event bus query handlers subscribed");
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

    private void startStoreHandlers(Handler<AsyncResult<Void>> completer)
    {
        EventBus eb = vertx.eventBus();

        // Camel consumers
        eb.consumer("ers.TradingSessionStatus", message -> store(message));
        eb.consumer("ers.MarginComponent", message -> store(message));
        eb.consumer("ers.TotalMarginRequirement", message -> store(message));
        eb.consumer("ers.MarginShortfallSurplus", message -> store(message));
        eb.consumer("ers.PositionReport", message -> store(message));
        // TODO: Use JsonObjects for risk limits and move it to store(message) method as well
        eb.consumer("ers.RiskLimit", message -> storeRiskLimit(message));

        completer.handle(Future.succeededFuture());
    }

    private void startQueryHandlers(Handler<AsyncResult<Void>> completer)
    {
        EventBus eb = vertx.eventBus();

        // Query endpoints
        eb.consumer("query.latestTradingSessionStatus", message -> queryLatest(message, new TradingSessionStatusModel()));
        eb.consumer("query.historyTradingSessionStatus", message -> queryHistory(message, new TradingSessionStatusModel()));
        eb.consumer("query.latestMarginComponent", message -> queryLatest(message, new MarginComponentModel()));
        eb.consumer("query.historyMarginComponent", message -> queryHistory(message, new MarginComponentModel()));
        eb.consumer("query.latestTotalMarginRequirement", message -> queryLatest(message, new TotalMarginRequirementModel()));
        eb.consumer("query.historyTotalMarginRequirement", message -> queryHistory(message, new TotalMarginRequirementModel()));
        eb.consumer("query.latestMarginShortfallSurplus", message -> queryLatest(message, new MarginShortfallSurplusModel()));
        eb.consumer("query.historyMarginShortfallSurplus", message -> queryHistory(message, new MarginShortfallSurplusModel()));
        eb.consumer("query.latestPositionReport", message -> queryLatest(message, new PositionReportModel()));
        eb.consumer("query.historyPositionReport", message -> queryHistory(message, new PositionReportModel()));
        eb.consumer("query.latestRiskLimit", message -> queryLatest(message, new RiskLimitModel()));
        eb.consumer("query.historyRiskLimit", message -> queryHistory(message, new RiskLimitModel()));

        completer.handle(Future.succeededFuture());
    }

    private void queryLatest(Message msg, AbstractModel model)
    {
        LOG.trace("Received {} query with message {}", msg.address(), msg.body());

        JsonObject params = (JsonObject)msg.body();

        mongo.runCommand("aggregate", model.getLatestCommand(params), res -> {
            if (res.succeeded()) {
                msg.reply(Json.encodePrettily(res.result().getJsonArray("result")));
            } else {
                LOG.error("{} query failed", msg.address(), res.cause());
            }
        });
    }

    private void queryHistory(Message msg, AbstractModel model)
    {
        LOG.trace("Received {} query with message {}", msg.address(), msg.body());

        JsonObject params = (JsonObject)msg.body();

        mongo.runCommand("aggregate", model.getHistoryCommand(params), res -> {
            if (res.succeeded()) {
                msg.reply(Json.encodePrettily(res.result().getJsonArray("result")));
            } else {
                LOG.error("{} query failed", msg.address(), res.cause());
            }
        });
    }

    private void store(Message msg)
    {
        LOG.trace("Storing message {} with body {}", msg.address(), msg.body().toString());
        JsonObject json = (JsonObject)msg.body();

        mongo.insert(msg.address(), json, res -> {
            if (res.succeeded())
            {
                LOG.trace("Stored {} into DB", msg.address());
                msg.reply(new JsonObject());
            }
            else
            {
                LOG.error("Failed to store {} into DB ", msg.address(), res.cause());
                msg.fail(1, res.cause().getMessage());
            }
        });
    }


    private void storeRiskLimit(Message msg)
    {
        LOG.trace("Storing RL message with body: " + msg.body().toString());

        List<Future> storeTasks = new ArrayList<>();
        JsonArray jsonMsg = (JsonArray)msg.body();

        for (int i = 0; i < jsonMsg.size(); i++)
        {
            Future storeTask = Future.future();
            storeTasks.add(storeTask);

            JsonObject jsonRl = jsonMsg.getJsonObject(i);
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
                msg.reply(new JsonObject());
            }
            else
            {
                LOG.trace("Failed to store complete RiskLimit message into DB");
                msg.fail(1, ar.cause().getMessage());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        LOG.info("MongoDBPersistenceVerticle is being stopped");
        mongo.close();
    }
}
