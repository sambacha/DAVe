package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.restapi.margin.*;
import com.deutscheboerse.risk.dave.restapi.user.UserApi;
import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.mongo.HashSaltStyle;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schojak on 19.8.16.
 */
public class HttpVerticle extends AbstractVerticle
{

    private enum Mode
    {
        HTTP, HTTPS
    };
    private static final Logger LOG = LoggerFactory.getLogger(HttpVerticle.class);

    private static final Integer MAX_BODY_SIZE = 1024 * 1024; // 1MB

    private static final Integer DEFAULT_PORT = 8080;

    private static final Boolean DEFAULT_SSL = false;
    private static final Boolean DEFAULT_SSL_REQUIRE_CLIENT_AUTH = false;

    private static final Boolean DEFAULT_CORS = false;
    private static final String DEFAULT_CORS_ORIGIN = "*";

    private static final Boolean DEFAULT_CSRF = false;
    private static final String DEFAULT_CSRF_SECRET = "DAVe-CSRF-Secret";

    private static final Boolean DEFAULT_COMPRESSION = false;

    private static final Boolean DEFAULT_AUTH_ENABLED = false;
    private static final String DEFAULT_AUTH_DB_NAME = "DAVe";
    private static final String DEFAULT_AUTH_CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DEFAULT_AUTH_SALT = "DAVe";

    private HttpServer server;
    private Mode operationMode;
    private HealthCheck healthCheck;

    @Override
    public void start(Future<Void> startFuture) throws Exception
    {
        LOG.info("Starting {} with configuration: {}", HttpVerticle.class.getSimpleName(), config().encodePrettily());

        healthCheck = new HealthCheck(this.vertx);

        if (config().getJsonObject("ssl", new JsonObject()).getBoolean("enable", DEFAULT_SSL)) {
            this.operationMode = Mode.HTTPS;
        } else {
            this.operationMode = Mode.HTTP;
        }
        List<Future> futures = new ArrayList<>();
        futures.add(startHttpServer());

        CompositeFuture.all(futures).setHandler(ar ->
        {
            if (ar.succeeded())
            {
                healthCheck.setHttpState(true);
                startFuture.complete();
            }
            else
            {
                healthCheck.setHttpState(false);
                startFuture.fail(ar.cause());
            }
        });
    }

    private AuthProvider createMongoAuthenticationProvider()
    {
        JsonObject dbConfig = new JsonObject();
        LOG.info("Auth config: {}", config().getJsonObject("auth").encodePrettily());
        dbConfig.put("db_name", config()
                .getJsonObject("auth")
                .getString("dbName", HttpVerticle.DEFAULT_AUTH_DB_NAME));
        dbConfig.put("useObjectId", true);
        dbConfig.put("connection_string", config()
                .getJsonObject("auth")
                .getString("connectionUrl", HttpVerticle.DEFAULT_AUTH_CONNECTION_STRING));
        MongoClient client = MongoClient.createShared(vertx, dbConfig);

        JsonObject authProperties = new JsonObject();
        MongoAuth authProvider = MongoAuth.create(client, authProperties);
        authProvider.getHashStrategy().setSaltStyle(HashSaltStyle.EXTERNAL);
        authProvider.getHashStrategy().setExternalSalt(config().getJsonObject("auth").getString("salt", HttpVerticle.DEFAULT_AUTH_SALT));
        return authProvider;
    }

    private Future<HttpServer> startHttpServer()
    {
        Future<HttpServer> webServerFuture = Future.future();
        Router router = configureRouter();
        HttpServerOptions httpOptions = configureWebServer();

        int port = config().getInteger("port", HttpVerticle.DEFAULT_PORT);
        LOG.info("Starting web server on port {}", port);
        server = vertx.createHttpServer(httpOptions)
                .requestHandler(router::accept)
                .listen(port, webServerFuture.completer());

        return webServerFuture;
    }

    private HttpServerOptions configureWebServer()
    {
        HttpServerOptions httpOptions = new HttpServerOptions();
        setSsl(httpOptions);
        setCompression(httpOptions);
        return httpOptions;
    }

    private void setSsl(HttpServerOptions httpOptions)
    {
        if (!this.operationMode.equals(HttpVerticle.Mode.HTTPS))
        {
            return;
        }
        if (config().getJsonObject("ssl", new JsonObject()).getBoolean("enable", DEFAULT_SSL) && config().getJsonObject("ssl", new JsonObject()).getString("keystore") != null && config().getJsonObject("ssl", new JsonObject()).getString("keystorePassword") != null)
        {
            LOG.info("Enabling SSL on webserver");
            httpOptions.setSsl(true).setKeyStoreOptions(new JksOptions().setPassword(config().getJsonObject("ssl").getString("keystorePassword")).setPath(config().getJsonObject("ssl").getString("keystore")));

            setSslClientAuthentication(httpOptions);
        }
    }

    private void setSslClientAuthentication(HttpServerOptions httpOptions)
    {
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

    private void setCompression(HttpServerOptions httpOptions)
    {
        if (config().getBoolean("compression", DEFAULT_COMPRESSION))
        {
            LOG.info("Enabling compression on webserver");
            httpOptions.setCompressionSupported(true);
        }

    }

    private Router configureRouter()
    {
        Router router = Router.router(vertx);

        setCorsHandler(router);
        UserApi userApi = setAuthHandler(router);

        LOG.info("Adding route REST API");
        router.get("/healthz").handler(this::healthz);
        router.get("/readiness").handler(this::readiness);
        router.route("/api/v1.0/*").handler(BodyHandler.create());
        router.mountSubRouter("/api/v1.0/user", userApi.getRoutes());
        router.mountSubRouter("/api/v1.0/tss", new TradingSessionStatusApi(vertx).getRoutes());
        router.mountSubRouter("/api/v1.0/mc", new MarginComponentApi(vertx).getRoutes());
        router.mountSubRouter("/api/v1.0/tmr", new TotalMarginRequirementApi(vertx).getRoutes());
        router.mountSubRouter("/api/v1.0/mss", new MarginShortfallSurplusApi(vertx).getRoutes());
        router.mountSubRouter("/api/v1.0/pr", new PositionReportApi(vertx).getRoutes());
        router.mountSubRouter("/api/v1.0/rl", new RiskLimitApi(vertx).getRoutes());

        return router;
    }

    private void setCorsHandler(Router router)
    {
        if (config().getJsonObject("CORS", new JsonObject()).getBoolean("enable", HttpVerticle.DEFAULT_CORS))
        {
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
    }

    private void setCsrfHandler(Router router)
    {
        if (config().getJsonObject("CSRF", new JsonObject()).getBoolean("enable", HttpVerticle.DEFAULT_CSRF))
        {
            LOG.info("Enabling CSRF handler");
            router.route().handler(CookieHandler.create());
            router.route().handler(CSRFHandler.create(config().getJsonObject("CSRF", new JsonObject()).getString("secret", HttpVerticle.DEFAULT_CSRF_SECRET)));
        }
    }

    private UserApi setAuthHandler(Router router)
    {
        UserApi userApi;

        if (config().getJsonObject("auth", new JsonObject()).getBoolean("enable", HttpVerticle.DEFAULT_AUTH_ENABLED))
        {
            LOG.info("Enabling authentication");

            // Create a JWT Auth Provider
            JsonObject jwtConfig = new JsonObject().put("keyStore", new JsonObject()
                    .put("type", "jceks")
                    .put("path", config().getJsonObject("auth").getString("jwtKeystorePath"))
                    .put("password", config().getJsonObject("auth").getString("jwtKeystorePassword")));
            JWTAuth jwtAuthenticationProvider = JWTAuth.create(vertx, jwtConfig);
            router.route("/api/v1.0/*").handler(JWTAuthHandler.create(jwtAuthenticationProvider, "/api/v1.0/user/login"));

            router.route().handler(BodyHandler.create().setBodyLimit(MAX_BODY_SIZE));
            setCsrfHandler(router);
            addSecurityHeaders(router);

            AuthProvider mongoAuthenticationProvider = this.createMongoAuthenticationProvider();
            userApi = new UserApi(vertx, jwtAuthenticationProvider, mongoAuthenticationProvider, config().getJsonObject("auth", new JsonObject()));
        }
        else
        {
            userApi = new UserApi(vertx, config().getJsonObject("auth", new JsonObject()));
        }

        return userApi;
    }

    private void addSecurityHeaders(Router router)
    {
        // From http://vertx.io/blog/writing-secure-vert-x-web-apps/
        router.route().handler(ctx ->
        {
            ctx.response()
                    // do not allow proxies to cache the data
                    .putHeader("Cache-Control", "no-store, no-cache")
                    // prevents Internet Explorer from MIME - sniffing a
                    // response away from the declared content-type
                    .putHeader("X-Content-Type-Options", "nosniff")
                    // Strict HTTPS (for about ~6Months)
                    .putHeader("Strict-Transport-Security", "max-age=" + 15768000)
                    // IE8+ do not allow opening of attachments in the context of this resource
                    .putHeader("X-Download-Options", "noopen")
                    // enable XSS for IE
                    .putHeader("X-XSS-Protection", "1; mode=block")
                    // deny frames
                    .putHeader("X-FRAME-OPTIONS", "DENY")
                    .putHeader("Expires", "0");

            ctx.next();
        });
    }

    private void healthz(RoutingContext routingContext) {
        routingContext.response().setStatusCode(200).end("ok");
    }

    private void readiness(RoutingContext routingContext) {
        if (healthCheck.ready())
        {
            routingContext.response().setStatusCode(200).end("ok");
        }
        else
        {
            routingContext.response().setStatusCode(503).end("nok");
        }
    }

    @Override
    public void stop() throws Exception
    {
        LOG.info("Shutting down webserver");
        server.close();
    }
}
