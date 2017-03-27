package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import com.deutscheboerse.risk.dave.model.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceException;

import javax.inject.Inject;

import java.util.function.BiFunction;

import static com.deutscheboerse.risk.dave.healthcheck.HealthCheck.Component.PERSISTENCE_SERVICE;

public class MongoPersistenceService implements PersistenceService {
    private static final Logger LOG = LoggerFactory.getLogger(MongoPersistenceService.class);

    public static final String ACCOUNT_MARGIN_COLLECTION = "AccountMargin";
    public static final String LIQUI_GROUP_MARGIN_COLLECTION = "LiquiGroupMargin";
    public static final String LIQUI_GROUP_SPLIT_MARGIN_COLLECTION = "LiquiGroupSplitMargin";
    public static final String POOL_MARGIN_COLLECTION = "PoolMargin";
    public static final String POSITION_REPORT_COLLECTION = "PositionReport";
    public static final String RISK_LIMIT_UTILIZATION_COLLECTION = "RiskLimitUtilization";

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
        this.find(type, ACCOUNT_MARGIN_COLLECTION, query, new AccountMarginModel(), resultHandler);
    }

    @Override
    public void findLiquiGroupMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.find(type, LIQUI_GROUP_MARGIN_COLLECTION, query, new LiquiGroupMarginModel(), resultHandler);
    }

    @Override
    public void findLiquiGroupSplitMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.find(type, LIQUI_GROUP_SPLIT_MARGIN_COLLECTION, query, new LiquiGroupSplitMarginModel(), resultHandler);
    }

    @Override
    public void findPoolMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.find(type, POOL_MARGIN_COLLECTION, query, new PoolMarginModel(), resultHandler);
    }

    @Override
    public void findPositionReport(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.find(type, POSITION_REPORT_COLLECTION, query, new PositionReportModel(), resultHandler);
    }

    @Override
    public void findRiskLimitUtilization(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.find(type, RISK_LIMIT_UTILIZATION_COLLECTION, query, new RiskLimitUtilizationModel(), resultHandler);
    }

    private void find(RequestType type, String collection, JsonObject query, AbstractModel model, Handler<AsyncResult<String>> resultHandler) {
        LOG.trace("Received {} {} query with message {}", type.name(), collection, query);
        BiFunction<JsonObject, AbstractModel, JsonArray> getPipeline;
        switch(type) {
            case LATEST:
                getPipeline = MongoPersistenceService::getLatestPipeline;
                break;
            case HISTORY:
                getPipeline = MongoPersistenceService::getHistoryPipeline;
                break;
            default:
                LOG.error("Unknown request type {}", type);
                resultHandler.handle(ServiceException.fail(QUERY_ERROR, "Unknown request type"));
                return;
        }
        mongo.runCommand("aggregate", MongoPersistenceService.getCommand(collection, query, model, getPipeline), res -> {
            if (res.succeeded()) {
                resultHandler.handle(Future.succeededFuture(Json.encodePrettily(res.result().getJsonArray("result"))));
            } else {
                LOG.error("{} query failed", collection, res.cause());
                connectionManager.startReconnection();
                resultHandler.handle(ServiceException.fail(QUERY_ERROR, res.cause().getMessage()));
            }
        });

    }

    private static JsonObject getCommand(String collection, JsonObject params, AbstractModel model, BiFunction<JsonObject, AbstractModel, JsonArray> getPipeline) {
        return new JsonObject()
                .put("aggregate", collection)
                .put("pipeline", getPipeline.apply(params, model))
                .put("allowDiskUse", true);
    }

    private static JsonArray getLatestPipeline(JsonObject params, AbstractModel model) {
        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$match", params));
        pipeline.add(new JsonObject().put("$project", getLatestSnapshotProject(model)));
        pipeline.add(new JsonObject().put("$unwind", "$snapshots"));
        pipeline.add(new JsonObject().put("$project", getFlattenProject(model)));
        return pipeline;
    }

    private static JsonArray getHistoryPipeline(JsonObject params, AbstractModel model) {
        JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$match", params));
        pipeline.add(new JsonObject().put("$unwind", "$snapshots"));
        pipeline.add(new JsonObject().put("$project", getFlattenProject(model)));
        return pipeline;
    }

    private static JsonObject getLatestSnapshotProject(AbstractModel model) {
        JsonObject project = new JsonObject();
        model.getKeys().forEach(key -> project.put(key, 1));
        project.put("snapshots", new JsonObject().put("$slice", new JsonArray().add("$snapshots").add(-1)));
        return project;
    }

    private static JsonObject getFlattenProject(AbstractModel model) {
        JsonObject project = new JsonObject();
        project.put("_id", 0);
        model.getKeys().forEach(key -> project.put(key, 1));
        model.getNonKeys().forEach(nonKey -> project.put(nonKey, "$snapshots." + nonKey));
        model.getHeader().forEach(header -> project.put(header, "$snapshots." + header));
        return project;
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
            mongo.runCommand("ping", new JsonObject().put("ping", 1), res -> {
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
