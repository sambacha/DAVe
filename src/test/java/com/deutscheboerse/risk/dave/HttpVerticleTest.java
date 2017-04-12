package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.persistence.EchoPersistenceService;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import com.deutscheboerse.risk.dave.utils.TestConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(VertxUnitRunner.class)
public class HttpVerticleTest {
    private static Vertx vertx;
    private static int port;
    private static PersistenceService persistenceProxy;

    @BeforeClass
    public static void setUp() throws IOException {
        HttpVerticleTest.vertx = Vertx.vertx();
        HttpVerticleTest.port = TestConfig.API_PORT;
    }

    private void deployHttpVerticle(TestContext context, JsonObject config) {
        final Async asyncStart = context.async();

        vertx.deployVerticle(HttpVerticle.class.getName(), new DeploymentOptions().setConfig(config), context.asyncAssertSuccess(res -> {
            ProxyHelper.registerService(PersistenceService.class, vertx, new EchoPersistenceService(vertx), PersistenceService.SERVICE_ADDRESS);
            persistenceProxy = ProxyHelper.createProxy(PersistenceService.class, vertx, PersistenceService.SERVICE_ADDRESS);
            persistenceProxy.initialize(context.asyncAssertSuccess());

            asyncStart.complete();
        }));

        asyncStart.awaitSuccess();
    }

    @Test
    public void testPlainHttp(TestContext context) {
        JsonObject config = TestConfig.getApiConfig();
        config.getJsonObject("ssl").put("enable", false);
        deployHttpVerticle(context, config);

        final Async asyncClient = context.async();

        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pr/latest", res -> {
            context.assertEquals(200, res.statusCode());
            asyncClient.complete();
        });
    }

    @Test
    public void testCORS(TestContext context) {
        JsonObject config = TestConfig.getApiConfig();
        config.getJsonObject("CORS").put("enable", true).put("origin", "https://localhost:8888");
        deployHttpVerticle(context, config);

        final Async asyncClient = context.async();

        String myOrigin = "https://localhost:8888";

        HttpClientOptions sslOpts = new HttpClientOptions().setSsl(true)
                .setVerifyHost(false).setPemTrustOptions(TestConfig.HTTP_API_CERTIFICATE.trustOptions());

        vertx.createHttpClient(sslOpts).get(port, "localhost", "/api/v1.0/pr/latest", res -> {
            context.assertEquals(200, res.statusCode());
            context.assertEquals(myOrigin, res.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
            asyncClient.complete();
        }).putHeader(HttpHeaders.ORIGIN, myOrigin).end();
    }

    @Test
    public void testSslServerAuthentication(TestContext context) {
        JsonObject config = TestConfig.getApiConfig();
        deployHttpVerticle(context, config);

        final Async asyncSslClient = context.async();

        HttpClientOptions sslOpts = new HttpClientOptions().setSsl(true)
                .setVerifyHost(false).setPemTrustOptions(TestConfig.HTTP_API_CERTIFICATE.trustOptions());

        vertx.createHttpClient(sslOpts).get(port, "localhost", "/api/v1.0/pr/latest", res -> {
            context.assertEquals(200, res.statusCode());
            asyncSslClient.complete();
        }).end();

        final Async asyncClient = context.async();

        vertx.createHttpClient().get(port, "localhost", "/api/v1.0/pr/latest", res ->
            context.fail("Connected to HTTPS connection with HTTP!")
        ).exceptionHandler(res ->
            asyncClient.complete()
        ).end();
    }

    @Test
    public void testSslClientAuthentication(TestContext context) {
        JsonObject config = TestConfig.getApiConfig();
        deployHttpVerticle(context, config);

        final Async asyncSslClient = context.async();

        HttpClientOptions sslOpts = new HttpClientOptions().setSsl(true)
                .setVerifyHost(false).setPemTrustOptions(TestConfig.HTTP_API_CERTIFICATE.trustOptions());

        vertx.createHttpClient(sslOpts).get(port, "localhost", "/api/v1.0/pr/latest", res -> {
            context.assertEquals(200, res.statusCode());
            asyncSslClient.complete();
        }).end();

        final Async asyncSslClientAuth = context.async();
        HttpClientOptions sslClientAuthOpts = new HttpClientOptions().setSsl(true)
                .setVerifyHost(false).setPemTrustOptions(TestConfig.HTTP_API_CERTIFICATE.trustOptions())
                .setPemKeyCertOptions(new PemKeyCertOptions()
                        .setKeyPath(TestConfig.HTTP_CLIENT_CERTIFICATE.privateKeyPath())
                        .setCertPath(TestConfig.HTTP_CLIENT_CERTIFICATE.certificatePath()));

        vertx.createHttpClient(sslClientAuthOpts).get(port, "localhost", "/api/v1.0/pr/latest", res -> {
            context.assertEquals(200, res.statusCode());
            asyncSslClientAuth.complete();
        }).end();

        final Async asyncClient = context.async();

        vertx.createHttpClient().get(port, "localhost", "/api/v1.0/pr/latest", res ->
            context.fail("Connected to HTTPS connection with HTTP!")
        ).exceptionHandler(res ->
            asyncClient.complete()
        ).end();
    }

    @Test
    public void testSslRequiredClientAuthentication(TestContext context) {
        JsonObject config = TestConfig.getApiConfig();
        config.getJsonObject("ssl").put("sslRequireClientAuth", true);
        deployHttpVerticle(context, config);

        final Async asyncSslClient = context.async();

        HttpClientOptions sslOpts = new HttpClientOptions().setSsl(true)
                .setVerifyHost(false).setPemTrustOptions(TestConfig.HTTP_API_CERTIFICATE.trustOptions());

        vertx.createHttpClient(sslOpts).get(port, "localhost", "/api/v1.0/pr/latest", res ->
            context.fail("Connected without client authentication!")
        ).exceptionHandler(res ->
            asyncSslClient.complete()
        ).end();

        final Async asyncSslClientAuth = context.async();
        HttpClientOptions sslClientAuthOpts = new HttpClientOptions().setSsl(true)
                .setVerifyHost(false).setPemTrustOptions(TestConfig.HTTP_API_CERTIFICATE.trustOptions())
                .setPemKeyCertOptions(new PemKeyCertOptions()
                        .setKeyPath(TestConfig.HTTP_CLIENT_CERTIFICATE.privateKeyPath())
                        .setCertPath(TestConfig.HTTP_CLIENT_CERTIFICATE.certificatePath()));

        vertx.createHttpClient(sslClientAuthOpts).get(port, "localhost", "/api/v1.0/pr/latest", res -> {
            context.assertEquals(200, res.statusCode());
            asyncSslClientAuth.complete();
        }).end();

        final Async asyncClient = context.async();

        vertx.createHttpClient().get(port, "localhost", "/api/v1.0/pr/latest", res ->
            context.fail("Connected to HTTPS connection with HTTP!")
        ).exceptionHandler(res ->
            asyncClient.complete()
        ).end();
    }

    @After
    public void cleanup(TestContext context) {
        vertx.deploymentIDs().forEach(id ->
            vertx.undeploy(id, context.asyncAssertSuccess())
        );
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

}
