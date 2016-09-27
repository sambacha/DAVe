package com.deutscheboerse.risk.dave;

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

        startWebserver(context);

        DeploymentOptions options = new DeploymentOptions().setConfig(config);
        MasterdataVerticleTest.vertx.deployVerticle(MasterdataVerticle.class.getName(), options, context.asyncAssertSuccess());
    }

    private static void startWebserver(TestContext context)
    {
        Async webserverStartup = context.async();

        Router router = Router.router(vertx);
        router.route("/productlist.csv").handler(req -> {
            req.response().setStatusCode(HttpResponseStatus.OK.code()).putHeader("content-type", "text/plain; charset=utf-8").end("PRODUCT_ID;PRODUCT_TYPE;PRODUCT_NAME;PRODUCT_GROUP;CURRENCY;PRODUCT_ISIN;UNDERLYING_ISIN;SHARE_ISIN;CONTRACT_SIZE;CONTRACT_MONTHS;MIN_PRICE_CHANGE;COUNTRY_CODE;GROUP_ID;CASH_MARKET_ID;\n" +
                    " JUN3;OSTK;Jungheinrich ;Equity Options;EUR;DE0006219934;DE0006219934;;100;60;.01;DE;DE12;XETR;;0;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0\n" +
                    "1COF;FSTK;Covestro;Single Stock Futures;EUR;DE000A164GJ2;DE0006062144;;100;36;.0001;DE;DE01;XETR;;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0\n" +
                    "1COV;OSTK;Covestro;Equity Options;EUR;DE0006062144;DE0006062144;;100;60;.01;DE;DE12;XETR;;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0");
        });

        server = vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, res -> {
                    if (res.succeeded())
                    {
                        webserverStartup.complete();
                    }
                    else
                    {
                        context.fail(res.cause());
                    }
                });

        webserverStartup.awaitSuccess();
    }

    private static void stopWebserver(TestContext context)
    {
        Async webserverShutdown = context.async();
        server.close(res -> {
            if (res.succeeded())
            {
                webserverShutdown.complete();
            }
            else
            {
                context.fail(res.cause());
            }
        });

        webserverShutdown.awaitSuccess();
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
    public void testProductParser(TestContext context)
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
        stopWebserver(context);
        MasterdataVerticleTest.vertx.close(context.asyncAssertSuccess());
    }
}
