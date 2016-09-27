package com.deutscheboerse.risk.dave.utils;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.Router;

/**
 * Created by schojak on 27.9.16.
 */
public class DummyWebServer {
    public static HttpServer startWebserver(TestContext context, Vertx vertx, int port, String uri, String dummyContent)
    {
        Async webserverStartup = context.async();

        Router router = Router.router(vertx);
        router.route(uri).handler(req -> {
            req.response().setStatusCode(HttpResponseStatus.OK.code()).putHeader("content-type", "text/plain; charset=utf-8").end(dummyContent);
        });

        HttpServer server = vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, res -> {
                    if (res.succeeded())
                    {
                        webserverStartup.complete();
                    }
                    else
                    {
                        context.fail(res.cause());
                    }
                });

        webserverStartup.awaitSuccess();

        return server;
    }

    public static void stopWebserver(TestContext context, HttpServer server)
    {
        Async webserverShutdown = context.async();
        server.close(res -> {
            if (res.succeeded())
            {
                webserverShutdown.complete();
            }
            else
            {
                context.fail(res.cause());
            }
        });

        webserverShutdown.awaitSuccess();
    }
}
