package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceException;

import javax.inject.Inject;

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
        initDb().setHandler(ar -> {
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
        }
    }

    private void find(String collection, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        LOG.trace("Received {} query with message {}", collection, query);
        FindOptions findOptions = new FindOptions()
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
    public void close() {
        this.closed = true;
        this.mongo.close();
    }

    private Future<Void> initDb() {
        Future<Void> initDbFuture = Future.future();
        mongo.getCollections(res -> {
            if (res.succeeded()) {
                LOG.info("Initialized MongoDB");
                initDbFuture.complete();
            } else {
                LOG.error("Failed to get collection list", res.cause());
                initDbFuture.fail(res.cause());
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
