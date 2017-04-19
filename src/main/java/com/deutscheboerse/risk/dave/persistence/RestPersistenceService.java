package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.config.StoreManagerConfig;
import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import com.deutscheboerse.risk.dave.util.URIBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Arrays;

public class RestPersistenceService implements PersistenceService {
    private static final Logger LOG = LoggerFactory.getLogger(RestPersistenceService.class);

    private final Vertx vertx;
    private final StoreManagerConfig config;
    private final HttpClient httpClient;
    private final HealthCheck healthCheck;

    @Inject
    public RestPersistenceService(Vertx vertx, @Named("storeManager.conf") JsonObject config) throws IOException {
        this.vertx = vertx;
        this.config = (new ObjectMapper()).readValue(config.toString(), StoreManagerConfig.class);
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
        this.query(this.config.getRestApi().getAccountMargin(), type, query, resultHandler);
    }

    @Override
    public void queryLiquiGroupMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(this.config.getRestApi().getLiquiGroupMargin(), type, query, resultHandler);
    }

    @Override
    public void queryLiquiGroupSplitMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(this.config.getRestApi().getLiquiGroupSplitMargin(), type, query, resultHandler);
    }

    @Override
    public void queryPoolMargin(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(this.config.getRestApi().getPoolMargin(), type, query, resultHandler);
    }

    @Override
    public void queryPositionReport(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(this.config.getRestApi().getPositionReport(), type, query, resultHandler);
    }

    @Override
    public void queryRiskLimitUtilization(RequestType type, JsonObject query, Handler<AsyncResult<String>> resultHandler) {
        this.query(this.config.getRestApi().getRiskLimitUtilization(), type, query, resultHandler);
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
        httpClientOptions.setVerifyHost(this.config.isVerifyHost());
        PemTrustOptions pemTrustOptions = new PemTrustOptions();
        Arrays.stream(this.config.getSslTrustCerts())
                .map(Object::toString)
                .forEach(trustKey -> pemTrustOptions.addCertValue(Buffer.buffer(trustKey)));
        httpClientOptions.setPemTrustOptions(pemTrustOptions);
        final String sslKey = this.config.getSslKey();
        final String sslCert = this.config.getSslCert();
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
                config.getPort(),
                config.getHostname(),
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
