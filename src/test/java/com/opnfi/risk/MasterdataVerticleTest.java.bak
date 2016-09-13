package com.opnfi.risk;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MasterdataVerticleTest {
    private static Vertx vertx;

    @BeforeClass
    public static void setUp(TestContext context) {
        MasterdataVerticleTest.vertx = Vertx.vertx();
        JsonObject config = new JsonObject()
                 .put("clearers", new JsonArray()
                         .add(new JsonObject().put("clearer", "ABCFR").put("members", new JsonArray()
                                 .add(new JsonObject().put("member", "ABCFR").put("accounts", new JsonArray().add("A1").add("A2").add("PP")))
                                 .add(new JsonObject().put("member", "GHIFR").put("accounts", new JsonArray().add("PP").add("MY")))
                         ))
                         .add(new JsonObject().put("clearer", "DEFFR").put("members", new JsonArray()
                                 .add(new JsonObject().put("member", "DEFFR").put("accounts", new JsonArray().add("A1").add("A2").add("PP")))
                         ))
                 );

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
        JsonArray expected = new JsonArray()
                .add(new JsonObject().put("member", "ABCFR").put("clearer", "ABCFR").put("accounts", new JsonArray().add("A1").add("A2").add("PP")))
                .add(new JsonObject().put("member", "GHIFR").put("clearer", "ABCFR").put("accounts", new JsonArray().add("PP").add("MY")));
        requestAndCompare(context, request, expected);
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        MasterdataVerticleTest.vertx.close(context.asyncAssertSuccess());
    }
}
