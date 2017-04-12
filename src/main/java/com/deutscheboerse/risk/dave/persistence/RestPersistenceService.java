package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import com.deutscheboerse.risk.dave.util.URIBuilder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;

import javax.inject.Inject;
import javax.inject.Named;

public class RestPersistenceService implements PersistenceService {
    private static final Logger LOG = LoggerFactory.getLogger(RestPersistenceService.class);

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 8443;

    private static final String DEFAULT_ACCOUNT_MARGIN_URI = "/api/v1.0/query/am";
    private static final String DEFAULT_LIQUI_GROUP_MARGIN_URI = "/api/v1.0/query/lgm";
    private static final String DEFAULT_LIQUI_SPLIT_MARGIN_URI = "/api/v1.0/query/lgsm";
    private static final String DEFAULT_POSITION_REPORT_URI = "/api/v1.0/query/pr";
    private static final String DEFAULT_POOL_MARGIN_URI = "/api/v1.0/query/pm";
    private static final String DEFAULT_RISK_LIMIT_UTILIZATION_URI = "/api/v1.0/query/rlu";

    private static final Boolean DEFAULT_VERIFY_HOST = true;

    private final Vertx vertx;
    private final JsonObject config;
    private final HttpClient httpClient;
    private final HealthCheck healthCheck;

    @Inject
    public RestPersistenceService(Vertx vertx, @Named("storeManager.conf") JsonObject config) {
        this.vertx = vertx;
        this.config = config;
        this.httpClient = this.createHttpClient();
        this.healthCheck = new HealthCheck(vertx);
    }

    @Override
    public void initialize(Handler<AsyncResult<Void>> resultHandler) {
        healthCheck.setComponentReady(HealthCheck.Component.PERSISTENCE_SERVICE);
        resultHandler.handle(Future.succeededFuture());
    }

    @Override
    public void queryAccountMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(DEFAULT_ACCOUNT_MARGIN_URI, type, query, resultHandler);
    }

    @Override
    public void queryLiquiGroupMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(DEFAULT_LIQUI_GROUP_MARGIN_URI, type, query, resultHandler);
    }

    @Override
    public void queryLiquiGroupSplitMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(DEFAULT_LIQUI_SPLIT_MARGIN_URI, type, query, resultHandler);
    }

    @Override
    public void queryPoolMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(DEFAULT_POOL_MARGIN_URI, type, query, resultHandler);
    }

    @Override
    public void queryPositionReport(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(DEFAULT_POSITION_REPORT_URI, type, query, resultHandler);
    }

    @Override
    public void queryRiskLimitUtilization(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(DEFAULT_RISK_LIMIT_UTILIZATION_URI, type, query, resultHandler);
    }

    @Override
    public void close() {
        this.httpClient.close();
    }

    private HttpClient createHttpClient() {
        HttpClientOptions httpClientOptions = this.createHttpClientOptions();
        return this.vertx.createHttpClient(httpClientOptions);
    }

    private HttpClientOptions createHttpClientOptions() {
        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setSsl(true);
        httpClientOptions.setVerifyHost(this.config.getBoolean("verifyHost", DEFAULT_VERIFY_HOST));
        PemTrustOptions pemTrustOptions = new PemTrustOptions();
        this.config.getJsonArray("sslTrustCerts", new JsonArray())
                .stream()
                .map(Object::toString)
                .forEach(trustKey -> pemTrustOptions.addCertValue(Buffer.buffer(trustKey)));
        httpClientOptions.setPemTrustOptions(pemTrustOptions);
        final String sslKey = this.config.getString("sslKey");
        final String sslCert = this.config.getString("sslCert");
        if (sslKey != null && sslCert != null) {
            PemKeyCertOptions pemKeyCertOptions = new PemKeyCertOptions()
                    .setKeyValue(Buffer.buffer(sslKey))
                    .setCertValue(Buffer.buffer(sslCert));
            httpClientOptions.setPemKeyCertOptions(pemKeyCertOptions);
        }
        return httpClientOptions;
    }

    private void query(String basePath, RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        final String requestURI = new URIBuilder(basePath + (type == RequestType.HISTORY ? "/history" : "/latest"))
            .addParams(query)
            .build();
        this.httpClient.get(
                config.getInteger("port", DEFAULT_PORT),
                config.getString("hostname", DEFAULT_HOSTNAME),
                requestURI,
                response -> {
                    if (response.statusCode() == HttpResponseStatus.OK.code()) {
                        response.bodyHandler(body -> resultHandler.handle(Future.succeededFuture(body.toString())));
                    } else {
                        LOG.error("{} failed: {}", requestURI, response.statusMessage());
                        resultHandler.handle(Future.failedFuture(response.statusMessage()));
                    }
                }).exceptionHandler(e -> {
                    LOG.error("{} failed: {}", requestURI, e.getMessage());
                    resultHandler.handle(Future.failedFuture(e.getMessage()));
                }).end();
    }
}
