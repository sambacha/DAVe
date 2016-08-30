package com.opnfi.risk;

import com.opnfi.risk.auth.ApiAuthHandler;
import com.opnfi.risk.restapi.ers.MarginComponentApi;
import com.opnfi.risk.restapi.ers.MarginShortfallSurplusApi;
import com.opnfi.risk.restapi.ers.TotalMarginRequirementApi;
import com.opnfi.risk.restapi.ers.TradingSessionStatusApi;
import com.opnfi.risk.restapi.user.UserApi;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
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
public class WebVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(WebVerticle.class);

    private static final Integer DEFAULT_HTTP_PORT = 8080;

    private static final Boolean DEFAULT_SSL = false;
    private static final String DEFAULT_SSL_KEYSTORE = "";
    private static final String DEFAULT_SSL_KEYSTORE_PASSWORD = "";

    private static final Boolean DEFAULT_CORS = false;
    private static final String DEFAULT_CORS_ORIGIN = "*";

    private static final Boolean DEFAULT_COMPRESSION = true;

    private static final Boolean DEFAULT_AUTH_ENABLED = false;
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
        LOG.info("Auth config: {}", config().getJsonObject("auth").encodePrettily());
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

        if (config().getJsonObject("CORS", new JsonObject()).getBoolean("enable", WebVerticle.DEFAULT_CORS)) {
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

        UserApi userApi = null;

        if (config().getJsonObject("auth", new JsonObject()).getBoolean("enable", WebVerticle.DEFAULT_AUTH_ENABLED)) {
            LOG.info("Enabling authentication");

            AuthProvider authenticationProvider = this.createAuthenticationProvider();

            router.route().handler(CookieHandler.create());
            router.route().handler(BodyHandler.create());
            router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
            router.route().handler(UserSessionHandler.create(authenticationProvider));
            router.routeWithRegex("^/api/v1.0/(?!user/login).*$").handler(ApiAuthHandler.create(authenticationProvider));

            userApi = new UserApi(authenticationProvider);
        }
        else
        {
            userApi = new UserApi(null);
        }

        TradingSessionStatusApi tssApi = new TradingSessionStatusApi(eb);
        MarginComponentApi mcApi = new MarginComponentApi(eb);
        TotalMarginRequirementApi tmrApi = new TotalMarginRequirementApi(eb);
        MarginShortfallSurplusApi mssApi = new MarginShortfallSurplusApi(eb);


        LOG.info("Adding route REST API");
        router.route("/api/v1.0/*").handler(BodyHandler.create());
        router.post("/api/v1.0/user/login").handler(userApi::login);
        router.get("/api/v1.0/user/logout").handler(userApi::logout);
        router.get("/api/v1.0/user/loginStatus").handler(userApi::loginStatus);
        router.get("/api/v1.0/latest/tss").handler(tssApi::latestTradingSessionStatus);
        router.get("/api/v1.0/history/tss").handler(tssApi::historyTradingSessionStatus);
        router.get("/api/v1.0/latest/mc").handler(mcApi::latestMarginComponent);
        router.get("/api/v1.0/latest/mc/:clearer").handler(mcApi::latestMarginComponent);
        router.get("/api/v1.0/latest/mc/:clearer/:member").handler(mcApi::latestMarginComponent);
        router.get("/api/v1.0/latest/mc/:clearer/:member/:account").handler(mcApi::latestMarginComponent);
        router.get("/api/v1.0/latest/mc/:clearer/:member/:account/:clss").handler(mcApi::latestMarginComponent);
        router.get("/api/v1.0/latest/mc/:clearer/:member/:account/:clss/:ccy").handler(mcApi::latestMarginComponent);
        router.get("/api/v1.0/history/mc/:clearer").handler(mcApi::historyMarginComponent);
        router.get("/api/v1.0/history/mc/:clearer/:member").handler(mcApi::historyMarginComponent);
        router.get("/api/v1.0/history/mc/:clearer/:member/:account").handler(mcApi::historyMarginComponent);
        router.get("/api/v1.0/history/mc/:clearer/:member/:account/:clss").handler(mcApi::historyMarginComponent);
        router.get("/api/v1.0/history/mc/:clearer/:member/:account/:clss/:ccy").handler(mcApi::historyMarginComponent);
        router.get("/api/v1.0/latest/tmr").handler(tmrApi::latestTotalMarginRequirement);
        router.get("/api/v1.0/latest/tmr/:clearer").handler(tmrApi::latestTotalMarginRequirement);
        router.get("/api/v1.0/latest/tmr/:clearer/:pool").handler(tmrApi::latestTotalMarginRequirement);
        router.get("/api/v1.0/latest/tmr/:clearer/:pool/:member").handler(tmrApi::latestTotalMarginRequirement);
        router.get("/api/v1.0/latest/tmr/:clearer/:pool/:member/:account").handler(tmrApi::latestTotalMarginRequirement);
        router.get("/api/v1.0/latest/tmr/:clearer/:pool/:member/:account/:ccy").handler(tmrApi::latestTotalMarginRequirement);
        router.get("/api/v1.0/history/tmr/:clearer").handler(tmrApi::historyTotalMarginRequirement);
        router.get("/api/v1.0/history/tmr/:clearer/:pool").handler(tmrApi::historyTotalMarginRequirement);
        router.get("/api/v1.0/history/tmr/:clearer/:pool/:member").handler(tmrApi::historyTotalMarginRequirement);
        router.get("/api/v1.0/history/tmr/:clearer/:pool/:member/:account").handler(tmrApi::historyTotalMarginRequirement);
        router.get("/api/v1.0/history/tmr/:clearer/:pool/:member/:account/:ccy").handler(tmrApi::historyTotalMarginRequirement);
        router.get("/api/v1.0/latest/mss").handler(mssApi::latestMarginShortfallSurplus);
        router.get("/api/v1.0/latest/mss/:clearer").handler(mssApi::latestMarginShortfallSurplus);
        router.get("/api/v1.0/latest/mss/:clearer/:pool").handler(mssApi::latestMarginShortfallSurplus);
        router.get("/api/v1.0/latest/mss/:clearer/:pool/:member").handler(mssApi::latestMarginShortfallSurplus);
        router.get("/api/v1.0/latest/mss/:clearer/:pool/:member/:clearingCcy").handler(mssApi::latestMarginShortfallSurplus);
        router.get("/api/v1.0/latest/mss/:clearer/:pool/:member/:clearingCcy/:ccy").handler(mssApi::latestMarginShortfallSurplus);
        router.get("/api/v1.0/history/mss/:clearer").handler(mssApi::historyMarginShortfallSurplus);
        router.get("/api/v1.0/history/mss/:clearer/:pool").handler(mssApi::historyMarginShortfallSurplus);
        router.get("/api/v1.0/history/mss/:clearer/:pool/:member").handler(mssApi::historyMarginShortfallSurplus);
        router.get("/api/v1.0/history/mss/:clearer/:pool/:member/:clearingCcy").handler(mssApi::historyMarginShortfallSurplus);
        router.get("/api/v1.0/history/mss/:clearer/:pool/:member/:clearingCcy/:ccy").handler(mssApi::historyMarginShortfallSurplus);

        router.route("/*").handler(StaticHandler.create("webroot"));

        LOG.info("Starting web server on port {}", config().getInteger("httpPort", WebVerticle.DEFAULT_HTTP_PORT));

        HttpServerOptions httpOptions = new HttpServerOptions();

        if (config().getJsonObject("ssl", new JsonObject()).getBoolean("enable", DEFAULT_SSL))
        {
            LOG.info("Enabling SSL on webserver");
            httpOptions.setSsl(true).setKeyStoreOptions(new JksOptions().setPassword(config().getJsonObject("ssl", new JsonObject()).getString("keystorePassword", DEFAULT_SSL_KEYSTORE_PASSWORD)).setPath(config().getJsonObject("ssl", new JsonObject()).getString("keystore", DEFAULT_SSL_KEYSTORE)));
        }

        if (config().getBoolean("compression", DEFAULT_COMPRESSION))
        {
            LOG.info("Enabling compression on webserver");
            httpOptions.setCompressionSupported(config().getBoolean("compression", DEFAULT_COMPRESSION));
        }

        server = vertx.createHttpServer(httpOptions)
                .requestHandler(router::accept)
                .listen(config().getInteger("httpPort", WebVerticle.DEFAULT_HTTP_PORT), webServerFuture.completer());
        return webServerFuture;
    }

    @Override
    public void stop() throws Exception {
        LOG.info("Shutting down webserver");
        server.close();
    }
}
