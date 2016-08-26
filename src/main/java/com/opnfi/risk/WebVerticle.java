package com.opnfi.risk;

import com.opnfi.risk.auth.ApiAuthHandler;
import com.opnfi.risk.auth.ApiAuthHandlerImpl;
import com.opnfi.risk.auth.JsonLoginHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.mongo.HashSaltStyle;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schojak on 19.8.16.
 */
public class WebVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(WebVerticle.class);
    private static final Integer DEFAULT_HTTP_PORT = 8080;
    private static final Boolean DEFAULT_SSL = false;
    private static final String DEFAULT_SSL_KEYSTORE = "";
    private static final String DEFAULT_SSL_KEYSTORE_PASSWORD = "";
    private static final Boolean DEFAULT_CORS = false;
    private static final String DEFAULT_CORS_ORIGIN = "*";

    private static final String DEFAULT_AUTH_DB_NAME = "OpnFi-Risk";
    private static final String DEFAULT_AUTH_CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DEFAULT_SALT = "OpnFiRisk";

    private HttpServer server;
    private EventBus eb;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        LOG.info("Starting {} with configuration: {}", WebVerticle.class.getSimpleName(), config().encodePrettily());
        eb = vertx.eventBus();

        List<Future> futures = new ArrayList<>();
        futures.add(startWebServer());

        CompositeFuture.all(futures).setHandler(ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(ar.cause());
            }
        });
    }

    private AuthProvider createAuthenticationProvider() {
        JsonObject config = new JsonObject();
        LOG.info("Auth config: ", config().getJsonObject("auth").encodePrettily());
        config.put("db_name", config()
                .getJsonObject("auth")
                .getString("db_name", WebVerticle.DEFAULT_AUTH_DB_NAME));
        config.put("useObjectId", true);
        config.put("connection_string", config()
                .getJsonObject("auth")
                .getString("connection_string", WebVerticle.DEFAULT_AUTH_CONNECTION_STRING));
        MongoClient client = MongoClient.createShared(vertx, config);

        JsonObject authProperties = new JsonObject();
        MongoAuth authProvider = MongoAuth.create(client, authProperties);
        authProvider.getHashStrategy().setSaltStyle(HashSaltStyle.EXTERNAL);
        authProvider.getHashStrategy().setExternalSalt(config().getJsonObject("auth").getString("salt", WebVerticle.DEFAULT_SALT));
        return authProvider;
    }

    private Future<HttpServer> startWebServer() {
        Future<HttpServer> webServerFuture = Future.future();
        Router router = Router.router(vertx);

        if (config().getJsonObject("CORS", new JsonObject()).getBoolean("allow", WebVerticle.DEFAULT_CORS)) {
            LOG.info("Enabling CORS handler");

            //Wildcard(*) not allowed if allowCredentials is true
            CorsHandler corsHandler = CorsHandler.create(config().getJsonObject("CORS", new JsonObject()).getString("origin", WebVerticle.DEFAULT_CORS_ORIGIN));
            corsHandler.allowCredentials(true);
            corsHandler.allowedMethod(HttpMethod.OPTIONS);
            corsHandler.allowedMethod(HttpMethod.GET);
            corsHandler.allowedMethod(HttpMethod.POST);
            corsHandler.allowedMethod(HttpMethod.DELETE);
            corsHandler.allowedHeader("Authorization");
            corsHandler.allowedHeader("www-authenticate");
            corsHandler.allowedHeader("Content-Type");

            router.route().handler(corsHandler);
        }

        router.route().handler(CookieHandler.create());
        router.route().handler(BodyHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
        AuthProvider authenticationProvider = this.createAuthenticationProvider();
        router.route().handler(UserSessionHandler.create(authenticationProvider));

        LOG.info("Adding route REST API");
        router.route("/api/v1.0/*").handler(BodyHandler.create());
        router.routeWithRegex("^/api/v1.0/(?!user/login).*$").handler(ApiAuthHandler.create(authenticationProvider));
        router.post("/api/v1.0/user/login").handler(JsonLoginHandler.create(authenticationProvider));
        router.get("/api/v1.0/user/logout").handler(this::logoutUser);
        router.get("/api/v1.0/user/loginStatus").handler(this::loginStatus);
        router.get("/api/v1.0/latest/tss").handler(this::latestTradingSessionStatus);
        router.get("/api/v1.0/history/tss").handler(this::historyTradingSessionStatus);
        router.get("/api/v1.0/latest/mc").handler(this::latestMarginComponent);
        router.get("/api/v1.0/latest/mc/:clearer").handler(this::latestMarginComponent);
        router.get("/api/v1.0/latest/mc/:clearer/:member").handler(this::latestMarginComponent);
        router.get("/api/v1.0/latest/mc/:clearer/:member/:account").handler(this::latestMarginComponent);
        router.get("/api/v1.0/latest/mc/:clearer/:member/:account/:clss").handler(this::latestMarginComponent);
        router.get("/api/v1.0/latest/mc/:clearer/:member/:account/:clss/:ccy").handler(this::latestMarginComponent);
        router.get("/api/v1.0/history/mc/:clearer").handler(this::historyMarginComponent);
        router.get("/api/v1.0/history/mc/:clearer/:member").handler(this::historyMarginComponent);
        router.get("/api/v1.0/history/mc/:clearer/:member/:account").handler(this::historyMarginComponent);
        router.get("/api/v1.0/history/mc/:clearer/:member/:account/:clss").handler(this::historyMarginComponent);
        router.get("/api/v1.0/history/mc/:clearer/:member/:account/:clss/:ccy").handler(this::historyMarginComponent);
        router.get("/api/v1.0/latest/tmr").handler(this::latestTotalMarginRequirement);
        router.get("/api/v1.0/latest/tmr/:clearer").handler(this::latestTotalMarginRequirement);
        router.get("/api/v1.0/latest/tmr/:clearer/:pool").handler(this::latestTotalMarginRequirement);
        router.get("/api/v1.0/latest/tmr/:clearer/:pool/:member").handler(this::latestTotalMarginRequirement);
        router.get("/api/v1.0/latest/tmr/:clearer/:pool/:member/:account").handler(this::latestTotalMarginRequirement);
        router.get("/api/v1.0/latest/tmr/:clearer/:pool/:member/:account/:ccy").handler(this::latestTotalMarginRequirement);
        router.get("/api/v1.0/history/tmr/:clearer").handler(this::historyTotalMarginRequirement);
        router.get("/api/v1.0/history/tmr/:clearer/:pool").handler(this::historyTotalMarginRequirement);
        router.get("/api/v1.0/history/tmr/:clearer/:pool/:member").handler(this::historyTotalMarginRequirement);
        router.get("/api/v1.0/history/tmr/:clearer/:pool/:member/:account").handler(this::historyTotalMarginRequirement);
        router.get("/api/v1.0/history/tmr/:clearer/:pool/:member/:account/:ccy").handler(this::historyTotalMarginRequirement);
        router.get("/api/v1.0/latest/mss").handler(this::latestMarginShortfallSurplus);
        router.get("/api/v1.0/latest/mss/:clearer").handler(this::latestMarginShortfallSurplus);
        router.get("/api/v1.0/latest/mss/:clearer/:pool").handler(this::latestMarginShortfallSurplus);
        router.get("/api/v1.0/latest/mss/:clearer/:pool/:member").handler(this::latestMarginShortfallSurplus);
        router.get("/api/v1.0/latest/mss/:clearer/:pool/:member/:clearingCcy").handler(this::latestMarginShortfallSurplus);
        router.get("/api/v1.0/latest/mss/:clearer/:pool/:member/:clearingCcy/:ccy").handler(this::latestMarginShortfallSurplus);
        router.get("/api/v1.0/history/mss/:clearer").handler(this::historyMarginShortfallSurplus);
        router.get("/api/v1.0/history/mss/:clearer/:pool").handler(this::historyMarginShortfallSurplus);
        router.get("/api/v1.0/history/mss/:clearer/:pool/:member").handler(this::historyMarginShortfallSurplus);
        router.get("/api/v1.0/history/mss/:clearer/:pool/:member/:clearingCcy").handler(this::historyMarginShortfallSurplus);
        router.get("/api/v1.0/history/mss/:clearer/:pool/:member/:clearingCcy/:ccy").handler(this::historyMarginShortfallSurplus);

        router.route("/*").handler(StaticHandler.create("webroot"));

        LOG.info("Starting web server on port {}", config().getInteger("httpPort", WebVerticle.DEFAULT_HTTP_PORT));

        HttpServerOptions httpOptions = new HttpServerOptions();

        if (config().getBoolean("ssl", DEFAULT_SSL))
        {
            LOG.info("Enabling SSL on webserver");
            httpOptions.setSsl(true).setKeyStoreOptions(new JksOptions().setPassword(config().getString("keystorePassword", DEFAULT_SSL_KEYSTORE_PASSWORD)).setPath(config().getString("keystore", DEFAULT_SSL_KEYSTORE)));
        }

        server = vertx.createHttpServer(httpOptions)
                .requestHandler(router::accept)
                .listen(config().getInteger("httpPort", WebVerticle.DEFAULT_HTTP_PORT), webServerFuture.completer());
        return webServerFuture;
    }

    private void logoutUser(RoutingContext routingContext) {
        routingContext.clearUser();
        routingContext.response().putHeader("location", "/").setStatusCode(HttpResponseStatus.FOUND.code()).end();
    }

    private void loginStatus(RoutingContext routingContext) {
        if (routingContext.user() != null) {
            JsonObject resp = new JsonObject().put("username", routingContext.user().principal().getString("username"));

            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(resp.encodePrettily());
        }
        else
        {
            //routingContext.fail(HttpResponseStatus.FORBIDDEN.code());
            // Return success to avoid triggering HTTP 410 from repeated calls
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(new JsonObject()));
        }
    }

    private void latestTradingSessionStatus(RoutingContext routingContext) {
        LOG.trace("Received latest/tss request");

        eb.send("query.latestTradingSessionStatus", null, ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response to latest/tss request");

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end((String)ar.result().body());
            }
            else
            {
                LOG.error("Failed to query the DB service", ar.cause());
            }
        });
    }

    private void historyTradingSessionStatus(RoutingContext routingContext) {
        LOG.trace("Received history/tss request");

        eb.send("query.historyTradingSessionStatus", null, ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response to history/tss request");

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end((String)ar.result().body());
            }
            else
            {
                LOG.error("Failed to query the DB service", ar.cause());
            }
        });
    }

    private void latestMarginComponent(RoutingContext routingContext) {
        LOG.trace("Received latest/mc request");

        JsonObject params = new JsonObject();

        if (routingContext.request().getParam("clearer") != null && !"*".equals(routingContext.request().getParam("clearer")))
        {
            params.put("clearer", routingContext.request().getParam("clearer"));
        }

        if (routingContext.request().getParam("member") != null && !"*".equals(routingContext.request().getParam("member")))
        {
            params.put("member", routingContext.request().getParam("member"));
        }

        if (routingContext.request().getParam("account") != null && !"*".equals(routingContext.request().getParam("account")))
        {
            params.put("account", routingContext.request().getParam("account"));
        }

        if (routingContext.request().getParam("clss") != null && !"*".equals(routingContext.request().getParam("clss")))
        {
            params.put("clss", routingContext.request().getParam("clss"));
        }

        if (routingContext.request().getParam("ccy") != null && !"*".equals(routingContext.request().getParam("ccy")))
        {
            params.put("ccy", routingContext.request().getParam("ccy"));
        }

        eb.send("query.latestMarginComponent", params, ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response latest/mc request");

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end((String)ar.result().body());
            }
            else
            {
                LOG.error("Failed to query the DB service", ar.cause());
            }
        });
    }

    private void historyMarginComponent(RoutingContext routingContext) {
        LOG.trace("Received history/mc request");

        JsonObject params = new JsonObject();

        if (routingContext.request().getParam("clearer") != null && !"*".equals(routingContext.request().getParam("clearer")))
        {
            params.put("clearer", routingContext.request().getParam("clearer"));
        }

        if (routingContext.request().getParam("member") != null && !"*".equals(routingContext.request().getParam("member")))
        {
            params.put("member", routingContext.request().getParam("member"));
        }

        if (routingContext.request().getParam("account") != null && !"*".equals(routingContext.request().getParam("account")))
        {
            params.put("account", routingContext.request().getParam("account"));
        }

        if (routingContext.request().getParam("clss") != null && !"*".equals(routingContext.request().getParam("clss")))
        {
            params.put("clss", routingContext.request().getParam("clss"));
        }

        if (routingContext.request().getParam("ccy") != null && !"*".equals(routingContext.request().getParam("ccy")))
        {
            params.put("ccy", routingContext.request().getParam("ccy"));
        }

        eb.send("query.historyMarginComponent", params, ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response history/mc request");

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end((String)ar.result().body());
            }
            else
            {
                LOG.error("Failed to query the DB service", ar.cause());
            }
        });
    }

    private void latestTotalMarginRequirement(RoutingContext routingContext) {
        LOG.trace("Received latest/tmr request");

        JsonObject params = new JsonObject();

        if (routingContext.request().getParam("clearer") != null && !"*".equals(routingContext.request().getParam("clearer")))
        {
            params.put("clearer", routingContext.request().getParam("clearer"));
        }

        if (routingContext.request().getParam("pool") != null && !"*".equals(routingContext.request().getParam("pool")))
        {
            params.put("pool", routingContext.request().getParam("pool"));
        }

        if (routingContext.request().getParam("member") != null && !"*".equals(routingContext.request().getParam("member")))
        {
            params.put("member", routingContext.request().getParam("member"));
        }

        if (routingContext.request().getParam("account") != null && !"*".equals(routingContext.request().getParam("account")))
        {
            params.put("account", routingContext.request().getParam("account"));
        }

        if (routingContext.request().getParam("ccy") != null && !"*".equals(routingContext.request().getParam("ccy")))
        {
            params.put("ccy", routingContext.request().getParam("ccy"));
        }

        eb.send("query.latestTotalMarginRequirement", params, ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response latest/tmr request");

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end((String)ar.result().body());
            }
            else
            {
                LOG.error("Failed to query the DB service", ar.cause());
            }
        });
    }

    private void historyTotalMarginRequirement(RoutingContext routingContext) {
        LOG.trace("Received history/tmr request");

        JsonObject params = new JsonObject();

        if (routingContext.request().getParam("clearer") != null && !"*".equals(routingContext.request().getParam("clearer")))
        {
            params.put("clearer", routingContext.request().getParam("clearer"));
        }

        if (routingContext.request().getParam("pool") != null && !"*".equals(routingContext.request().getParam("pool")))
        {
            params.put("pool", routingContext.request().getParam("pool"));
        }

        if (routingContext.request().getParam("member") != null && !"*".equals(routingContext.request().getParam("member")))
        {
            params.put("member", routingContext.request().getParam("member"));
        }

        if (routingContext.request().getParam("account") != null && !"*".equals(routingContext.request().getParam("account")))
        {
            params.put("account", routingContext.request().getParam("account"));
        }

        if (routingContext.request().getParam("ccy") != null && !"*".equals(routingContext.request().getParam("ccy")))
        {
            params.put("ccy", routingContext.request().getParam("ccy"));
        }

        eb.send("query.historyTotalMarginRequirement", params, ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response history/tmr request");

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end((String)ar.result().body());
            }
            else
            {
                LOG.error("Failed to query the DB service", ar.cause());
            }
        });
    }

    private void latestMarginShortfallSurplus(RoutingContext routingContext) {
        LOG.trace("Received latest/mss request");

        JsonObject params = new JsonObject();

        if (routingContext.request().getParam("clearer") != null && !"*".equals(routingContext.request().getParam("clearer")))
        {
            params.put("clearer", routingContext.request().getParam("clearer"));
        }

        if (routingContext.request().getParam("pool") != null && !"*".equals(routingContext.request().getParam("pool")))
        {
            params.put("pool", routingContext.request().getParam("pool"));
        }

        if (routingContext.request().getParam("member") != null && !"*".equals(routingContext.request().getParam("member")))
        {
            params.put("member", routingContext.request().getParam("member"));
        }

        if (routingContext.request().getParam("clearingCcy") != null && !"*".equals(routingContext.request().getParam("clearingCcy")))
        {
            params.put("clearingCcy", routingContext.request().getParam("clearingCcy"));
        }

        if (routingContext.request().getParam("ccy") != null && !"*".equals(routingContext.request().getParam("ccy")))
        {
            params.put("ccy", routingContext.request().getParam("ccy"));
        }

        eb.send("query.latestMarginShortfallSurplus", params, ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response latest/mss request");

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end((String)ar.result().body());
            }
            else
            {
                LOG.error("Failed to query the DB service", ar.cause());
            }
        });
    }

    private void historyMarginShortfallSurplus(RoutingContext routingContext) {
        LOG.trace("Received history/mss request");

        JsonObject params = new JsonObject();

        if (routingContext.request().getParam("clearer") != null && !"*".equals(routingContext.request().getParam("clearer")))
        {
            params.put("clearer", routingContext.request().getParam("clearer"));
        }

        if (routingContext.request().getParam("pool") != null && !"*".equals(routingContext.request().getParam("pool")))
        {
            params.put("pool", routingContext.request().getParam("pool"));
        }

        if (routingContext.request().getParam("member") != null && !"*".equals(routingContext.request().getParam("member")))
        {
            params.put("member", routingContext.request().getParam("member"));
        }

        if (routingContext.request().getParam("clearingCcy") != null && !"*".equals(routingContext.request().getParam("clearingCcy")))
        {
            params.put("clearingCcy", routingContext.request().getParam("clearingCcy"));
        }

        if (routingContext.request().getParam("ccy") != null && !"*".equals(routingContext.request().getParam("ccy")))
        {
            params.put("ccy", routingContext.request().getParam("ccy"));
        }

        eb.send("query.historyMarginShortfallSurplus", params, ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response history/mss request");

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end((String)ar.result().body());
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
