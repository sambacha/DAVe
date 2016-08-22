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
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
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

    private TradingSessionStatus cacheTss = null;

    @Override
    public void start(Future<Void> fut) throws Exception {
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
        router.get("/api/v1.0/tss").handler(this::tss);
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
        EventBus eb = vertx.eventBus();

        eb.consumer("ers.TradingSessionStatus", message -> cacheTradingSessionStatus(message));

        fut.complete();
    }

    private void cacheTradingSessionStatus(Message msg)
    {
        LOG.trace("Caching TSS message with body: " + msg.body().toString());
        cacheTss = Json.decodeValue(msg.body().toString(), TradingSessionStatus.class);
    }

    private void tss(RoutingContext routingContext) {
        LOG.trace("Received tss request");

        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(cacheTss));
    }

    @Override
    public void stop() throws Exception {
        LOG.info("Shutting down webserver");
        server.close();
    }
}
