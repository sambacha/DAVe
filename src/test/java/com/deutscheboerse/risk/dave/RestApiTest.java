package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.persistence.EchoPersistenceService;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(VertxUnitRunner.class)
public class RestApiTest {
    private static Vertx vertx;
    private static int port;
    private static PersistenceService persistenceProxy;

    @BeforeClass
    public static void setUp(TestContext context) throws IOException {
        RestApiTest.vertx = Vertx.vertx();
        RestApiTest.port = Integer.getInteger("http.port", 8080);

        JsonObject config = new JsonObject().put("port", port);
        vertx.deployVerticle(HttpVerticle.class.getName(), new DeploymentOptions().setConfig(config), context.asyncAssertSuccess());

        ProxyHelper.registerService(PersistenceService.class, vertx, new EchoPersistenceService(), PersistenceService.SERVICE_ADDRESS);
        persistenceProxy = ProxyHelper.createProxy(PersistenceService.class, vertx, PersistenceService.SERVICE_ADDRESS);
        persistenceProxy.initialize(context.asyncAssertSuccess());
    }

    @Test
    public void testTradingSessionStatusLatest(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("method", "latestTradingSessionStatus")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/tss/latest", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(expectedResult, bd);
                async.complete();
            });
        });

        async.awaitSuccess(30000);
    }

    @Test
    public void testTradingSessionStatusHistory(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("method", "historyTradingSessionStatus")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/tss/history", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                System.out.println(body.toJsonArray().encodePrettily());
                JsonArray bd = body.toJsonArray();
                context.assertEquals(expectedResult, bd);
                async.complete();
            });
        });

        async.awaitSuccess(30000);
    }

    @Test
    public void testPositionReportLatest(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("method", "latestPositionReport")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("clss", "CLASS")
                .put("symbol", "SYMBOL")
                .put("putCall", "PUTCALL")
                .put("strikePrice", "STRIKE")
                .put("optAttribute", "OPTAT")
                .put("maturityMonthYear", "MMY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pr/latest/CLEARER/MEMBER/ACCOUNT/CLASS/SYMBOL/PUTCALL/STRIKE/OPTAT/MMY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(expectedResult, bd);
                async.complete();
            });
        });

        async.awaitSuccess(30000);
    }

    @Test
    public void testPositionReportHistory(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("method", "historyPositionReport")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("clss", "CLASS")
                .put("symbol", "SYMBOL")
                .put("putCall", "PUTCALL")
                .put("strikePrice", "STRIKE")
                .put("optAttribute", "OPTAT")
                .put("maturityMonthYear", "MMY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pr/history/CLEARER/MEMBER/ACCOUNT/CLASS/SYMBOL/PUTCALL/STRIKE/OPTAT/MMY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(expectedResult, bd);
                async.complete();
            });
        });

        async.awaitSuccess(30000);
    }

    @Test
    public void testMarginComponentLatest(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("method", "latestMarginComponent")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("clss", "CLASS")
                .put("ccy", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/mc/latest/CLEARER/MEMBER/ACCOUNT/CLASS/CURRENCY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(expectedResult, bd);
                async.complete();
            });
        });

        async.awaitSuccess(30000);
    }

    @Test
    public void testMarginComponentHistory(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("method", "historyMarginComponent")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("clss", "CLASS")
                .put("ccy", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/mc/history/CLEARER/MEMBER/ACCOUNT/CLASS/CURRENCY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(expectedResult, bd);
                async.complete();
            });
        });

        async.awaitSuccess(30000);
    }

    @Test
    public void testTotalMarginRequirementLatest(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("method", "latestTotalMarginRequirement")
                .put("clearer", "CLEARER")
                .put("pool", "POOL")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("ccy", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/tmr/latest/CLEARER/POOL/MEMBER/ACCOUNT/CURRENCY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(expectedResult, bd);
                async.complete();
            });
        });

        async.awaitSuccess(30000);
    }

    @Test
    public void testTotalMarginRequirementHistory(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("method", "historyTotalMarginRequirement")
                .put("clearer", "CLEARER")
                .put("pool", "POOL")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("ccy", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/tmr/history/CLEARER/POOL/MEMBER/ACCOUNT/CURRENCY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(expectedResult, bd);
                async.complete();
            });
        });

        async.awaitSuccess(30000);
    }

    @Test
    public void testMarginShortfallSurplusLatest(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("method", "latestMarginShortfallSurplus")
                .put("clearer", "CLEARER")
                .put("pool", "POOL")
                .put("member", "MEMBER")
                .put("clearingCcy", "CLEARINGCURRENCY")
                .put("ccy", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/mss/latest/CLEARER/POOL/MEMBER/CLEARINGCURRENCY/CURRENCY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(expectedResult, bd);
                async.complete();
            });
        });

        async.awaitSuccess(30000);
    }

    @Test
    public void testMarginShortfallSurplusHistory(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("method", "historyMarginShortfallSurplus")
                .put("clearer", "CLEARER")
                .put("pool", "POOL")
                .put("member", "MEMBER")
                .put("clearingCcy", "CLEARINGCURRENCY")
                .put("ccy", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/mss/history/CLEARER/POOL/MEMBER/CLEARINGCURRENCY/CURRENCY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(expectedResult, bd);
                async.complete();
            });
        });

        async.awaitSuccess(30000);
    }

    @Test
    public void testRiskLimitLatest(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("method", "latestRiskLimit")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("maintainer", "MAINTAINER")
                .put("limitType", "TYPE")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/rl/latest/CLEARER/MEMBER/MAINTAINER/TYPE", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(expectedResult, bd);
                async.complete();
            });
        });

        async.awaitSuccess(30000);
    }

    @Test
    public void testRiskLimitHistory(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("method", "historyRiskLimit")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("maintainer", "MAINTAINER")
                .put("limitType", "TYPE")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/rl/history/CLEARER/MEMBER/MAINTAINER/TYPE", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(expectedResult, bd);
                async.complete();
            });
        });

        async.awaitSuccess(30000);
    }

    @Test
    public void testIncompleteUrl(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("method", "latestPositionReport")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pr/latest/CLEARER/MEMBER/ACCOUNT", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(expectedResult, bd);
                async.complete();
            });
        });

        async.awaitSuccess(30000);
    }

    @Test
    public void testStars(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("method", "latestPositionReport")
                .put("clearer", "CLEARER")
                .put("maturityMonthYear", "MMY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pr/latest/CLEARER/*/*/*/*/*/*/*/MMY", res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                JsonArray bd = body.toJsonArray();
                context.assertEquals(expectedResult, bd);
                async.complete();
            });
        });

        async.awaitSuccess(30000);
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        persistenceProxy.close();
        vertx.close(context.asyncAssertSuccess());
    }

}
