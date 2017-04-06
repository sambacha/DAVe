package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.model.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

public class StorageManagerMock {
    private static final Logger LOG = LoggerFactory.getLogger(StorageManagerMock.class);

    private static final Integer DEFAULT_PORT = 8080;

    private static final AccountMarginModel ACCOUNT_MARGIN_MODEL = new AccountMarginModel();
    private static final LiquiGroupMarginModel LIQUI_GROUP_MARGIN_MODEL = new LiquiGroupMarginModel();
    private static final LiquiGroupSplitMarginModel LIQUI_GROUP_SPLIT_MARGIN_MODEL = new LiquiGroupSplitMarginModel();
    private static final PoolMarginModel POOL_MARGIN_MODEL = new PoolMarginModel();
    private static final PositionReportModel POSITION_REPORT_MODEL = new PositionReportModel();
    private static final RiskLimitUtilizationModel RISK_LIMIT_UTILIZATION_MODEL = new RiskLimitUtilizationModel();

    private final Vertx vertx;
    private final JsonObject config;
    private final HttpServer server;
    private boolean health = true;

    public StorageManagerMock(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
        this.server = this.createHttpServer();
    }

    public StorageManagerMock listen(Handler<AsyncResult<Void>> resultHandler) {

        int port = config.getInteger("port", DEFAULT_PORT);
        LOG.info("Starting web server on port {}", port);

        Future<HttpServer> listenFuture = Future.future();
        server.listen(port, listenFuture);

        listenFuture.map((Void)null).setHandler(resultHandler);
        return this;
    }

    StorageManagerMock setHealth(boolean health) {
        this.health = health;
        return this;
    }

    private HttpServer createHttpServer() {
        Router router = configureRouter();

        return vertx.createHttpServer().requestHandler(router::accept);
    }

    private Router configureRouter() {
        HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);

        healthCheckHandler.register("healthz", this::healthz);

        Router router = Router.router(vertx);

        LOG.info("Adding route REST API");
        router.get("/healthz").handler(healthCheckHandler);
        registerRestPoint(router, "accountMargin", this::queryAccountMargin);
        registerRestPoint(router, "liquiGroupMargin", this::queryLiquiGroupMargin);
        registerRestPoint(router, "liquiGroupSplitMargin", this::queryLiquiGroupSplitMargin);
        registerRestPoint(router, "poolMargin", this::queryPoolMargin);
        registerRestPoint(router, "positionReport", this::queryPositionReport);
        registerRestPoint(router, "riskLimitUtilization", this::queryRiskLimitUtilization);

        return router;
    }

    private void registerRestPoint(Router router, String restApi, Handler<RoutingContext> handler) {
        final String baseURI = config.getJsonObject("restApi", new JsonObject()).getString(restApi);

        router.get(baseURI+"/history").handler(handler);
        router.get(baseURI+"/latest").handler(handler);
    }

    private void healthz(Future<Status> future) {
        future.complete(this.health ? Status.OK() : Status.KO());
    }

    private void queryAccountMargin(RoutingContext routingContext) {
        LOG.trace("Received queryAccountMargin request");
        this.createResponse(routingContext, ACCOUNT_MARGIN_MODEL);
    }

    private void queryLiquiGroupMargin(RoutingContext routingContext) {
        LOG.trace("Received queryLiquiGroupMargin request");
        this.createResponse(routingContext, LIQUI_GROUP_MARGIN_MODEL);
    }

    private void queryLiquiGroupSplitMargin(RoutingContext routingContext) {
        LOG.trace("Received queryLiquiGroupSplitMargin request");
        this.createResponse(routingContext, LIQUI_GROUP_SPLIT_MARGIN_MODEL);
    }

    private void queryPoolMargin(RoutingContext routingContext) {
        LOG.trace("Received queryPoolMargin request");
        this.createResponse(routingContext, POOL_MARGIN_MODEL);
    }

    private void queryPositionReport(RoutingContext routingContext) {
        LOG.trace("Received queryPositionReport request");
        this.createResponse(routingContext, POSITION_REPORT_MODEL);
    }

    private void queryRiskLimitUtilization(RoutingContext routingContext) {
        LOG.trace("Received queryRiskLimitUtilization request");
        this.createResponse(routingContext, RISK_LIMIT_UTILIZATION_MODEL);
    }

    public void close(Handler<AsyncResult<Void>> completionHandler) {
        LOG.info("Shutting down webserver");
        server.close(completionHandler);
    }

    private void createResponse(RoutingContext context, AbstractModel model) {
        RequestType requestType = context.request().uri().contains("/history")
                ? RequestType.HISTORY
                : RequestType.LATEST;

        String response = new JsonArray().add(new JsonObject()
                .put("model", model.getClass().getSimpleName())
                .put("requestType", requestType)
                .mergeIn(paramsToJson(context.request().params(), model))).encodePrettily();

        context.response().setStatusCode(getStatusCode()).end(response);
    }

    private JsonObject paramsToJson(MultiMap params, AbstractModel model) {
        Map<String, Class<?>> keyDescriptor = model.getKeysDescriptor();

        JsonObject json = new JsonObject();
        params.forEach(entry -> {
            final String param = entry.getKey();
            Class<?> convertTo = keyDescriptor.containsKey(param) ? keyDescriptor.get(param) : String.class;
            json.put(param, convertValue(entry.getValue(), convertTo));
        });
        return json;
    }

    private <T> T convertValue(String value, Class<T> clazz) {
        if (clazz.equals(String.class)) {
            return clazz.cast(value);
        } else if (clazz.equals(Integer.class)) {
            return clazz.cast(Integer.parseInt(value));
        } else if (clazz.equals(Double.class)) {
            return clazz.cast(Double.parseDouble(value));
        } else {
            throw new AssertionError("Unsupported type " + clazz);
        }
    }

    private int getStatusCode() {
        return health ? HttpResponseStatus.OK.code(): HttpResponseStatus.SERVICE_UNAVAILABLE.code();
    }
}
