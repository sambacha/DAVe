package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import com.deutscheboerse.risk.dave.restapi.margin.*;
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
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;

import java.util.ArrayList;
import java.util.List;

import static com.deutscheboerse.risk.dave.healthcheck.HealthCheck.Component.HTTP;

public class HttpVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(HttpVerticle.class);

    private static final String API_VERSION = "v1.0";
    private static final String API_PREFIX = String.format("/api/%s", API_VERSION);

    static final String REST_HEALTHZ = "/healthz";
    static final String REST_READINESS = "/readiness";

    private enum Mode {
        HTTP, HTTPS
    }

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
    private static final String DEFAULT_AUTH_PERMISSIONS_CLAIM_KEY = "realm_access/roles";

    private static final String AUTH_ENABLE_KEY = "enable";
    private static final String AUTH_PERMISSIONS_CLAIM_KEY = "permissionsClaimKey";
    private static final String AUTH_PUBLIC_KEY = "jwtPublicKey";

    private HttpServer server;
    private Mode operationMode;
    private HealthCheck healthCheck;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        LOG.info("Starting {} with configuration: {}", HttpVerticle.class.getSimpleName(), config().encodePrettily());

        healthCheck = new HealthCheck(this.vertx);

        if (config().getJsonObject("ssl", new JsonObject()).getBoolean(AUTH_ENABLE_KEY, DEFAULT_SSL)) {
            this.operationMode = Mode.HTTPS;
        } else {
            this.operationMode = Mode.HTTP;
        }
        List<Future> futures = new ArrayList<>();
        futures.add(startHttpServer());

        CompositeFuture.all(futures).setHandler(ar ->
        {
            if (ar.succeeded()) {
                healthCheck.setComponentReady(HTTP);
                startFuture.complete();
            } else {
                healthCheck.setComponentFailed(HTTP);
                startFuture.fail(ar.cause());
            }
        });
    }

    private Future<HttpServer> startHttpServer() {
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

    private HttpServerOptions configureWebServer() {
        HttpServerOptions httpOptions = new HttpServerOptions();
        setSsl(httpOptions);
        setCompression(httpOptions);
        return httpOptions;
    }

    private void setSsl(HttpServerOptions httpOptions) {
        if (!this.operationMode.equals(HttpVerticle.Mode.HTTPS)) {
            return;
        }
        if (config().getJsonObject("ssl", new JsonObject()).getBoolean(AUTH_ENABLE_KEY, DEFAULT_SSL) && config().getJsonObject("ssl", new JsonObject()).getString("keystore") != null && config().getJsonObject("ssl", new JsonObject()).getString("keystorePassword") != null) {
            LOG.info("Enabling SSL on webserver");
            httpOptions.setSsl(true).setKeyStoreOptions(new JksOptions().setPassword(config().getJsonObject("ssl").getString("keystorePassword")).setPath(config().getJsonObject("ssl").getString("keystore")));

            setSslClientAuthentication(httpOptions);
        }
    }

    private void setSslClientAuthentication(HttpServerOptions httpOptions) {
        if (config().getJsonObject("ssl", new JsonObject()).getString("truststore") != null && config().getJsonObject("ssl", new JsonObject()).getString("truststorePassword") != null) {
            LOG.info("Enabling SSL Client Authentication on webserver");
            httpOptions.setTrustStoreOptions(new JksOptions().setPassword(config().getJsonObject("ssl").getString("truststorePassword")).setPath(config().getJsonObject("ssl").getString("truststore")));

            if (config().getJsonObject("ssl").getBoolean("requireTLSClientAuth", DEFAULT_SSL_REQUIRE_CLIENT_AUTH)) {
                LOG.info("Setting SSL Client Authentication as required");
                httpOptions.setClientAuth(ClientAuth.REQUIRED);
            } else {
                httpOptions.setClientAuth(ClientAuth.REQUEST);
            }
        }
    }

    private void setCompression(HttpServerOptions httpOptions) {
        if (config().getBoolean("compression", DEFAULT_COMPRESSION)) {
            LOG.info("Enabling compression on webserver");
            httpOptions.setCompressionSupported(true);
        }

    }

    private Router configureRouter() {
        HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);
        HealthCheckHandler readinessHandler = HealthCheckHandler.create(vertx);

        healthCheckHandler.register("healthz", this::healthz);
        readinessHandler.register("readiness", this::readiness);

        Router router = Router.router(vertx);

        setCorsHandler(router);
        setAuthHandler(router);

        LOG.info("Adding route REST API");
        router.get(REST_HEALTHZ).handler(healthCheckHandler);
        router.get(REST_READINESS).handler(readinessHandler);
        router.route(API_PREFIX+"/*").handler(BodyHandler.create());
        router.mountSubRouter(API_PREFIX, new AccountMarginApi(vertx).getRoutes());
        router.mountSubRouter(API_PREFIX, new LiquiGroupMarginApi(vertx).getRoutes());
        router.mountSubRouter(API_PREFIX, new LiquiGroupSplitMarginApi(vertx).getRoutes());
        router.mountSubRouter(API_PREFIX, new PoolMarginApi(vertx).getRoutes());
        router.mountSubRouter(API_PREFIX, new PositionReportApi(vertx).getRoutes());
        router.mountSubRouter(API_PREFIX, new RiskLimitUtilizationApi(vertx).getRoutes());

        return router;
    }

    private void setCorsHandler(Router router) {
        if (config().getJsonObject("CORS", new JsonObject()).getBoolean(AUTH_ENABLE_KEY, HttpVerticle.DEFAULT_CORS)) {
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

    private void setCsrfHandler(Router router) {
        if (config().getJsonObject("CSRF", new JsonObject()).getBoolean(AUTH_ENABLE_KEY, HttpVerticle.DEFAULT_CSRF)) {
            LOG.info("Enabling CSRF handler");
            router.route().handler(CookieHandler.create());
            router.route().handler(CSRFHandler.create(config().getJsonObject("CSRF", new JsonObject()).getString("secret", HttpVerticle.DEFAULT_CSRF_SECRET)));
        }
    }

    private void setAuthHandler(Router router) {
        if (config().getJsonObject("auth", new JsonObject()).getBoolean(AUTH_ENABLE_KEY, HttpVerticle.DEFAULT_AUTH_ENABLED)) {
            LOG.info("Enabling authentication");

            // Create a JWT Auth Provider
            JsonObject jwtConfig = new JsonObject()
                    .put("public-key", config().getJsonObject("auth", new JsonObject()).getString(AUTH_PUBLIC_KEY, null))
                    .put("permissionsClaimKey", config().getJsonObject("auth", new JsonObject()).getString(AUTH_PERMISSIONS_CLAIM_KEY, HttpVerticle.DEFAULT_AUTH_PERMISSIONS_CLAIM_KEY));
            JWTAuth jwtAuthenticationProvider = JWTAuth.create(vertx, jwtConfig);
            router.route(API_PREFIX+"/*").handler(JWTAuthHandler.create(jwtAuthenticationProvider));

            router.route().handler(BodyHandler.create().setBodyLimit(MAX_BODY_SIZE));
            setCsrfHandler(router);
            addSecurityHeaders(router);
        }
    }

    private void addSecurityHeaders(Router router) {
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

    private void healthz(Future<Status> future) {
        future.complete(Status.OK());
    }

    private void readiness(Future<Status> future) {
        future.complete(healthCheck.ready() ? Status.OK() : Status.KO());
    }

    @Override
    public void stop() throws Exception {
        LOG.info("Shutting down webserver");
        server.close();
    }
}
