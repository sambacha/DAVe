package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import com.deutscheboerse.risk.dave.model.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.ext.mongo.MongoClient;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by schojak on 19.8.16.
 */
public class MongoDBPersistenceVerticle extends AbstractVerticle {
    final static private Logger LOG = LoggerFactory.getLogger(MongoDBPersistenceVerticle.class);

    private static final String DEFAULT_DB_NAME = "DAVe";
    private static final String DEFAULT_CONNECTION_STRING = "mongodb://localhost:27017";

    private MongoClient mongo;
    private final List<MessageConsumer<?>> eventBusConsumers = new ArrayList<>();

    private HealthCheck healthCheck;

    private final AbstractModel tssModel = new TradingSessionStatusModel();
    private final AbstractModel mcModel = new MarginComponentModel();
    private final AbstractModel tmrModel = new TotalMarginRequirementModel();
    private final AbstractModel mssModel = new MarginShortfallSurplusModel();
    private final AbstractModel prModel = new PositionReportModel();
    private final AbstractModel rlModel = new RiskLimitModel();

    @Override
    public void start(Future<Void> fut) throws Exception {
        LOG.info("Starting {} with configuration: {}", MongoDBPersistenceVerticle.class.getSimpleName(), config().encodePrettily());

        healthCheck = new HealthCheck(this.vertx);

        Future<Void> chainFuture = Future.future();
        connectDb()
                .compose(this::initDb)
                .compose(this::createIndexes)
                .compose(this::startQueryHandlers)
                .compose(chainFuture::complete, chainFuture);
        chainFuture.setHandler(ar -> {
            if (ar.succeeded()) {
                LOG.info("MongoDB verticle started");
                healthCheck.setMongoState(true);
                fut.complete();
            } else {
                LOG.error("MongoDB verticle failed to deploy", chainFuture.cause());
                healthCheck.setMongoState(false);
                fut.fail(chainFuture.cause());
            }
        });
    }

    private Future<Void> connectDb() {
        JsonObject dbConfig = new JsonObject();
        dbConfig.put("db_name", config().getString("dbName", MongoDBPersistenceVerticle.DEFAULT_DB_NAME));
        dbConfig.put("useObjectId", true);
        dbConfig.put("connection_string", config().getString("connectionUrl", MongoDBPersistenceVerticle.DEFAULT_CONNECTION_STRING));

        mongo = MongoClient.createShared(vertx, dbConfig);
        LOG.info("Connected to MongoDB");
        return Future.succeededFuture();
    }

    private Future<Void> initDb(Void unused) {
        Future<Void> initDbFuture = Future.future();
        mongo.getCollections(res -> {
            if (res.succeeded()) {
                List<String> mongoCollections = res.result();
                List<String> neededCollections = new ArrayList<>(Arrays.asList(
                        "ers.TradingSessionStatus",
                        "ers.TradingSessionStatus.latest",
                        "ers.MarginComponent",
                        "ers.MarginComponent.latest",
                        "ers.TotalMarginRequirement",
                        "ers.TotalMarginRequirement.latest",
                        "ers.MarginShortfallSurplus",
                        "ers.MarginShortfallSurplus.latest",
                        "ers.PositionReport",
                        "ers.PositionReport.latest",
                        "ers.RiskLimit",
                        "ers.RiskLimit.latest"
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
                        LOG.info("Initialized MongoDB");
                        initDbFuture.complete();
                    }
                    else
                    {
                        LOG.error("Failed to add all collections needed for ERS to Mongo", ar.cause());
                        initDbFuture.fail(ar.cause());
                    }
                });
            } else {
                LOG.error("Failed to get collection list", res.cause());
                initDbFuture.fail(res.cause());
            }
        });
        return initDbFuture;
    }

    private Future<Void> createIndexes(Void unused) {
        Future<Void> initDbFuture = Future.future();
        Map<String, JsonObject> indexes = new HashMap<>();
        indexes.put("ers.MarginComponent", new JsonObject().put("clearer", 1).put("member", 1).put("account", 1).put("clss", 1).put("ccy", 1));
        indexes.put("ers.MarginShortfallSurplus", new JsonObject().put("clearer", 1).put("pool", 1).put("member", 1).put("clearingCcy", 1).put("ccy", 1));
        indexes.put("ers.PositionReport", new JsonObject().put("clearer", 1).put("member", 1).put("account", 1).put("clss", 1).put("symbol", 1).put("putCall", 1).put("strikePrice", 1).put("optAttribute", 1).put("maturityMonthYear", 1));
        indexes.put("ers.RiskLimit", new JsonObject().put("clearer", 1).put("member", 1).put("maintainer", 1).put("limitType", 1));
        indexes.put("ers.TotalMarginRequirement", new JsonObject().put("clearer", 1).put("pool", 1).put("member", 1).put("account", 1).put("ccy", 1));
        List<Future> futs = new ArrayList<>();
        for (Entry<String, JsonObject> index : indexes.entrySet()) {
            Future<Void> receivedIndexFuture = Future.future();
            mongo.createIndexWithOptions(index.getKey(), new JsonObject().put("received", 1), new IndexOptions().name("received_idx"), receivedIndexFuture.completer());
            futs.add(receivedIndexFuture);

            Future<Void> compoundIndexFuture = Future.future();
            mongo.createIndexWithOptions(index.getKey(), index.getValue(), new IndexOptions().name("compound_idx"), compoundIndexFuture.completer());
            futs.add(compoundIndexFuture);
        }

        CompositeFuture.all(futs).setHandler(ar -> {
            if (ar.succeeded()) {
                LOG.info("Mongo has all needed indexes");
                initDbFuture.complete();
            } else {
                LOG.error("Failed to create all needed indexes in Mongo", ar.cause());
                initDbFuture.fail(ar.cause());
            }
        });
        return initDbFuture;
    }

    private Future<Void> startQueryHandlers(Void unused)
    {
        // Query endpoints
        this.registerConsumer("query.latestTradingSessionStatus", message -> queryLatest(message, this.tssModel));
        this.registerConsumer("query.historyTradingSessionStatus", message -> queryHistory(message, this.tssModel));
        this.registerConsumer("query.latestMarginComponent", message -> queryLatest(message, this.mcModel));
        this.registerConsumer("query.historyMarginComponent", message -> queryHistory(message, this.mcModel));
        this.registerConsumer("query.latestTotalMarginRequirement", message -> queryLatest(message, this.tmrModel));
        this.registerConsumer("query.historyTotalMarginRequirement", message -> queryHistory(message, this.tmrModel));
        this.registerConsumer("query.latestMarginShortfallSurplus", message -> queryLatest(message, this.mssModel));
        this.registerConsumer("query.historyMarginShortfallSurplus", message -> queryHistory(message, this.mssModel));
        this.registerConsumer("query.latestPositionReport", message -> queryLatest(message, this.prModel));
        this.registerConsumer("query.historyPositionReport", message -> queryHistory(message, this.prModel));
        this.registerConsumer("query.latestRiskLimit", message -> queryLatest(message, this.rlModel));
        this.registerConsumer("query.historyRiskLimit", message -> queryHistory(message, this.rlModel));

        LOG.info("Event bus query handlers subscribed");
        return Future.succeededFuture();
    }

    private <T> void registerConsumer(String address, Handler<Message<T>> handler) {
        EventBus eb = vertx.eventBus();
        this.eventBusConsumers.add(eb.consumer(address, handler));
    }

    private void queryLatest(Message<?> msg, AbstractModel model) {
        LOG.trace("Received {} query with message {}", msg.address(), msg.body());
        JsonObject params = (JsonObject)msg.body();
        mongo.find(model.getLatestCollection(), params, res -> {
            if (res.succeeded()) {
                msg.reply(Json.encodePrettily(res.result()));
            } else {
                LOG.error("{} query failed", msg.address(), res.cause());
            }
        });
    }

    private void queryHistory(Message<?> msg, AbstractModel model) {
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

    @Override
    public void stop() throws Exception {
        LOG.info("MongoDBPersistenceVerticle is being stopped");
        this.eventBusConsumers.forEach(consumer -> consumer.unregister());
        mongo.close();
    }
}
