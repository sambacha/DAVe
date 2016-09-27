package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.utils.DummyData;
import com.deutscheboerse.risk.dave.utils.DummyWebServer;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MasterdataVerticleTest {
    private static Vertx vertx;
    private static int port;
    private static HttpServer server;

    @BeforeClass
    public static void setUp(TestContext context) {
        vertx = Vertx.vertx();
        port = Integer.getInteger("http.port", 8080);

        JsonObject clearerABCFR = new JsonObject().put("clearer", "ABCFR").put("members", new JsonArray().add(new JsonObject().put("member", "ABCFR").put("accounts", new JsonArray().add("A1").add("A2").add("PP"))).add(new JsonObject().put("member", "GHIFR").put("accounts", new JsonArray().add("PP").add("MY"))));
        JsonObject clearerDEFFR = new JsonObject().put("clearer", "DEFFR").put("members", new JsonArray().add(new JsonObject().put("member", "DEFFR").put("accounts", new JsonArray().add("A1").add("A2").add("PP"))));
        JsonObject config = new JsonObject().put("clearers", new JsonArray().add(clearerABCFR).add(clearerDEFFR)).put("productListUrl", "http://localhost:" + port + "/productlist.csv");

        server = DummyWebServer.startWebserver(context, vertx, port, "/productlist.csv", DummyData.productList);

        DeploymentOptions options = new DeploymentOptions().setConfig(config);
        MasterdataVerticleTest.vertx.deployVerticle(MasterdataVerticle.class.getName(), options, context.asyncAssertSuccess());
    }



    private void requestAndCompare(TestContext context, JsonObject request, JsonArray expected)
    {
        final Async asyncRequest = context.async();
        vertx.eventBus().send("masterdata.getMembershipInfo", request, res -> {
            if (res.succeeded())
            {
                context.assertEquals(expected, res.result().body());
                asyncRequest.complete();
            }
            else
            {
                context.fail("Didn't received repsonse from masterdata.getMembershipInfo to query " + Json.encodePrettily(request));
            }
        });

        asyncRequest.awaitSuccess();
    }

    @Test
    public void testRequestNCM(TestContext context)
    {
        JsonObject request = new JsonObject().put("member", "GHIFR");
        JsonArray expected = new JsonArray().add(new JsonObject().put("member", "GHIFR").put("clearer", "ABCFR").put("accounts", new JsonArray().add("PP").add("MY")));
        requestAndCompare(context, request, expected);
    }

    @Test
    public void testRequestDCM(TestContext context)
    {
        JsonObject request = new JsonObject().put("member", "DEFFR");
        JsonArray expected = new JsonArray().add(new JsonObject().put("member", "DEFFR").put("clearer", "DEFFR").put("accounts", new JsonArray().add("A1").add("A2").add("PP")));
        requestAndCompare(context, request, expected);
    }

    @Test
    public void testRequestGCM(TestContext context)
    {
        JsonObject request = new JsonObject().put("member", "ABCFR");
        JsonArray expected = new JsonArray().add(new JsonObject().put("member", "ABCFR").put("clearer", "ABCFR").put("accounts", new JsonArray().add("A1").add("A2").add("PP"))).add(new JsonObject().put("member", "GHIFR").put("clearer", "ABCFR").put("accounts", new JsonArray().add("PP").add("MY")));
        requestAndCompare(context, request, expected);
    }

    @Test
    public void testProductLoad(TestContext context)
    {
        final Async asyncRequest = context.async();
        vertx.eventBus().send("masterdata.getProducts", new JsonObject(), res -> {
            if (res.succeeded())
            {
                context.assertEquals(new JsonArray().add("JUN3").add("1COF").add("1COV"), res.result().body());
                asyncRequest.complete();
            }
            else
            {
                context.fail("Didn't received repsonse from masterdata.getProducts");
            }
        });

        asyncRequest.awaitSuccess();
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        DummyWebServer.stopWebserver(context, server);
        MasterdataVerticleTest.vertx.close(context.asyncAssertSuccess());
    }
}
