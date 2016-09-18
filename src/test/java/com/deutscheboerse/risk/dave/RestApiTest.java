package com.deutscheboerse.risk.dave;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
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

import java.io.IOException;

/**
 * Created by jakub on 03.09.16.
 */
@RunWith(VertxUnitRunner.class)
public class RestApiTest {
    private static Vertx vertx;
    private static int port;

    @BeforeClass
    public static void setUp(TestContext context) throws IOException {
        RestApiTest.vertx = Vertx.vertx();
        RestApiTest.port = Integer.getInteger("http.port", 8080);

        JsonObject config = new JsonObject().put("httpPort", port);
        vertx.deployVerticle(HttpVerticle.class.getName(), new DeploymentOptions().setConfig(config), context.asyncAssertSuccess());
    }

    @Test
    public void testTradingSessionStatusLatest(TestContext context) {
        final Async asyncQuery = context.async();
        MessageConsumer cons = vertx.eventBus().consumer("query.latestTradingSessionStatus", msg -> {
            try
            {
                JsonObject params = (JsonObject)msg.body();

                context.assertEquals(new JsonObject(), params);
                msg.reply(Json.encodePrettily(new JsonObject()));
                asyncQuery.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/tss/latest", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonObject bd = body.toJsonObject();
                context.assertEquals(new JsonObject(), bd);
                asyncRest.complete();
            });
        });

        asyncQuery.awaitSuccess();
        asyncRest.awaitSuccess();
        cons.unregister(context.asyncAssertSuccess());
    }

    @Test
    public void testTradingSessionStatusHistory(TestContext context) {
        final Async asyncQuery = context.async();
        MessageConsumer cons = vertx.eventBus().consumer("query.historyTradingSessionStatus", msg -> {
            try
            {
                JsonObject params = (JsonObject)msg.body();

                context.assertEquals(new JsonObject(), params);
                msg.reply(Json.encodePrettily(new JsonArray()));
                asyncQuery.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/tss/history", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(new JsonArray(), bd);
                asyncRest.complete();
            });
        });

        asyncQuery.awaitSuccess();
        asyncRest.awaitSuccess();
        cons.unregister(context.asyncAssertSuccess());
    }

    @Test
    public void testPositionReportLatest(TestContext context) {
        final Async asyncQuery = context.async();
        MessageConsumer cons = vertx.eventBus().consumer("query.latestPositionReport", msg -> {
            try
            {
                JsonObject params = (JsonObject)msg.body();

                context.assertEquals("CLEARER", params.getString("clearer"));
                context.assertEquals("MEMBER", params.getString("member"));
                context.assertEquals("ACCOUNT", params.getString("account"));
                context.assertEquals("SYMBOL", params.getString("symbol"));
                context.assertEquals("PUTCALL", params.getString("putCall"));
                context.assertEquals("STRIKE", params.getString("strikePrice"));
                context.assertEquals("OPTAT", params.getString("optAttribute"));
                context.assertEquals("MMY", params.getString("maturityMonthYear"));

                msg.reply(Json.encodePrettily(new JsonArray()));
                asyncQuery.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pr/latest/CLEARER/MEMBER/ACCOUNT/SYMBOL/PUTCALL/STRIKE/OPTAT/MMY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(new JsonArray(), bd);
                asyncRest.complete();
            });
        });

        asyncQuery.awaitSuccess();
        asyncRest.awaitSuccess();
        cons.unregister(context.asyncAssertSuccess());
    }

    @Test
    public void testPositionReportHistory(TestContext context) {
        final Async asyncQuery = context.async();
        MessageConsumer cons = vertx.eventBus().consumer("query.historyPositionReport", msg -> {
            try
            {
                JsonObject params = (JsonObject)msg.body();

                context.assertEquals("CLEARER", params.getString("clearer"));
                context.assertEquals("MEMBER", params.getString("member"));
                context.assertEquals("ACCOUNT", params.getString("account"));
                context.assertEquals("SYMBOL", params.getString("symbol"));
                context.assertEquals("PUTCALL", params.getString("putCall"));
                context.assertEquals("STRIKE", params.getString("strikePrice"));
                context.assertEquals("OPTAT", params.getString("optAttribute"));
                context.assertEquals("MMY", params.getString("maturityMonthYear"));

                msg.reply(Json.encodePrettily(new JsonArray()));
                asyncQuery.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pr/history/CLEARER/MEMBER/ACCOUNT/SYMBOL/PUTCALL/STRIKE/OPTAT/MMY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(new JsonArray(), bd);
                asyncRest.complete();
            });
        });

        asyncQuery.awaitSuccess();
        asyncRest.awaitSuccess();
        cons.unregister(context.asyncAssertSuccess());
    }

    @Test
    public void testMarginComponentLatest(TestContext context) {
        final Async asyncQuery = context.async();
        MessageConsumer cons = vertx.eventBus().consumer("query.latestMarginComponent", msg -> {
            try
            {
                JsonObject params = (JsonObject)msg.body();

                context.assertEquals("CLEARER", params.getString("clearer"));
                context.assertEquals("MEMBER", params.getString("member"));
                context.assertEquals("ACCOUNT", params.getString("account"));
                context.assertEquals("CLASS", params.getString("clss"));
                context.assertEquals("CURRENCY", params.getString("ccy"));

                msg.reply(Json.encodePrettily(new JsonArray()));
                asyncQuery.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/mc/latest/CLEARER/MEMBER/ACCOUNT/CLASS/CURRENCY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(new JsonArray(), bd);
                asyncRest.complete();
            });
        });

        asyncQuery.awaitSuccess();
        asyncRest.awaitSuccess();
        cons.unregister(context.asyncAssertSuccess());
    }

    @Test
    public void testMarginComponentHistory(TestContext context) {
        final Async asyncQuery = context.async();
        MessageConsumer cons = vertx.eventBus().consumer("query.historyMarginComponent", msg -> {
            try
            {
                JsonObject params = (JsonObject)msg.body();

                context.assertEquals("CLEARER", params.getString("clearer"));
                context.assertEquals("MEMBER", params.getString("member"));
                context.assertEquals("ACCOUNT", params.getString("account"));
                context.assertEquals("CLASS", params.getString("clss"));
                context.assertEquals("CURRENCY", params.getString("ccy"));

                msg.reply(Json.encodePrettily(new JsonArray()));
                asyncQuery.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/mc/history/CLEARER/MEMBER/ACCOUNT/CLASS/CURRENCY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(new JsonArray(), bd);
                asyncRest.complete();
            });
        });

        asyncQuery.awaitSuccess();
        asyncRest.awaitSuccess();
        cons.unregister(context.asyncAssertSuccess());
    }

    @Test
    public void testTotalMarginRequirementLatest(TestContext context) {
        final Async asyncQuery = context.async();
        MessageConsumer cons = vertx.eventBus().consumer("query.latestTotalMarginRequirement", msg -> {
            try
            {
                JsonObject params = (JsonObject)msg.body();

                context.assertEquals("CLEARER", params.getString("clearer"));
                context.assertEquals("POOL", params.getString("pool"));
                context.assertEquals("MEMBER", params.getString("member"));
                context.assertEquals("ACCOUNT", params.getString("account"));
                context.assertEquals("CURRENCY", params.getString("ccy"));

                msg.reply(Json.encodePrettily(new JsonArray()));
                asyncQuery.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/tmr/latest/CLEARER/POOL/MEMBER/ACCOUNT/CURRENCY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(new JsonArray(), bd);
                asyncRest.complete();
            });
        });

        asyncQuery.awaitSuccess();
        asyncRest.awaitSuccess();
        cons.unregister(context.asyncAssertSuccess());
    }

    @Test
    public void testTotalMarginRequirementHistory(TestContext context) {
        final Async asyncQuery = context.async();
        MessageConsumer cons = vertx.eventBus().consumer("query.historyTotalMarginRequirement", msg -> {
            try
            {
                JsonObject params = (JsonObject)msg.body();

                context.assertEquals("CLEARER", params.getString("clearer"));
                context.assertEquals("POOL", params.getString("pool"));
                context.assertEquals("MEMBER", params.getString("member"));
                context.assertEquals("ACCOUNT", params.getString("account"));
                context.assertEquals("CURRENCY", params.getString("ccy"));

                msg.reply(Json.encodePrettily(new JsonArray()));
                asyncQuery.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/tmr/history/CLEARER/POOL/MEMBER/ACCOUNT/CURRENCY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(new JsonArray(), bd);
                asyncRest.complete();
            });
        });

        asyncQuery.awaitSuccess();
        asyncRest.awaitSuccess();
        cons.unregister(context.asyncAssertSuccess());
    }

    @Test
    public void testMarginShortfallSurplusLatest(TestContext context) {
        final Async asyncQuery = context.async();
        MessageConsumer cons = vertx.eventBus().consumer("query.latestMarginShortfallSurplus", msg -> {
            try
            {
                JsonObject params = (JsonObject)msg.body();

                context.assertEquals("CLEARER", params.getString("clearer"));
                context.assertEquals("POOL", params.getString("pool"));
                context.assertEquals("MEMBER", params.getString("member"));
                context.assertEquals("CLEARINGCURRENCY", params.getString("clearingCcy"));
                context.assertEquals("CURRENCY", params.getString("ccy"));

                msg.reply(Json.encodePrettily(new JsonArray()));
                asyncQuery.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/mss/latest/CLEARER/POOL/MEMBER/CLEARINGCURRENCY/CURRENCY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(new JsonArray(), bd);
                asyncRest.complete();
            });
        });

        asyncQuery.awaitSuccess();
        asyncRest.awaitSuccess();
        cons.unregister(context.asyncAssertSuccess());
    }

    @Test
    public void testMarginShortfallSurplusHistory(TestContext context) {
        final Async asyncQuery = context.async();
        MessageConsumer cons = vertx.eventBus().consumer("query.historyMarginShortfallSurplus", msg -> {
            try
            {
                JsonObject params = (JsonObject)msg.body();

                context.assertEquals("CLEARER", params.getString("clearer"));
                context.assertEquals("POOL", params.getString("pool"));
                context.assertEquals("MEMBER", params.getString("member"));
                context.assertEquals("CLEARINGCURRENCY", params.getString("clearingCcy"));
                context.assertEquals("CURRENCY", params.getString("ccy"));

                msg.reply(Json.encodePrettily(new JsonArray()));
                asyncQuery.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/mss/history/CLEARER/POOL/MEMBER/CLEARINGCURRENCY/CURRENCY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(new JsonArray(), bd);
                asyncRest.complete();
            });
        });

        asyncQuery.awaitSuccess();
        asyncRest.awaitSuccess();
        cons.unregister(context.asyncAssertSuccess());
    }

    @Test
    public void testRiskLimitLatest(TestContext context) {
        final Async asyncQuery = context.async();
        MessageConsumer cons = vertx.eventBus().consumer("query.latestRiskLimit", msg -> {
            try
            {
                JsonObject params = (JsonObject)msg.body();

                context.assertEquals("CLEARER", params.getString("clearer"));
                context.assertEquals("MEMBER", params.getString("member"));
                context.assertEquals("MAINTAINER", params.getString("maintainer"));
                context.assertEquals("TYPE", params.getString("limitType"));

                msg.reply(Json.encodePrettily(new JsonArray()));
                asyncQuery.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/rl/latest/CLEARER/MEMBER/MAINTAINER/TYPE", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(new JsonArray(), bd);
                asyncRest.complete();
            });
        });

        asyncQuery.awaitSuccess();
        asyncRest.awaitSuccess();
        cons.unregister(context.asyncAssertSuccess());
    }

    @Test
    public void testRiskLimitHistory(TestContext context) {
        final Async asyncQuery = context.async();
        MessageConsumer cons = vertx.eventBus().consumer("query.historyRiskLimit", msg -> {
            try
            {
                JsonObject params = (JsonObject)msg.body();

                context.assertEquals("CLEARER", params.getString("clearer"));
                context.assertEquals("MEMBER", params.getString("member"));
                context.assertEquals("MAINTAINER", params.getString("maintainer"));
                context.assertEquals("TYPE", params.getString("limitType"));

                msg.reply(Json.encodePrettily(new JsonArray()));
                asyncQuery.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/rl/history/CLEARER/MEMBER/MAINTAINER/TYPE", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(new JsonArray(), bd);
                asyncRest.complete();
            });
        });

        asyncQuery.awaitSuccess();
        asyncRest.awaitSuccess();
        cons.unregister(context.asyncAssertSuccess());
    }

    @Test
    public void testIncompleteUrl(TestContext context) {
        final Async asyncQuery = context.async();
        MessageConsumer cons = vertx.eventBus().consumer("query.latestPositionReport", msg -> {
            try
            {
                JsonObject params = (JsonObject)msg.body();

                context.assertEquals("CLEARER", params.getString("clearer"));
                context.assertEquals("MEMBER", params.getString("member"));
                context.assertEquals("ACCOUNT", params.getString("account"));
                context.assertNull(params.getString("symbol"));
                context.assertNull(params.getString("putCall"));
                context.assertNull(params.getString("strikePrice"));
                context.assertNull(params.getString("optAttribute"));
                context.assertNull(params.getString("maturityMonthYear"));

                msg.reply(Json.encodePrettily(new JsonArray()));
                asyncQuery.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pr/latest/CLEARER/MEMBER/ACCOUNT", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(new JsonArray(), bd);
                asyncRest.complete();
            });
        });

        asyncQuery.awaitSuccess();
        asyncRest.awaitSuccess();
        cons.unregister(context.asyncAssertSuccess());
    }

    @Test
    public void testStars(TestContext context) {
        final Async asyncQuery = context.async();
        MessageConsumer cons = vertx.eventBus().consumer("query.latestPositionReport", msg -> {
            try
            {
                JsonObject params = (JsonObject)msg.body();

                context.assertEquals("CLEARER", params.getString("clearer"));
                context.assertNull(params.getString("member"));
                context.assertNull(params.getString("account"));
                context.assertNull(params.getString("symbol"));
                context.assertNull(params.getString("putCall"));
                context.assertNull(params.getString("strikePrice"));
                context.assertNull(params.getString("optAttribute"));
                context.assertEquals("MMY", params.getString("maturityMonthYear"));

                msg.reply(Json.encodePrettily(new JsonArray()));
                asyncQuery.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pr/latest/CLEARER/*/*/*/*/*/*/MMY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(new JsonArray(), bd);
                asyncRest.complete();
            });
        });

        asyncQuery.awaitSuccess();
        asyncRest.awaitSuccess();
        cons.unregister(context.asyncAssertSuccess());
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

}
