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
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(VertxUnitRunner.class)
public class AuthTest {
    private static Vertx vertx;
    private static int port;
    private static PersistenceService persistenceProxy;

    private static String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA33TqqLR3eeUmDtHS89qF3p4MP7Wfqt2Zjj3lZjLjjCGDvwr9cJNlNDiuKboODgUiT4ZdPWbOiMAfDcDzlOxA04DDnEFGAf+kDQiNSe2ZtqC7bnIc8+KSG/qOGQIVaay4Ucr6ovDkykO5Hxn7OU7sJp9TP9H0JH8zMQA6YzijYH9LsupTerrY3U6zyihVEDXXOv08vBHk50BMFJbE9iwFwnxCsU5+UZUZYw87Uu0n4LPFS9BT8tUIvAfnRXIEWCha3KbFWmdZQZlyrFw0buUEf0YN3/Q0auBkdbDR/ES2PbgKTJdkjc/rEeM0TxvOUf7HuUNOhrtAVEN1D5uuxE1WSwIDAQAB";
    private static String INVALID_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmae5XwoRlK2Ew2vSPLzV7Zlnrtz0YDp3UPSvxvW+zN9dGZm38E+7ihUOQbIjvkKnK30v0QgvKDOKgwY7Ney1nwcZfZTNwkRXh1qzLGXRZYD3ZBWx9vIhPcG6DyQBGXrhYgN4RFEIBzybKARMjxfZR9y9vckK7Wd0D2bmBGk4knvzI8fRPaGTt42dmLaP1X7yT/s0DygQbHFUj4ukXR8LXYGEGpt2OHRVlb/MrIl7Ko2Ad6oSpcfjzePd6BJNjuYSczmbTVfClMbw/GFHk5icAsRJt8a2B7Fvbgcxz9kyT5p/QCTza/5qNtvf1+wl20TDw9JglF2oOPRwQlQW2MXY/QIDAQAB";
    private static String PERMISSIONS_CLAIM_KEY = "realm_access/roles";
    private static String JWT_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDAvYXV0aC9yZWFsbXMvbWFzdGVyIiwic3ViIjoiYjgwNGU2ZGUtZWUzNy00NjAwLTg2M2EtY2M4NjlkYmQ1MjUyIiwibmJmIjoxNDkxMzI0NzUzLCJleHAiOjE1MjI4NjA3NTMsImlhdCI6MTQ5MTMyNDc1MywianRpIjoiZjFhZGUzYjEtMGI4OC00NWRlLWE5YjctNzY4Y2FjOWFhYWUzIiwidHlwIjoiQmVhcmVyIn0.kOVqtaEDLc176teca-H_pVnCP4JyYmbp2YxAZpc4tsVSUm0F-8lHQKvnNQsSdRxJND5k5yz7nrpPtcu-JtVJBiiqMBHxP4rZ7mR3gwmLBBP-GLuKOS3MSMTKlTex_6YO4v3Po6EN8gWhdcZzpgyz64mSwCL2A5kmpuz5ILc1rnZCSsOD9aaawJjIqEuCRIxLs0dMax6ryDNvomKW8nL_Pb1ECQ4XZOh93Rl_2XFj6BIUv7Q7QlqAR5jDQw13ZVvmuqqhbttuiIWQ2yys36c99WDmB-U-anbQ4Nd0N4PMAnmG5CvTLkjH-FcKtqU890QDwdeKhSUsxPjJoYsexbdiCg";
    private static String EXPIRED_JWT_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDAvYXV0aC9yZWFsbXMvbWFzdGVyIiwic3ViIjoiYjgwNGU2ZGUtZWUzNy00NjAwLTg2M2EtY2M4NjlkYmQ1MjUyIiwibmJmIjoxNDkxMzI1MDQ5LCJleHAiOjE0OTEzMjg2NDksImlhdCI6MTQ5MTMyNTA0OSwianRpIjoiZjFhZGUzYjEtMGI4OC00NWRlLWE5YjctNzY4Y2FjOWFhYWUzIiwidHlwIjoiQmVhcmVyIn0.h7tlf9mfg5UtgPxnhtWj5GpLJpQQc6JvpkjhJEH6fCyIn_sKQf3TxFC2_ApryIjwQy8GkqsEHbb4RiRAxaazB_XFCTnrvbkdFVT79iYmU5Zy_wLDr6xuytw5v4kkJ6zLBWXXd8IYs3oBk8zGs_dReYLGCnii8dKLzzkNd0TSUqYza7nC9d4j1eZHUGtdpjta48nNljYekTx8bGb6mGqHCppn0juFTws_Y8yXgFN89gq374wlGlecMffzU9acJ2FHKMaICB4LV3cS53XfFSzczhVmto6DLU97zqz11HqOmqFJ8It-SYB-cJOWwtP_920ADwTKoQsCuYKLBjD--pk73A";

    @BeforeClass
    public static void setUp(TestContext context) throws IOException {
        AuthTest.vertx = Vertx.vertx();
        AuthTest.port = Integer.getInteger("http.port", 8080);
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
    public void testValidJWT(TestContext context) {
        JsonObject config = new JsonObject().put("port", port).put("auth", new JsonObject().put("enable", true).put("jwtPublicKey", PUBLIC_KEY).put("permissionsClaimKey", PERMISSIONS_CLAIM_KEY));
        deployHttpVerticle(context, config);

        final Async asyncClient = context.async();

        WebClient.create(vertx)
                .get(port, "localhost", "/api/v1.0/pr/latest")
                .putHeader("Authorization", "Bearer " + JWT_TOKEN)
                .send(ar -> {
                    if (ar.succeeded()) {
                        HttpResponse res = ar.result();
                        context.assertEquals(200, res.statusCode());
                        asyncClient.complete();
                    }
                    else
                    {
                        context.fail(ar.cause());
                    }
                });
    }

    @Test
    public void testInvalidPublicKey(TestContext context) {
        JsonObject config = new JsonObject().put("port", port).put("auth", new JsonObject().put("enable", true).put("jwtPublicKey", INVALID_PUBLIC_KEY).put("permissionsClaimKey", PERMISSIONS_CLAIM_KEY));
        deployHttpVerticle(context, config);

        final Async asyncClient = context.async();

        WebClient.create(vertx)
                .get(port, "localhost", "/api/v1.0/pr/latest")
                .putHeader("Authorization", "Bearer " + JWT_TOKEN)
                .send(ar -> {
                    if (ar.succeeded()) {
                        HttpResponse res = ar.result();
                        context.assertEquals(401, res.statusCode());
                        asyncClient.complete();
                    }
                    else
                    {
                        context.fail(ar.cause());
                    }
                });
    }

    @Test
    public void testExpiredJWT(TestContext context) {
        JsonObject config = new JsonObject().put("port", port).put("auth", new JsonObject().put("enable", true).put("jwtPublicKey", PUBLIC_KEY).put("permissionsClaimKey", PERMISSIONS_CLAIM_KEY));
        deployHttpVerticle(context, config);

        final Async asyncClient = context.async();

        WebClient.create(vertx)
                .get(port, "localhost", "/api/v1.0/pr/latest")
                .putHeader("Authorization", "Bearer " + EXPIRED_JWT_TOKEN)
                .send(ar -> {
                    if (ar.succeeded()) {
                        HttpResponse res = ar.result();
                        context.assertEquals(401, res.statusCode());
                        asyncClient.complete();
                    }
                    else
                    {
                        context.fail(ar.cause());
                    }
                });
    }

    @Test
    public void testNoJWT(TestContext context) {
        JsonObject config = new JsonObject().put("port", port).put("auth", new JsonObject().put("enable", true).put("jwtPublicKey", PUBLIC_KEY).put("permissionsClaimKey", PERMISSIONS_CLAIM_KEY));
        deployHttpVerticle(context, config);

        final Async asyncClient = context.async();

        WebClient.create(vertx)
                .get(port, "localhost", "/api/v1.0/pr/latest")
                .send(ar -> {
                    if (ar.succeeded()) {
                        HttpResponse res = ar.result();
                        context.assertEquals(401, res.statusCode());
                        asyncClient.complete();
                    }
                    else
                    {
                        context.fail(ar.cause());
                    }
                });
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
