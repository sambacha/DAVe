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
                .put("model", "AccountMarginModel")
                .put("requestType", "LATEST")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("marginCurrency", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/am/latest?clearer=CLEARER&member=MEMBER&" +
                        "account=ACCOUNT&marginCurrency=CURRENCY", res -> {
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
                .put("model", "AccountMarginModel")
                .put("requestType", "HISTORY")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("marginCurrency", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/am/history?clearer=CLEARER&member=MEMBER&" +
                "account=ACCOUNT&marginCurrency=CURRENCY", res -> {
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
                .put("model", "LiquiGroupMarginModel")
                .put("requestType", "LATEST")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("marginClass", "CLASS")
                .put("marginCurrency", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/lgm/latest?clearer=CLEARER&member=MEMBER&" +
                        "account=ACCOUNT&marginClass=CLASS&marginCurrency=CURRENCY", res -> {
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
                .put("model", "LiquiGroupMarginModel")
                .put("requestType", "HISTORY")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("marginClass", "CLASS")
                .put("marginCurrency", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/lgm/history?clearer=CLEARER&member=MEMBER&" +
                        "account=ACCOUNT&marginClass=CLASS&marginCurrency=CURRENCY", res -> {
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
                .put("model", "LiquiGroupSplitMarginModel")
                .put("requestType", "LATEST")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("liquidationGroup", "GROUP")
                .put("liquidationGroupSplit", "SPLIT")
                .put("marginCurrency", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/lgsm/latest?clearer=CLEARER&member=MEMBER&" +
                "account=ACCOUNT&liquidationGroup=GROUP&liquidationGroupSplit=SPLIT&marginCurrency=CURRENCY", res -> {
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
                .put("model", "LiquiGroupSplitMarginModel")
                .put("requestType", "HISTORY")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
                .put("liquidationGroup", "GROUP")
                .put("liquidationGroupSplit", "SPLIT")
                .put("marginCurrency", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/lgsm/history?clearer=CLEARER&member=MEMBER&" +
                "account=ACCOUNT&liquidationGroup=GROUP&liquidationGroupSplit=SPLIT&marginCurrency=CURRENCY", res -> {
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
                .put("model", "PoolMarginModel")
                .put("requestType", "LATEST")
                .put("clearer", "CLEARER")
                .put("pool", "POOL")
                .put("marginCurrency", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pm/latest?clearer=CLEARER&pool=POOL&" +
                "marginCurrency=CURRENCY", res -> {
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
                .put("model", "PoolMarginModel")
                .put("requestType", "HISTORY")
                .put("clearer", "CLEARER")
                .put("pool", "POOL")
                .put("marginCurrency", "CURRENCY")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pm/history?clearer=CLEARER&pool=POOL&" +
                "marginCurrency=CURRENCY", res -> {
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
                .put("model", "PositionReportModel")
                .put("requestType", "LATEST")
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
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pr/latest?clearer=CLEARER&member=MEMBER&" +
                "account=ACCOUNT&liquidationGroup=GROUP&liquidationGroupSplit=SPLIT&product=PRODUCT&callPut=CALLPUT&" +
                "contractYear=2016&contractMonth=10&expiryDay=5&exercisePrice=1234.5&version=VERSION&" +
                "flexContractSymbol=SYMBOL", res -> {
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
                .put("model", "PositionReportModel")
                .put("requestType", "HISTORY")
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
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pr/history?clearer=CLEARER&member=MEMBER&" +
                "account=ACCOUNT&liquidationGroup=GROUP&liquidationGroupSplit=SPLIT&product=PRODUCT&callPut=CALLPUT&" +
                "contractYear=2016&contractMonth=10&expiryDay=5&exercisePrice=1234.5&version=VERSION&" +
                "flexContractSymbol=SYMBOL", res -> {
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
                .put("model", "RiskLimitUtilizationModel")
                .put("requestType", "LATEST")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("maintainer", "MAINTAINER")
                .put("limitType", "TYPE")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/rlu/latest?clearer=CLEARER&member=MEMBER&" +
                        "maintainer=MAINTAINER&limitType=TYPE", res -> {
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
                .put("model", "RiskLimitUtilizationModel")
                .put("requestType", "HISTORY")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("maintainer", "MAINTAINER")
                .put("limitType", "TYPE")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/rlu/history?clearer=CLEARER&member=MEMBER&" +
                "maintainer=MAINTAINER&limitType=TYPE", res -> {
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
                .put("model", "PositionReportModel")
                .put("requestType", "LATEST")
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT")
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pr/latest?clearer=CLEARER&member=MEMBER&" +
                "account=ACCOUNT", res -> {
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
                .put("model", "PositionReportModel")
                .put("requestType", "LATEST")
                .put("clearer", "CLEARER")
                .put("contractYear", 2016)
        );

        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/pr/latest?clearer=CLEARER&" +
                "contractYear=2016", res -> {
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
