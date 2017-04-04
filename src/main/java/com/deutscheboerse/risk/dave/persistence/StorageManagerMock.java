package com.deutscheboerse.risk.dave.persistence;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class StorageManagerMock {
    private static final Logger LOG = LoggerFactory.getLogger(StorageManagerMock.class);

    private static final Integer DEFAULT_PORT = 8080;

    private final Vertx vertx;
    private final JsonObject config;
    private final HttpServer server;
    private boolean health = true;

    StorageManagerMock(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
        this.server = this.createHttpServer();
    }

    StorageManagerMock listen(Handler<AsyncResult<Void>> resultHandler) {

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
        this.createResponse(routingContext);
    }

    private void queryLiquiGroupMargin(RoutingContext routingContext) {
        LOG.trace("Received queryLiquiGroupMargin request");
        this.createResponse(routingContext);
    }

    private void queryLiquiGroupSplitMargin(RoutingContext routingContext) {
        LOG.trace("Received queryLiquiGroupSplitMargin request");
        this.createResponse(routingContext);
    }

    private void queryPoolMargin(RoutingContext routingContext) {
        LOG.trace("Received queryPoolMargin request");
        this.createResponse(routingContext);
    }

    private void queryPositionReport(RoutingContext routingContext) {
        LOG.trace("Received queryPositionReport request");
        this.createResponse(routingContext);
    }

    private void queryRiskLimitUtilization(RoutingContext routingContext) {
        LOG.trace("Received queryRiskLimitUtilization request");
        this.createResponse(routingContext);
    }

    public void close(Handler<AsyncResult<Void>> completionHandler) {
        LOG.info("Shutting down webserver");
        server.close(completionHandler);
    }

    private void createResponse(RoutingContext context) {
        context.response().setStatusCode(getStatusCode()).end(context.request().params().toString());
    }

    private int getStatusCode() {
        return health ? HttpResponseStatus.OK.code(): HttpResponseStatus.SERVICE_UNAVAILABLE.code();
    }
}
