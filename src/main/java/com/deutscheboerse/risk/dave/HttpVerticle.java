package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import com.deutscheboerse.risk.dave.restapi.margin.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.deutscheboerse.risk.dave.healthcheck.HealthCheck.Component.HTTP;

public class HttpVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(HttpVerticle.class);

    private static final String API_VERSION = "v1.0";
    private static final String API_PREFIX = String.format("/api/%s", API_VERSION);

    private static final Integer MAX_BODY_SIZE = 1024 * 1024; // 1MB

    private static final Integer DEFAULT_PORT = 8443;

    private static final Boolean DEFAULT_SSL = true;
    private static final Boolean DEFAULT_SSL_REQUIRE_CLIENT_AUTH = false;
    private static final String DEFAULT_SSL_KEY = "";
    private static final String DEFAULT_SSL_CERT = "";

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

    private static final String HIDDEN_CERTIFICATE = "******************";

    private HttpServer server;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        LOG.info("Starting {} with configuration: {}", HttpVerticle.class.getSimpleName(), hideCertificates(config()).encodePrettily());

        HealthCheck healthCheck = new HealthCheck(this.vertx);

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

    private JsonObject hideCertificates(JsonObject config) {
        return config.copy()
                .getJsonObject("ssl", new JsonObject())
                .put("sslKey", HIDDEN_CERTIFICATE)
                .put("sslCert", HIDDEN_CERTIFICATE)
                .put("sslTrustCerts", new JsonArray(
                        config.getJsonObject("ssl", new JsonObject()).getJsonArray("sslTrustCerts").stream()
                                .map(i -> HIDDEN_CERTIFICATE).collect(Collectors.toList()))
                );
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

    private void setSsl(HttpServerOptions httpServerOptions) {
        JsonObject sslConfig = config().getJsonObject("ssl", new JsonObject());
        boolean sslEnable = sslConfig.getBoolean(AUTH_ENABLE_KEY, DEFAULT_SSL);
        if (!sslEnable) {
            return;
        }
        httpServerOptions.setSsl(true);
        PemKeyCertOptions pemKeyCertOptions = new PemKeyCertOptions()
                .setKeyValue(Buffer.buffer(sslConfig.getString("sslKey", DEFAULT_SSL_KEY)))
                .setCertValue(Buffer.buffer(sslConfig.getString("sslCert", DEFAULT_SSL_CERT)));
        httpServerOptions.setPemKeyCertOptions(pemKeyCertOptions);

        PemTrustOptions pemTrustOptions = new PemTrustOptions();
        sslConfig.getJsonArray("sslTrustCerts", new JsonArray())
                .stream()
                .map(Object::toString)
                .forEach(trustKey -> pemTrustOptions.addCertValue(Buffer.buffer(trustKey)));
        if (!pemTrustOptions.getCertValues().isEmpty()) {
            httpServerOptions.setPemTrustOptions(pemTrustOptions);
            ClientAuth clientAuth = sslConfig.getBoolean("sslRequireClientAuth", DEFAULT_SSL_REQUIRE_CLIENT_AUTH) ?
                    ClientAuth.REQUIRED : ClientAuth.REQUEST;
            httpServerOptions.setClientAuth(clientAuth);
        }
    }

    private void setCompression(HttpServerOptions httpOptions) {
        if (config().getBoolean("compression", DEFAULT_COMPRESSION)) {
            LOG.info("Enabling compression on webserver");
            httpOptions.setCompressionSupported(true);
        }
    }

    private Router configureRouter() {
        Router router = Router.router(vertx);

        setCorsHandler(router);
        setAuthHandler(router);

        LOG.info("Adding route REST API");
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

    @Override
    public void stop() throws Exception {
        LOG.info("Shutting down webserver");
        server.close();
    }
}
