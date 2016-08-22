package com.opnfi.risk;

import com.opnfi.risk.model.TradingSessionStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Created by schojak on 19.8.16.
 */
public class WebVerticle extends AbstractVerticle {
    final static private Logger LOG = LoggerFactory.getLogger(WebVerticle.class);

    private HttpServer server;
    private EventBus eb;

    private TradingSessionStatus cacheTss = null;

    @Override
    public void start(Future<Void> fut) throws Exception {
        eb = vertx.eventBus();

        startWebServer(
                (nothing) -> startCache(
                        fut),
                fut);
    }

    private void startWebServer(Handler<AsyncResult<Void>> next, Future<Void> fut)
    {
        Router router = Router.router(vertx);

        LOG.info("Adding route REST API");
        router.route("/api/v1.0/*").handler(BodyHandler.create());
        router.get("/api/v1.0/latest/tss").handler(this::latestTradingSessionStatus);
        router.get("/api/v1.0/latest/mc").handler(this::latestMarginComponent);
        router.get("/api/v1.0/latest/mc/:clearer").handler(this::latestMarginComponent);
        router.get("/api/v1.0/latest/mc/:clearer/:member").handler(this::latestMarginComponent);
        router.get("/api/v1.0/latest/mc/:clearer/:member/:account").handler(this::latestMarginComponent);
        router.get("/api/v1.0/latest/mc/:clearer/:member/:account/:clss").handler(this::latestMarginComponent);
        router.get("/api/v1.0/latest/mc/:clearer/:member/:account/:clss/:ccy").handler(this::latestMarginComponent);
        /*router.post("/api/request").handler(this::request);
        router.get("/api/messages").handler(this::messages);
        router.get("/api/messages/:queueName").handler(this::messagesByQueue);
        router.get("/api/messages/:queueName/:correlationId").handler(this::messagesByQueueWithCorrelationId);*/
        router.route("/*").handler(StaticHandler.create("webroot"));

        LOG.info("Starting web server on port 8080");
        server = vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        8080,
                        res -> {
                            if (res.succeeded())
                            {
                                next.handle(fut);
                            }
                            else
                            {
                                fut.fail(res.cause());
                            }
                        }
                );
    }

    private void startCache(Future<Void> fut)
    {
        eb.consumer("ers.TradingSessionStatus", message -> cacheTradingSessionStatus(message));

        fut.complete();
    }

    private void cacheTradingSessionStatus(Message msg)
    {
        LOG.trace("Caching TSS message with body: " + msg.body().toString());
        cacheTss = Json.decodeValue(msg.body().toString(), TradingSessionStatus.class);
    }

    private void latestTradingSessionStatus(RoutingContext routingContext) {
        LOG.trace("Received latest/tss request");

        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(cacheTss));
    }

    private void latestMarginComponent(RoutingContext routingContext) {
        LOG.info("Received latest/mc request");

        eb.send("db.query.MarginComponent", new JsonArray(), ar -> {
            if (ar.succeeded()) {
                LOG.info("Received response latest/mc request");

                LOG.info("Received response for latest/mc query " + ar.result().body());

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(ar.result().body()));
            }
            else
            {
                LOG.error("Failed to query the DB service", ar.cause());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        LOG.info("Shutting down webserver");
        server.close();
    }
}
