package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.auth.JWKSAuthProviderImpl;
import com.deutscheboerse.risk.dave.persistence.EchoPersistenceService;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import com.deutscheboerse.risk.dave.utils.TestConfig;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@RunWith(VertxUnitRunner.class)
public class AuthTest {
    private static Vertx vertx;
    private static PersistenceService persistenceProxy;

    private static final String CERTS_VALID = "certs.valid";
    private static final String CERTS_INVALID = "certs.invalid";
    private static final String JWT_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJNQjFJZjk4R1lZS0Z2WmFxamdzUHVrMXVlamx6WGhFVXRkbGtEZGU0NFcwIn0.eyJqdGkiOiI1MWFlOWVhNS00NWNiLTQ3MzYtODk3NC0wYWE5NjBmYTQ3ZDIiLCJleHAiOjIxNDc0MTE5NTMsIm5iZiI6MCwiaWF0IjoxNTAyMzQ5NTUzLCJpc3MiOiJodHRwczovL2F1dGguZGF2ZS5kYmctZGV2b3BzLmNvbS9hdXRoL3JlYWxtcy9EQVZlIiwiYXVkIjoiZGF2ZS11aSIsInN1YiI6IjEwNDNmNmZkLTliMjUtNGI4Zi05NDhiLTc2MjIxOWY1NWEzZSIsInR5cCI6IklEIiwiYXpwIjoiZGF2ZS11aSIsIm5vbmNlIjoiMjAyLWUzYmNiZjA4NGEyNWIyZWUxZGQtMzhlNDQ0NWMiLCJhdXRoX3RpbWUiOjE1MDIzNDk1NTAsInNlc3Npb25fc3RhdGUiOiJmYTA2OWEzYS1kY2U4LTRkZjUtOTY3NC04NTA5MzAwOWJlYWEiLCJhY3IiOiIxIiwiZnlpIjoiUGxlYXNlIHJlZ2VuZXJhdGUgb24gTW9uZGF5LCBKYW51YXJ5IDE4LCAyMDM4IiwibmFtZSI6IkRBVmUgUmlza0lUIiwiZ2l2ZW5fbmFtZSI6IkRBVmUiLCJmYW1pbHlfbmFtZSI6IlJpc2tJVCIsImVtYWlsIjoicmlza2l0YnJvd3NlcnN0YWNrQGRldXRzY2hlLWJvZXJzZS5jb20iLCJ1c2VybmFtZSI6ImRhdmUifQ.dC5Cdx09dv6SDQJEm6ke4qRJ17zHlflz4lGMkzGai7Zl_YqNdLOrOJudtE8iATmWZBJ68RF4yDiGloBPiq2oFfO2aScaW8xpXp5_8_ILlL9baKFcxuHpqgyE2be4Q-oSJZIaEYWB3c8fYrImOVuBRrhtjV39G5DXTUo4_f6Ff_B9nuaTozr6QfXiQ3DwmZ80oqw19oKzhrDU0mjLsUtc7iQm7_h8rHz_Ps_F3Me7DmmYdhewcMRKWO9tf82-BchDOvv-2qP1ePrFTHfi8zBE5AFxyB11Y1wsMemCzNk_37g6JbjmlJRSiu8neWZYiqkRmg__r6-nv0fc2xxFAyC0Xg";
    private static final String EXPIRED_JWT_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJNQjFJZjk4R1lZS0Z2WmFxamdzUHVrMXVlamx6WGhFVXRkbGtEZGU0NFcwIn0.eyJqdGkiOiJhNTgxZGQwYy00OTcyLTQyNDUtYTlhZC04ZDBkN2JjYWJkZGUiLCJleHAiOjE1MDIzNzU0NzUsIm5iZiI6MCwiaWF0IjoxNTAyMzc1MzU1LCJpc3MiOiJodHRwczovL2F1dGguZGF2ZS5kYmctZGV2b3BzLmNvbS9hdXRoL3JlYWxtcy9EQVZlIiwiYXVkIjoiZGF2ZS11aSIsInN1YiI6IjEwNDNmNmZkLTliMjUtNGI4Zi05NDhiLTc2MjIxOWY1NWEzZSIsInR5cCI6IklEIiwiYXpwIjoiZGF2ZS11aSIsIm5vbmNlIjoiMGQzLTFhYTFlYmU3NDM5M2JhOWU3N2EtZjU2ODQwOWIiLCJhdXRoX3RpbWUiOjE1MDIzNzUzNTEsInNlc3Npb25fc3RhdGUiOiIzMjg0NmIyZS03MjEzLTQ4OTgtYTIzYS03NWY2OTE5NTNhYzYiLCJhY3IiOiIxIiwibmFtZSI6IkRBVmUgUmlza0lUIiwiZ2l2ZW5fbmFtZSI6IkRBVmUiLCJmYW1pbHlfbmFtZSI6IlJpc2tJVCIsImVtYWlsIjoicmlza2l0YnJvd3NlcnN0YWNrQGRldXRzY2hlLWJvZXJzZS5jb20iLCJ1c2VybmFtZSI6ImRhdmUifQ.bXMW01YDxXSVrsEawKHlZu3H7lVaeSEKFzjjLhacBtNARQJyXo-Po0Ia6b3GwXuwl3TC8E_CBBsxSWFioAXxYlieP9L1paTl3jYeZJZCz63dA7E7J3l_IcpAujH5gDA8ZfCrVjQ83xNN7R61PTCNCN4s1s3wXc7d4CGPslyZZj7H6HFDDB-nYJj_KE6RoB7j6h1nFT8HG51Lodp65a5JDVTWiN4omwe-Yd6SyX2VcQWtM4HocXrfQ2ZHkdn16HboF2CmKRuhucNw05IVKfHViyFMF7haUMRrrOZsUvMqbRIf2odwZ5QvkTctnwGpLBqgXcx6U-MA59zpo_1SQAwfKA";

    @BeforeClass
    public static void setUp() throws IOException {
        AuthTest.vertx = Vertx.vertx();
    }

    private void deployApiVerticle(TestContext context, JsonObject config) {
        final Async asyncStart = context.async();

        vertx.deployVerticle(ApiVerticle.class.getName(), new DeploymentOptions().setConfig(config), res -> {
            if (res.succeeded()) {
                ProxyHelper.registerService(PersistenceService.class, vertx, new EchoPersistenceService(vertx), PersistenceService.SERVICE_ADDRESS);
                persistenceProxy = ProxyHelper.createProxy(PersistenceService.class, vertx, PersistenceService.SERVICE_ADDRESS);
                persistenceProxy.initialize(context.asyncAssertSuccess());

                asyncStart.complete();
            } else {
                context.fail(res.cause());
            }
        });

        asyncStart.awaitSuccess();
    }

    private HttpServer createOpenIdMockServer(String jwksCerts) {
        return AuthTest.vertx.createHttpServer().requestHandler(request -> {
            HttpServerResponse response = request.response();
            response.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
            JsonObject content = new JsonObject();
            content.put("issuer", "https://auth.dave.dbg-devops.com/auth/realms/DAVe");
            content.put("jwks_uri", JWKSAuthProviderImpl.class.getResource(jwksCerts).toString());
            response.end(content.toBuffer());
        });
    }

    @Test
    public void testValidJWT(TestContext context) throws URISyntaxException {
        HttpServer openIdMockServer = this.createOpenIdMockServer(CERTS_VALID);
        openIdMockServer.listen(TestConfig.OPENID_PORT);
        JsonObject config = TestConfig.getApiConfig();
        config.getJsonObject("auth").put("enable", true);
        deployApiVerticle(context, config);

        createSslRequest("/api/v1.0/pr/latest")
                .putHeader("Authorization", "Bearer " + JWT_TOKEN)
                .send(context.asyncAssertSuccess(res ->
                        context.assertEquals(200, res.statusCode())
                ));
        openIdMockServer.close(context.asyncAssertSuccess());
    }

    @Test
    public void testInvalidPublicKey(TestContext context) throws URISyntaxException {
        HttpServer openIdMockServer = this.createOpenIdMockServer(CERTS_INVALID);
        openIdMockServer.listen(TestConfig.OPENID_PORT);
        JsonObject config = TestConfig.getApiConfig();
        config.getJsonObject("auth").put("enable", true);
        deployApiVerticle(context, config);

        createSslRequest("/api/v1.0/pr/latest")
                .putHeader("Authorization", "Bearer " + JWT_TOKEN)
                .send(context.asyncAssertSuccess(res ->
                        context.assertEquals(401, res.statusCode())
                ));
        openIdMockServer.close(context.asyncAssertSuccess());
    }

    @Test
    public void testExpiredJWT(TestContext context) throws URISyntaxException {
        HttpServer openIdMockServer = this.createOpenIdMockServer(CERTS_VALID);
        openIdMockServer.listen(TestConfig.OPENID_PORT);
        JsonObject config = TestConfig.getApiConfig();
        config.getJsonObject("auth").put("enable", true);
        deployApiVerticle(context, config);

        createSslRequest("/api/v1.0/pr/latest")
                .putHeader("Authorization", "Bearer " + EXPIRED_JWT_TOKEN)
                .send(context.asyncAssertSuccess(res ->
                        context.assertEquals(401, res.statusCode())
                ));
        openIdMockServer.close(context.asyncAssertSuccess());
    }

    @Test
    public void testNoJWT(TestContext context) throws URISyntaxException {
        HttpServer openIdMockServer = this.createOpenIdMockServer(CERTS_VALID);
        openIdMockServer.listen(TestConfig.OPENID_PORT);
        JsonObject config = TestConfig.getApiConfig();
        config.getJsonObject("auth").put("enable", true);
        deployApiVerticle(context, config);

        createSslRequest("/api/v1.0/am/latest")
                .send(context.asyncAssertSuccess(res ->
                        context.assertEquals(401, res.statusCode())
                ));
        openIdMockServer.close(context.asyncAssertSuccess());
    }

    @Test
    public void testCSRF(TestContext context) throws InterruptedException, URISyntaxException {
        HttpServer openIdMockServer = this.createOpenIdMockServer(CERTS_VALID);
        openIdMockServer.listen(TestConfig.OPENID_PORT);
        JsonObject config = TestConfig.getApiConfig();
        config.getJsonObject("auth").put("enable", true);
        config.getJsonObject("csrf").put("enable", true);
        deployApiVerticle(context, config);

        createSslRequest("/api/v1.0/pr/latest")
                .putHeader("Authorization", "Bearer " + JWT_TOKEN)
                .send(context.asyncAssertSuccess(res -> {
                    context.assertEquals(200, res.statusCode());

                    final String csrfToken = getCsrfCookie(res.cookies())
                            .orElseThrow(() -> new RuntimeException("XSRF-TOKEN cookie not found"));

                    createSslPostRequest("/api/v1.0/pr/delete")
                            .putHeader("Authorization", "Bearer " + JWT_TOKEN)
                            .putHeader("X-XSRF-TOKEN", csrfToken)
                            .send(context.asyncAssertSuccess(csrfRes -> {
                                // We expect "Not found (404)" error code instead of "Forbidden (403)"
                                context.assertEquals(404, csrfRes.statusCode());
                            }));
                }));
        openIdMockServer.close(context.asyncAssertSuccess());
    }

    private Optional<String> getCsrfCookie(List<String> cookies) {
        return cookies.stream()
                .filter(cookie -> cookie.startsWith("XSRF-TOKEN="))
                .map(cookie -> cookie.replaceFirst("XSRF-TOKEN=", "").replaceFirst("; Path=.*", ""))
                .findFirst();
    }

    private HttpRequest<Buffer> createSslRequest(String uri) {
        WebClientOptions sslOpts = new WebClientOptions()
                .setSsl(true)
                .setPemTrustOptions(TestConfig.HTTP_API_CERTIFICATE.trustOptions());

        return WebClient.create(vertx, sslOpts)
                .get(TestConfig.API_PORT, "localhost", uri);
    }

    private HttpRequest<Buffer> createSslPostRequest(String uri) {
        WebClientOptions sslOpts = new WebClientOptions()
                .setSsl(true)
                .setPemTrustOptions(TestConfig.HTTP_API_CERTIFICATE.trustOptions());

        return WebClient.create(vertx, sslOpts)
                .post(TestConfig.API_PORT, "localhost", uri);
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
