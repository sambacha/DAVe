package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import com.deutscheboerse.risk.dave.util.URIBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

public class RestPersistenceService implements PersistenceService {
    private static final Logger LOG = LoggerFactory.getLogger(RestPersistenceService.class);

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 80;

    private static final int RECONNECT_DELAY = 2000;

    private static final String DEFAULT_ACCOUNT_MARGIN_URI = "/api/v1.0/query/am";
    private static final String DEFAULT_LIQUI_GROUP_MARGIN_URI = "/api/v1.0/query/lgm";
    private static final String DEFAULT_LIQUI_SPLIT_MARGIN_URI = "/api/v1.0/query/lgsm";
    private static final String DEFAULT_POSITION_REPORT_URI = "/api/v1.0/query/pr";
    private static final String DEFAULT_POOL_MARGIN_URI = "/api/v1.0/query/pm";
    private static final String DEFAULT_RISK_LIMIT_UTILIZATION_URI = "/api/v1.0/query/rlu";
    private static final String DEFAULT_HEALTHZ_URI = "/healthz";

    private final Vertx vertx;
    private final JsonObject config;
    private final JsonObject restApi;
    private final HttpClient httpClient;
    private final HealthCheck healthCheck;
    private final ConnectionManager connectionManager = new ConnectionManager();

    @Inject
    public RestPersistenceService(Vertx vertx, @Named("storeManager.conf") JsonObject config) {
        this.vertx = vertx;
        this.config = config;
        this.restApi = this.config.getJsonObject("restApi", new JsonObject());
        this.httpClient = this.vertx.createHttpClient();
        this.healthCheck = new HealthCheck(vertx);
    }

    @Override
    public void initialize(Handler<AsyncResult<Void>> resultHandler) {
        this.connectionManager.ping(ar -> {
            if (ar.succeeded()) {
                healthCheck.setComponentReady(HealthCheck.Component.PERSISTENCE_SERVICE);
            } else {
                // Try to re-initialize in a few seconds
                vertx.setTimer(RECONNECT_DELAY, i -> initialize(res -> {/*empty handler*/}));
                LOG.error("Initialize failed, trying again...");
            }
            // Inform the caller that we succeeded even if the connection to the http server
            // failed. We will try to reconnect automatically on background.
            resultHandler.handle(Future.succeededFuture());
        });
    }

    @Override
    public void queryAccountMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(DEFAULT_ACCOUNT_MARGIN_URI, type, query, resultHandler);
    }

    @Override
    public void queryLiquiGroupMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(DEFAULT_LIQUI_GROUP_MARGIN_URI, type, query, resultHandler);
    }

    @Override
    public void queryLiquiGroupSplitMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(DEFAULT_LIQUI_SPLIT_MARGIN_URI, type, query, resultHandler);
    }

    @Override
    public void queryPoolMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(DEFAULT_POOL_MARGIN_URI, type, query, resultHandler);
    }

    @Override
    public void queryPositionReport(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(DEFAULT_POSITION_REPORT_URI, type, query, resultHandler);
    }

    @Override
    public void queryRiskLimitUtilization(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(DEFAULT_RISK_LIMIT_UTILIZATION_URI, type, query, resultHandler);
    }

    @Override
    public void close() {
        this.httpClient.close();
    }

    private void query(String basePath, RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        final String requestURI = new URIBuilder(basePath + (type == RequestType.HISTORY ? "/history" : "/latest"))
            .addParams(query)
            .build();
        this.httpClient.get(
                config.getInteger("port", DEFAULT_PORT),
                config.getString("hostname", DEFAULT_HOSTNAME),
                requestURI,
                response -> {
                    if (response.statusCode() == 200) {
                        response.bodyHandler(body -> resultHandler.handle(Future.succeededFuture(body.toString())));
                    } else {
                        LOG.error("{} failed: {}", requestURI, response.statusMessage());
                        connectionManager.startReconnection();
                        resultHandler.handle(Future.failedFuture(response.statusMessage()));
                    }
                }).exceptionHandler(e -> {
                    LOG.error("{} failed: {}", requestURI, e.getMessage());
                    connectionManager.startReconnection();
                    resultHandler.handle(Future.failedFuture(e.getMessage()));
                }).end();
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

        void ping(Handler<AsyncResult<Void>> resultHandler) {
            httpClient.get(
                    config.getInteger("port", DEFAULT_PORT),
                    config.getString("hostname", DEFAULT_HOSTNAME),
                    restApi.getString("healthz", DEFAULT_HEALTHZ_URI),
                    response -> {
                        if (response.statusCode() == 200) {
                            resultHandler.handle(Future.succeededFuture());
                        } else {
                            resultHandler.handle(Future.failedFuture(response.statusMessage()));
                        }
                    }).exceptionHandler(e ->
                        resultHandler.handle(Future.failedFuture(e.getMessage()))
                    ).end();
        }

        private void scheduleConnectionStatus() {
            vertx.setTimer(RECONNECT_DELAY, id -> checkConnectionStatus());
        }

        private void checkConnectionStatus() {
            this.ping(res -> {
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
