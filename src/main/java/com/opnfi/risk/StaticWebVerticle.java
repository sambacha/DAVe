package com.opnfi.risk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Created by schojak on 19.8.16.
 */
public class StaticWebVerticle extends AbstractVerticle {
    final static private Logger LOG = LoggerFactory.getLogger(StaticWebVerticle.class);

    private HttpServer server;

    @Override
    public void start(Future<Void> fut) throws Exception {
        Router router = Router.router(vertx);

        LOG.info("Adding route for static assets");
        router.route("/*").handler(StaticHandler.create("webroot"));

        LOG.info("Starting web server on port 8080");
        server = vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        8080,
                        res -> {
                            if (res.succeeded())
                            {
                                fut.complete();
                            }
                            else
                            {
                                fut.fail(res.cause());
                            }
                        }
                );
    }

    @Override
    public void stop() throws Exception {
        LOG.info("Shutting down webserver");
        server.close();
    }
}
