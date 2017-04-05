package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.persistence.EchoPersistenceService;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
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
    public static void setUp(TestContext context) throws IOException {
        HttpVerticleTest.vertx = Vertx.vertx();
        HttpVerticleTest.port = Integer.getInteger("http.port", 8080);
    }

    private void deployHttpVerticle(TestContext context, JsonObject config) {
        final Async asyncStart = context.async();

        vertx.deployVerticle(HttpVerticle.class.getName(), new DeploymentOptions().setConfig(config), res -> {
            if (res.succeeded()) {
                ProxyHelper.registerService(PersistenceService.class, vertx, new EchoPersistenceService(), PersistenceService.SERVICE_ADDRESS);
                persistenceProxy = ProxyHelper.createProxy(PersistenceService.class, vertx, PersistenceService.SERVICE_ADDRESS);
                persistenceProxy.initialize(context.asyncAssertSuccess());

                asyncStart.complete();
            } else {
                context.fail(res.cause());
            }
        });

        asyncStart.awaitSuccess();
    }

    @Test
    public void testPlainHttp(TestContext context) {
        JsonObject config = new JsonObject().put("port", port);
        deployHttpVerticle(context, config);

        final Async asyncClient = context.async();

        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pr/latest", res -> {
            context.assertEquals(200, res.statusCode());
            asyncClient.complete();
        });
    }

    @Test
    public void testCORS(TestContext context) {
        JsonObject config = new JsonObject().put("port", port).put("CORS", new JsonObject().put("enable", true).put("origin", "https://localhost:8888"));
        deployHttpVerticle(context, config);

        final Async asyncClient = context.async();

        String myOrigin = "https://localhost:8888";

        vertx.createHttpClient().get(port, "localhost", "/api/v1.0/pr/latest", res -> {
            context.assertEquals(200, res.statusCode());
            context.assertEquals(myOrigin, res.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
            asyncClient.complete();
        }).putHeader(HttpHeaders.ORIGIN, myOrigin).end();
    }

    @Test
    public void testSslServerAuthentication(TestContext context) {
        JsonObject config = new JsonObject().put("port", port).put("ssl", new JsonObject().put("enable", true).put("keystore", getClass().getResource("http.keystore").getPath()).put("keystorePassword", "123456"));
        deployHttpVerticle(context, config);

        final Async asyncSslClient = context.async();

        HttpClientOptions sslOpts = new HttpClientOptions().setSsl(true).setTrustStoreOptions(new JksOptions().setPath(getClass().getResource("client.truststore").getPath()).setPassword("123456"));

        vertx.createHttpClient(sslOpts).get(port, "localhost", "/api/v1.0/pr/latest", res -> {
            context.assertEquals(200, res.statusCode());
            asyncSslClient.complete();
        }).end();

        final Async asyncClient = context.async();

        vertx.createHttpClient().get(port, "localhost", "/api/v1.0/pr/latest", res -> {
            context.fail("Connected to HTTPS connection with HTTP!");
        }).exceptionHandler(res -> {
            asyncClient.complete();
        }).end();
    }

    @Test
    public void testSslClientAuthentication(TestContext context) {
        JsonObject config = new JsonObject().put("port", port).put("ssl", new JsonObject().put("enable", true).put("keystore", getClass().getResource("http.keystore").getPath()).put("keystorePassword", "123456").put("truststore", getClass().getResource("http.truststore").getPath()).put("truststorePassword", "123456").put("requireTLSClientAuth", false));
        deployHttpVerticle(context, config);

        final Async asyncSslClient = context.async();

        HttpClientOptions sslOpts = new HttpClientOptions().setSsl(true).setTrustStoreOptions(new JksOptions().setPath(getClass().getResource("client.truststore").getPath()).setPassword("123456"));

        vertx.createHttpClient(sslOpts).get(port, "localhost", "/api/v1.0/pr/latest", res -> {
            context.assertEquals(200, res.statusCode());
            asyncSslClient.complete();
        }).end();

        final Async asyncSslClientAuth = context.async();
        HttpClientOptions sslClientAuthOpts = new HttpClientOptions().setSsl(true).setTrustStoreOptions(new JksOptions().setPath(getClass().getResource("client.truststore").getPath()).setPassword("123456")).setKeyStoreOptions(new JksOptions().setPath(getClass().getResource("client.keystore").getPath()).setPassword("123456"));

        vertx.createHttpClient(sslClientAuthOpts).get(port, "localhost", "/api/v1.0/pr/latest", res -> {
            context.assertEquals(200, res.statusCode());
            asyncSslClientAuth.complete();
        }).end();

        final Async asyncClient = context.async();

        vertx.createHttpClient().get(port, "localhost", "/api/v1.0/pr/latest", res -> {
            context.fail("Connected to HTTPS connection with HTTP!");
        }).exceptionHandler(res -> {
            asyncClient.complete();
        }).end();
    }

    @Test
    public void testSslRequiredClientAuthentication(TestContext context) {
        JsonObject config = new JsonObject().put("port", port).put("ssl", new JsonObject().put("enable", true).put("keystore", getClass().getResource("http.keystore").getPath()).put("keystorePassword", "123456").put("truststore", getClass().getResource("http.truststore").getPath()).put("truststorePassword", "123456").put("requireTLSClientAuth", true));
        deployHttpVerticle(context, config);

        final Async asyncSslClient = context.async();

        HttpClientOptions sslOpts = new HttpClientOptions().setSsl(true).setTrustStoreOptions(new JksOptions().setPath(getClass().getResource("client.truststore").getPath()).setPassword("123456"));

        vertx.createHttpClient(sslOpts).get(port, "localhost", "/api/v1.0/pr/latest", res -> {
            context.fail("Connected without client authentication!");
        }).exceptionHandler(res -> {
            asyncSslClient.complete();
        }).end();

        final Async asyncSslClientAuth = context.async();
        HttpClientOptions sslClientAuthOpts = new HttpClientOptions().setSsl(true).setTrustStoreOptions(new JksOptions().setPath(getClass().getResource("client.truststore").getPath()).setPassword("123456")).setKeyStoreOptions(new JksOptions().setPath(getClass().getResource("client.keystore").getPath()).setPassword("123456"));

        vertx.createHttpClient(sslClientAuthOpts).get(port, "localhost", "/api/v1.0/pr/latest", res -> {
            context.assertEquals(200, res.statusCode());
            asyncSslClientAuth.complete();
        }).end();

        final Async asyncClient = context.async();

        vertx.createHttpClient().get(port, "localhost", "/api/v1.0/pr/latest", res -> {
            context.fail("Connected to HTTPS connection with HTTP!");
        }).exceptionHandler(res -> {
            asyncClient.complete();
        }).end();
    }

    @After
    public void cleanup(TestContext context) {
        vertx.deploymentIDs().forEach(id -> {
            vertx.undeploy(id, context.asyncAssertSuccess());
        });
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

}
