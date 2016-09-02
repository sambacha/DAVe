package com.opnfi.risk;

import com.opnfi.risk.auth.ApiAuthHandler;
import com.opnfi.risk.restapi.ers.*;
import com.opnfi.risk.restapi.user.UserApi;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.mongo.HashSaltStyle;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
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
public class HttpVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(HttpVerticle.class);

    private static final Integer DEFAULT_HTTP_PORT = 8080;

    private static final Boolean DEFAULT_SSL = false;
    private static final Boolean DEFAULT_SSL_REQUIRE_CLIENT_AUTH = false;

    private static final Boolean DEFAULT_CORS = false;
    private static final String DEFAULT_CORS_ORIGIN = "*";

    private static final Boolean DEFAULT_COMPRESSION = true;

    private static final Boolean DEFAULT_AUTH_ENABLED = false;
    private static final String DEFAULT_AUTH_DB_NAME = "OpnFi-Risk";
    private static final String DEFAULT_AUTH_CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DEFAULT_AUTH_SALT = "OpnFiRisk";
    private static final Boolean DEFAULT_AUTH_CHECK_USER_AGAINST_CERTIFICATE = false;

    private HttpServer server;
    private EventBus eb;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        LOG.info("Starting {} with configuration: {}", HttpVerticle.class.getSimpleName(), config().encodePrettily());
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
        LOG.info("Auth config: {}", config().getJsonObject("auth").encodePrettily());
        config.put("db_name", config()
                .getJsonObject("auth")
                .getString("db_name", HttpVerticle.DEFAULT_AUTH_DB_NAME));
        config.put("useObjectId", true);
        config.put("connection_string", config()
                .getJsonObject("auth")
                .getString("connection_string", HttpVerticle.DEFAULT_AUTH_CONNECTION_STRING));
        MongoClient client = MongoClient.createShared(vertx, config);

        JsonObject authProperties = new JsonObject();
        MongoAuth authProvider = MongoAuth.create(client, authProperties);
        authProvider.getHashStrategy().setSaltStyle(HashSaltStyle.EXTERNAL);
        authProvider.getHashStrategy().setExternalSalt(config().getJsonObject("auth").getString("salt", HttpVerticle.DEFAULT_AUTH_SALT));
        return authProvider;
    }

    private Future<HttpServer> startWebServer() {
        Future<HttpServer> webServerFuture = Future.future();
        Router router = Router.router(vertx);

        if (config().getJsonObject("CORS", new JsonObject()).getBoolean("enable", HttpVerticle.DEFAULT_CORS)) {
            LOG.info("Enabling CORS handler");

            //Wildcard(*) not allowed if allowCredentials is true
            CorsHandler corsHandler = CorsHandler.create(config().getJsonObject("CORS", new JsonObject()).getString("origin", HttpVerticle.DEFAULT_CORS_ORIGIN));
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

        UserApi userApi;

        if (config().getJsonObject("auth", new JsonObject()).getBoolean("enable", HttpVerticle.DEFAULT_AUTH_ENABLED)) {
            LOG.info("Enabling authentication");

            AuthProvider authenticationProvider = this.createAuthenticationProvider();

            router.route().handler(CookieHandler.create());
            router.route().handler(BodyHandler.create());
            router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
            router.route().handler(UserSessionHandler.create(authenticationProvider));
            router.routeWithRegex("^/api/v1.0/(?!user/login).*$").handler(ApiAuthHandler.create(authenticationProvider));

            userApi = new UserApi(authenticationProvider, config().getJsonObject("auth").getBoolean("checkUserAgainstCertificate", HttpVerticle.DEFAULT_AUTH_CHECK_USER_AGAINST_CERTIFICATE));
        }
        else
        {
            userApi = new UserApi(null, false);
        }

        LOG.info("Adding route REST API");
        router.route("/api/v1.0/*").handler(BodyHandler.create());
        router.post("/api/v1.0/user/login").handler(userApi::login);
        router.get("/api/v1.0/user/logout").handler(userApi::logout);
        router.get("/api/v1.0/user/loginStatus").handler(userApi::loginStatus);

        router.mountSubRouter("/api/v1.0", this.createTssSubRoutes());
        router.mountSubRouter("/api/v1.0", this.createMcSubRoutes());
        router.mountSubRouter("/api/v1.0", this.createTmrSubRoutes());
        router.mountSubRouter("/api/v1.0", this.createMssSubRoutes());
        router.mountSubRouter("/api/v1.0", this.createPrSubRoutes());
        router.mountSubRouter("/api/v1.0", this.createRlSubRoutes());

        router.route("/*").handler(StaticHandler.create("webroot"));

        LOG.info("Starting web server on port {}", config().getInteger("httpPort", HttpVerticle.DEFAULT_HTTP_PORT));

        HttpServerOptions httpOptions = new HttpServerOptions();

        if (config().getJsonObject("ssl", new JsonObject()).getBoolean("enable", DEFAULT_SSL) && config().getJsonObject("ssl", new JsonObject()).getString("keystore") != null && config().getJsonObject("ssl", new JsonObject()).getString("keystorePassword") != null)
        {
            LOG.info("Enabling SSL on webserver");
            httpOptions.setSsl(true).setKeyStoreOptions(new JksOptions().setPassword(config().getJsonObject("ssl").getString("keystorePassword")).setPath(config().getJsonObject("ssl").getString("keystore")));

            if (config().getJsonObject("ssl", new JsonObject()).getString("truststore") != null && config().getJsonObject("ssl", new JsonObject()).getString("truststorePassword") != null)
            {
                LOG.info("Enabling SSL Client Authentication on webserver");
                httpOptions.setTrustStoreOptions(new JksOptions().setPassword(config().getJsonObject("ssl").getString("truststorePassword")).setPath(config().getJsonObject("ssl").getString("truststore")));

                if (config().getJsonObject("ssl").getBoolean("requireTLSClientAuth", DEFAULT_SSL_REQUIRE_CLIENT_AUTH))
                {
                    LOG.info("Setting SSL Client Authentication as required");
                    httpOptions.setClientAuth(ClientAuth.REQUIRED);
                }
                else
                {
                    httpOptions.setClientAuth(ClientAuth.REQUEST);
                }
            }
        }

        if (config().getBoolean("compression", DEFAULT_COMPRESSION))
        {
            LOG.info("Enabling compression on webserver");
            httpOptions.setCompressionSupported(config().getBoolean("compression", DEFAULT_COMPRESSION));
        }

        server = vertx.createHttpServer(httpOptions)
                .requestHandler(router::accept)
                .listen(config().getInteger("httpPort", HttpVerticle.DEFAULT_HTTP_PORT), webServerFuture.completer());
        return webServerFuture;
    }

    private Router createTssSubRoutes() {
        Router router = Router.router(vertx);
        TradingSessionStatusApi tssApi = new TradingSessionStatusApi(eb);
        router.get("/latest/tss").handler(tssApi::latestCall);
        router.get("/history/tss").handler(tssApi::historyCall);
        return router;
    }

    private Router createMcSubRoutes() {
        Router router = Router.router(vertx);
        MarginComponentApi mcApi = new MarginComponentApi(eb);
        router.get("/latest/mc").handler(mcApi::latestCall);
        router.get("/latest/mc/:clearer").handler(mcApi::latestCall);
        router.get("/latest/mc/:clearer/:member").handler(mcApi::latestCall);
        router.get("/latest/mc/:clearer/:member/:account").handler(mcApi::latestCall);
        router.get("/latest/mc/:clearer/:member/:account/:clss").handler(mcApi::latestCall);
        router.get("/latest/mc/:clearer/:member/:account/:clss/:ccy").handler(mcApi::latestCall);
        router.get("/history/mc").handler(mcApi::historyCall);
        router.get("/history/mc/:clearer").handler(mcApi::historyCall);
        router.get("/history/mc/:clearer/:member").handler(mcApi::historyCall);
        router.get("/history/mc/:clearer/:member/:account").handler(mcApi::historyCall);
        router.get("/history/mc/:clearer/:member/:account/:clss").handler(mcApi::historyCall);
        router.get("/history/mc/:clearer/:member/:account/:clss/:ccy").handler(mcApi::historyCall);
        return router;
    }

    private Router createTmrSubRoutes() {
        Router router = Router.router(vertx);
        TotalMarginRequirementApi tmrApi = new TotalMarginRequirementApi(eb);
        router.get("/latest/tmr").handler(tmrApi::latestCall);
        router.get("/latest/tmr/:clearer").handler(tmrApi::latestCall);
        router.get("/latest/tmr/:clearer/:pool").handler(tmrApi::latestCall);
        router.get("/latest/tmr/:clearer/:pool/:member").handler(tmrApi::latestCall);
        router.get("/latest/tmr/:clearer/:pool/:member/:account").handler(tmrApi::latestCall);
        router.get("/latest/tmr/:clearer/:pool/:member/:account/:ccy").handler(tmrApi::latestCall);
        router.get("/history/tmr").handler(tmrApi::historyCall);
        router.get("/history/tmr/:clearer").handler(tmrApi::historyCall);
        router.get("/history/tmr/:clearer/:pool").handler(tmrApi::historyCall);
        router.get("/history/tmr/:clearer/:pool/:member").handler(tmrApi::historyCall);
        router.get("/history/tmr/:clearer/:pool/:member/:account").handler(tmrApi::historyCall);
        router.get("/history/tmr/:clearer/:pool/:member/:account/:ccy").handler(tmrApi::historyCall);
        return router;
    }

    private Router createMssSubRoutes() {
        Router router = Router.router(vertx);
        MarginShortfallSurplusApi mssApi = new MarginShortfallSurplusApi(eb);
        router.get("/latest/mss").handler(mssApi::latestCall);
        router.get("/latest/mss/:clearer").handler(mssApi::latestCall);
        router.get("/latest/mss/:clearer/:pool").handler(mssApi::latestCall);
        router.get("/latest/mss/:clearer/:pool/:member").handler(mssApi::latestCall);
        router.get("/latest/mss/:clearer/:pool/:member/:clearingCcy").handler(mssApi::latestCall);
        router.get("/latest/mss/:clearer/:pool/:member/:clearingCcy/:ccy").handler(mssApi::latestCall);
        router.get("/history/mss").handler(mssApi::historyCall);
        router.get("/history/mss/:clearer").handler(mssApi::historyCall);
        router.get("/history/mss/:clearer/:pool").handler(mssApi::historyCall);
        router.get("/history/mss/:clearer/:pool/:member").handler(mssApi::historyCall);
        router.get("/history/mss/:clearer/:pool/:member/:clearingCcy").handler(mssApi::historyCall);
        router.get("/history/mss/:clearer/:pool/:member/:clearingCcy/:ccy").handler(mssApi::historyCall);
        return router;
    }

    private Router createPrSubRoutes() {
        Router router = Router.router(vertx);
        PositionReportApi prApi = new PositionReportApi(eb);
        router.get("/latest/pr").handler(prApi::latestCall);
        router.get("/latest/pr/:clearer").handler(prApi::latestCall);
        router.get("/latest/pr/:clearer/:member").handler(prApi::latestCall);
        router.get("/latest/pr/:clearer/:member/:account").handler(prApi::latestCall);
        router.get("/latest/pr/:clearer/:member/:account/:symbol").handler(prApi::latestCall);
        router.get("/latest/pr/:clearer/:member/:account/:symbol/:putCall").handler(prApi::latestCall);
        router.get("/latest/pr/:clearer/:member/:account/:symbol/:putCall/:strikePrice").handler(prApi::latestCall);
        router.get("/latest/pr/:clearer/:member/:account/:symbol/:putCall/:strikePrice/:optAttribute").handler(prApi::latestCall);
        router.get("/latest/pr/:clearer/:member/:account/:symbol/:putCall/:strikePrice/:optAttribute/:maturityMonthYear").handler(prApi::latestCall);
        router.get("/history/pr").handler(prApi::historyCall);
        router.get("/history/pr/:clearer").handler(prApi::historyCall);
        router.get("/history/pr/:clearer/:member").handler(prApi::historyCall);
        router.get("/history/pr/:clearer/:member/:account").handler(prApi::historyCall);
        router.get("/history/pr/:clearer/:member/:account/:symbol").handler(prApi::historyCall);
        router.get("/history/pr/:clearer/:member/:account/:symbol/:putCall").handler(prApi::historyCall);
        router.get("/history/pr/:clearer/:member/:account/:symbol/:putCall/:strikePrice").handler(prApi::historyCall);
        router.get("/history/pr/:clearer/:member/:account/:symbol/:putCall/:strikePrice/:optAttribute").handler(prApi::historyCall);
        router.get("/history/pr/:clearer/:member/:account/:symbol/:putCall/:strikePrice/:optAttribute/:maturityMonthYear").handler(prApi::historyCall);
        return router;
    }

    private Router createRlSubRoutes() {
        Router router = Router.router(vertx);
        RiskLimitApi rlApi = new RiskLimitApi(eb);
        router.get("/latest/rl").handler(rlApi::latestCall);
        router.get("/latest/rl/:clearer").handler(rlApi::latestCall);
        router.get("/latest/rl/:clearer/:member").handler(rlApi::latestCall);
        router.get("/latest/rl/:clearer/:member/:maintainer").handler(rlApi::latestCall);
        router.get("/latest/rl/:clearer/:member/:maintainer/:limitType").handler(rlApi::latestCall);
        router.get("/history/rl").handler(rlApi::historyCall);
        router.get("/history/rl/:clearer").handler(rlApi::historyCall);
        router.get("/history/rl/:clearer/:member").handler(rlApi::historyCall);
        router.get("/history/rl/:clearer/:member/:maintainer").handler(rlApi::historyCall);
        router.get("/history/rl/:clearer/:member/:maintainer/:limitType").handler(rlApi::historyCall);
        return router;
    }

    @Override
    public void stop() throws Exception {
        LOG.info("Shutting down webserver");
        server.close();
    }
}
