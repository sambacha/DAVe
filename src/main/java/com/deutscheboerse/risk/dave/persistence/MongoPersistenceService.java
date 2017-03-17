package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import io.vertx.core.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.*;
import io.vertx.serviceproxy.ServiceException;

import javax.inject.Inject;
import java.util.*;
import java.util.Map.Entry;

import static com.deutscheboerse.risk.dave.healthcheck.HealthCheck.Component.PERSISTENCE_SERVICE;

/**
 * @author Created by schojak on 19.8.16.
 */
public class MongoPersistenceService implements PersistenceService {
    private static final Logger LOG = LoggerFactory.getLogger(MongoPersistenceService.class);

    private static final int RECONNECT_DELAY = 2000;

    private final Vertx vertx;
    private final MongoClient mongo;
    private final HealthCheck healthCheck;

    private boolean closed;

    private final ConnectionManager connectionManager = new ConnectionManager();

    @Inject
    public MongoPersistenceService(Vertx vertx, MongoClient mongo) {
        this.vertx = vertx;
        this.healthCheck = new HealthCheck(this.vertx);
        this.mongo = mongo;
        this.closed = false;
    }

    @Override
    public void initialize(Handler<AsyncResult<Void>> resultHandler) {
        initDb()
                .compose(i -> createIndexes())
                .setHandler(ar -> {
                    if (ar.succeeded()) {
                        healthCheck.setComponentReady(PERSISTENCE_SERVICE);
                    } else {
                        if (!closed) {
                            // Try to re-initialize in a few seconds
                            vertx.setTimer(RECONNECT_DELAY, i -> initialize(res -> {/*empty handler*/}));
                        }
                        LOG.error("Initialize failed, trying again...");
                    }
                    // Inform the caller that we succeeded even if the connection to mongo database
                    // failed. We will try to reconnect automatically on background.
                    resultHandler.handle(Future.succeededFuture());
                });
    }

    @Override
    public void find(String collection, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        LOG.trace("Received {} query with message {}", collection, query);
        FindOptions findOptions = new FindOptions()
                .setFields(new JsonObject().put("_id", 0))
                .setSort(new JsonObject().put("snapshotID", 1));
        mongo.findWithOptions(collection, query, findOptions, res -> {
            if (res.succeeded()) {
                resultHandler.handle(Future.succeededFuture(Json.encodePrettily(res.result())));
            } else {
                LOG.error("{} query failed", collection, res.cause());
                connectionManager.startReconnection();
                resultHandler.handle(ServiceException.fail(STORE_ERROR, res.cause().getMessage()));
            }
        });
    }

    @Override
    public void insert(String collection, JsonObject document, Handler<AsyncResult<String>> resultHandler) {
        mongo.insert(collection, document, resultHandler);
    }

    @Override
    public void upsert(String collection, JsonObject query, JsonObject document, Handler<AsyncResult<String>> resultHandler) {
        Future<MongoClientUpdateResult> upsertFuture = Future.future();
        mongo.replaceDocumentsWithOptions(collection, query, document,
                new UpdateOptions().setUpsert(true), upsertFuture);

        upsertFuture.map(res -> res.toJson().toString()).setHandler(resultHandler);
    }

    @Override
    public void close() {
        this.closed = true;
        this.mongo.close();
    }

    private Future<Void> initDb() {
        Future<Void> initDbFuture = Future.future();
        mongo.getCollections(res -> {
            if (res.succeeded()) {
                List<String> mongoCollections = res.result();
                List<String> neededCollections = new ArrayList<>(Arrays.asList(
                    "AccountMargin",
                    "AccountMargin.latest",
                    "LiquiGroupMargin",
                    "LiquiGroupMargin.latest",
                    "LiquiGroupSplitMargin",
                    "LiquiGroupSplitMargin.latest",
                    "PoolMargin",
                    "PoolMargin.latest",
                    "PositionReport",
                    "PositionReport.latest",
                    "RiskLimitUtilization",
                    "RiskLimitUtilization.latest"
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

    private Future<Void> createIndexes() {
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

    private class ConnectionManager {

        void startReconnection() {
            if (healthCheck.isComponentReady(HealthCheck.Component.PERSISTENCE_SERVICE)) {
                // Inform other components that we have failed
                healthCheck.setComponentFailed(HealthCheck.Component.PERSISTENCE_SERVICE);
                // Re-check the connection
                scheduleConnectionStatus();
            }
        }

        private void scheduleConnectionStatus() {
            if (!closed) {
                vertx.setTimer(RECONNECT_DELAY, id -> checkConnectionStatus());
            }
        }

        private void checkConnectionStatus() {
            mongo.runCommand("dbstats", new JsonObject().put("dbstats", 1), res -> {
                if (res.succeeded()) {
                    LOG.info("Back online");
                    healthCheck.setComponentReady(HealthCheck.Component.PERSISTENCE_SERVICE);
                } else {
                    LOG.error("Still disconnected");
                    scheduleConnectionStatus();
                }
            });
        }
    }
}
