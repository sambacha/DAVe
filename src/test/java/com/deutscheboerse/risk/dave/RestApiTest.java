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
    public void testAccountMarginLatest(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("collection", "AccountMargin.latest")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("marginCurrency", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/am/latest/CLEARER/MEMBER/ACCOUNT/CURRENCY", res -> {
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
    public void testAccountMarginHistory(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("collection", "AccountMargin")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("marginCurrency", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/am/history/CLEARER/MEMBER/ACCOUNT/CURRENCY", res -> {
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
    public void testLiquiGroupMarginLatest(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("collection", "LiquiGroupMargin.latest")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("marginClass", "CLASS")
                .put("marginCurrency", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/lgm/latest/CLEARER/MEMBER/ACCOUNT/CLASS/CURRENCY", res -> {
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
    public void testLiquiGroupMarginHistory(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("collection", "LiquiGroupMargin")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("marginClass", "CLASS")
                .put("marginCurrency", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/lgm/history/CLEARER/MEMBER/ACCOUNT/CLASS/CURRENCY", res -> {
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
    public void testLiquiGroupSplitMarginLatest(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("collection", "LiquiGroupSplitMargin.latest")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("liquidationGroup", "GROUP")
                .put("liquidationGroupSplit", "SPLIT")
                .put("marginCurrency", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/lgsm/latest/CLEARER/MEMBER/ACCOUNT/GROUP/SPLIT/CURRENCY", res -> {
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
    public void testLiquiGroupSplitMarginHistory(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("collection", "LiquiGroupSplitMargin")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("liquidationGroup", "GROUP")
                .put("liquidationGroupSplit", "SPLIT")
                .put("marginCurrency", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/lgsm/history/CLEARER/MEMBER/ACCOUNT/GROUP/SPLIT/CURRENCY", res -> {
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
    public void testPoolMarginLatest(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("collection", "PoolMargin.latest")
                .put("clearer", "CLEARER")
                .put("pool", "POOL")
                .put("marginCurrency", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pm/latest/CLEARER/POOL/CURRENCY", res -> {
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
    public void testPoolMarginHistory(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("collection", "PoolMargin")
                .put("clearer", "CLEARER")
                .put("pool", "POOL")
                .put("marginCurrency", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pm/history/CLEARER/POOL/CURRENCY", res -> {
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
    public void testPositionReportLatest(TestContext context) {

        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("collection", "PositionReport.latest")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("liquidationGroup", "GROUP")
                .put("liquidationGroupSplit", "SPLIT")
                .put("product", "PRODUCT")
                .put("callPut", "CALLPUT")
                .put("contractYear", 2016)
                .put("contractMonth", 10)
                .put("expiryDay", 5)
                .put("exercisePrice", 1234.5)
                .put("version", "VERSION")
                .put("flexContractSymbol", "SYMBOL")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost",
                "/api/v1.0/pr/latest/CLEARER/MEMBER/ACCOUNT/GROUP/SPLIT/PRODUCT/CALLPUT/2016/10/5/1234.5/VERSION/SYMBOL", res -> {
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
                .put("collection", "PositionReport")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("liquidationGroup", "GROUP")
                .put("liquidationGroupSplit", "SPLIT")
                .put("product", "PRODUCT")
                .put("callPut", "CALLPUT")
                .put("contractYear", 2016)
                .put("contractMonth", 10)
                .put("expiryDay", 5)
                .put("exercisePrice", 1234.5)
                .put("version", "VERSION")
                .put("flexContractSymbol", "SYMBOL")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost",
                "/api/v1.0/pr/history/CLEARER/MEMBER/ACCOUNT/GROUP/SPLIT/PRODUCT/CALLPUT/2016/10/5/1234.5/VERSION/SYMBOL", res -> {
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
    public void testRiskLimitUtilizationLatest(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("collection", "RiskLimitUtilization.latest")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("maintainer", "MAINTAINER")
                .put("limitType", "TYPE")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/rlu/latest/CLEARER/MEMBER/MAINTAINER/TYPE", res -> {
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
    public void testRiskLimitUtilizationHistory(TestContext context) {
        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("collection", "RiskLimitUtilization")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("maintainer", "MAINTAINER")
                .put("limitType", "TYPE")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/rlu/history/CLEARER/MEMBER/MAINTAINER/TYPE", res -> {
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
                .put("collection", "PositionReport.latest")
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
                .put("collection", "PositionReport.latest")
                .put("clearer", "CLEARER")
                .put("contractYear", 2016)
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pr/latest/CLEARER/*/*/*/*/*/*/2016", res -> {
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
