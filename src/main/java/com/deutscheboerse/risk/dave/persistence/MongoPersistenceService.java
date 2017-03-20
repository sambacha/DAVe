package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import com.deutscheboerse.risk.dave.model.AbstractModel;
import io.vertx.core.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.*;
import io.vertx.serviceproxy.ServiceException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.deutscheboerse.risk.dave.healthcheck.HealthCheck.Component.PERSISTENCE_SERVICE;

public class MongoPersistenceService implements PersistenceService {
    private static final Logger LOG = LoggerFactory.getLogger(MongoPersistenceService.class);

    public static final String ACCOUNT_MARGIN_HISTORY_COLLECTION = "AccountMargin";
    public static final String ACCOUNT_MARGIN_LATEST_COLLECTION = "AccountMargin.latest";
    public static final String LIQUI_GROUP_MARGIN_HISTORY_COLLECTION = "LiquiGroupMargin";
    public static final String LIQUI_GROUP_MARGIN_LATEST_COLLECTION = "LiquiGroupMargin.latest";
    public static final String LIQUI_GROUP_SPLIT_MARGIN_HISTORY_COLLECTION = "LiquiGroupSplitMargin";
    public static final String LIQUI_GROUP_SPLIT_MARGIN_LATEST_COLLECTION = "LiquiGroupSplitMargin.latest";
    public static final String POOL_MARGIN_HISTORY_COLLECTION = "PoolMargin";
    public static final String POOL_MARGIN_LATEST_COLLECTION = "PoolMargin.latest";
    public static final String POSITION_REPORT_HISTORY_COLLECTION = "PositionReport";
    public static final String POSITION_REPORT_LATEST_COLLECTION = "PositionReport.latest";
    public static final String RISK_LIMIT_UTILIZATION_HISTORY_COLLECTION = "RiskLimitUtilization";
    public static final String RISK_LIMIT_UTILIZATION_LATEST_COLLECTION = "RiskLimitUtilization.latest";

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
    public void findAccountMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.find(type, ACCOUNT_MARGIN_LATEST_COLLECTION, ACCOUNT_MARGIN_HISTORY_COLLECTION, query, resultHandler);
    }

    @Override
    public void findLiquiGroupMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.find(type, LIQUI_GROUP_MARGIN_LATEST_COLLECTION, LIQUI_GROUP_MARGIN_HISTORY_COLLECTION, query, resultHandler);
    }

    @Override
    public void findLiquiGroupSplitMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.find(type, LIQUI_GROUP_SPLIT_MARGIN_LATEST_COLLECTION, LIQUI_GROUP_SPLIT_MARGIN_HISTORY_COLLECTION, query, resultHandler);
    }

    @Override
    public void findPoolMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.find(type, POOL_MARGIN_LATEST_COLLECTION, POOL_MARGIN_HISTORY_COLLECTION, query, resultHandler);
    }

    @Override
    public void findPositionReport(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.find(type, POSITION_REPORT_LATEST_COLLECTION, POSITION_REPORT_HISTORY_COLLECTION, query, resultHandler);
    }

    @Override
    public void findRiskLimitUtilization(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.find(type, RISK_LIMIT_UTILIZATION_LATEST_COLLECTION, RISK_LIMIT_UTILIZATION_HISTORY_COLLECTION, query, resultHandler);
    }

    private void find(RequestType type, String latestCollection, String historyCollection, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        switch(type) {
            case LATEST:
                this.find(latestCollection, query, resultHandler);
                break;
            case HISTORY:
                this.find(historyCollection, query, resultHandler);
                break;
            default:
                LOG.error("Unknown request type {}", type);
                resultHandler.handle(ServiceException.fail(QUERY_ERROR, "Unknown request type"));
                return;
        }
    }

    private void find(String collection, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
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
                resultHandler.handle(ServiceException.fail(QUERY_ERROR, res.cause().getMessage()));
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

    private static List<String> getRequiredCollections() {
        List<String> requiredCollections = new ArrayList<>();
        AbstractModel.getAllModels().forEach(model -> {
            requiredCollections.add(model.getHistoryCollection());
            requiredCollections.add(model.getLatestCollection());
        });
        return requiredCollections;
    }

    private Future<Void> createMissingCollections(List<String> existingCollections) {
        List<String> requiredCollections = getRequiredCollections();
        requiredCollections.removeAll(existingCollections);

        List<Future> futs = new ArrayList<>();
        requiredCollections.forEach(collection -> {
            LOG.info("Collection {} is missing and will be added", collection);
            Future<Void> fut = Future.future();
            mongo.createCollection(collection, fut.completer());
            futs.add(fut);
        });

        return CompositeFuture.all(futs).mapEmpty();
    }

    private Future<Void> initDb() {
        Future<Void> initDbFuture = Future.future();
        mongo.getCollections(res -> {
            if (res.succeeded()) {
                createMissingCollections(res.result()).setHandler(ar -> {
                    if (ar.succeeded()) {
                        LOG.info("Mongo has all needed collections for DAVe");
                        LOG.info("Initialized MongoDB");
                        initDbFuture.complete();
                    } else {
                        LOG.error("Failed to add all collections needed for DAVe to Mongo", ar.cause());
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
        Future<Void> createIndexesFuture = Future.future();

        List<Future> futs = new ArrayList<>();
        for (AbstractModel model: AbstractModel.getAllModels()) {
            futs.add(createIndexesForModel(model));
        }

        CompositeFuture.all(futs).setHandler(ar -> {
            if (ar.succeeded()) {
                LOG.info("Mongo has all needed indexes");
                createIndexesFuture.complete();
            } else {
                LOG.error("Failed to create all needed indexes in Mongo", ar.cause());
                createIndexesFuture.fail(ar.cause());
            }
        });
        return createIndexesFuture;
    }

    private Future<CompositeFuture> createIndexesForModel(AbstractModel model) {
        IndexOptions indexOptions = new IndexOptions().name("unique_idx").unique(true);

        JsonObject historyIndex = new JsonObject().put("snapshotID", 1);
        model.getKeys().forEach(key -> historyIndex.put(key, 1));

        JsonObject latestIndex = new JsonObject();
        model.getKeys().forEach(key -> latestIndex.put(key, 1));

        Future<Void> historyIndexFuture = Future.future();
        Future<Void> latestIndexFuture = Future.future();
        mongo.createIndexWithOptions(model.getHistoryCollection(), historyIndex, indexOptions, historyIndexFuture);
        mongo.createIndexWithOptions(model.getLatestCollection(), latestIndex, indexOptions, latestIndexFuture);

        return CompositeFuture.all(historyIndexFuture, latestIndexFuture);
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
