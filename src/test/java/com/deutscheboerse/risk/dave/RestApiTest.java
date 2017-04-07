package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.model.*;
import com.deutscheboerse.risk.dave.persistence.EchoPersistenceService;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import com.deutscheboerse.risk.dave.persistence.RequestType;
import com.deutscheboerse.risk.dave.utils.DataHelper;
import com.deutscheboerse.risk.dave.utils.TestConfig;
import com.deutscheboerse.risk.dave.util.URIBuilder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
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
        RestApiTest.port = TestConfig.API_PORT;

        JsonObject config = TestConfig.getHttpConfig();
        vertx.deployVerticle(HttpVerticle.class.getName(), new DeploymentOptions().setConfig(config), context.asyncAssertSuccess());

        ProxyHelper.registerService(PersistenceService.class, vertx, new EchoPersistenceService(vertx), PersistenceService.SERVICE_ADDRESS);
        persistenceProxy = ProxyHelper.createProxy(PersistenceService.class, vertx, PersistenceService.SERVICE_ADDRESS);
        persistenceProxy.initialize(context.asyncAssertSuccess());
    }

    @Test
    public void testAccountMarginLatest(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/am/latest", RequestType.LATEST, AccountMarginModel.class);
    }

    @Test
    public void testAccountMarginHistory(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/am/history", RequestType.HISTORY, AccountMarginModel.class);
    }

    @Test
    public void testLiquiGroupMarginLatest(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/lgm/latest", RequestType.LATEST, LiquiGroupMarginModel.class);
    }

    @Test
    public void testLiquiGroupMarginHistory(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/lgm/history", RequestType.HISTORY, LiquiGroupMarginModel.class);
    }

    @Test
    public void testLiquiGroupSplitMarginLatest(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/lgsm/latest", RequestType.LATEST, LiquiGroupSplitMarginModel.class);
    }

    @Test
    public void testLiquiGroupSplitMarginHistory(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/lgsm/history", RequestType.HISTORY, LiquiGroupSplitMarginModel.class);
    }

    @Test
    public void testPoolMarginLatest(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/pm/latest", RequestType.LATEST, PoolMarginModel.class);
    }

    @Test
    public void testPoolMarginHistory(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/pm/history", RequestType.HISTORY, PoolMarginModel.class);
    }

    @Test
    public void testPositionReportLatest(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/pr/latest", RequestType.LATEST, PositionReportModel.class);
    }

    @Test
    public void testPositionReportHistory(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/pr/history", RequestType.HISTORY, PositionReportModel.class);
    }

    @Test
    public void testRiskLimitUtilizationLatest(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/rlu/latest", RequestType.LATEST, RiskLimitUtilizationModel.class);
    }

    @Test
    public void testRiskLimitUtilizationHistory(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/rlu/history", RequestType.HISTORY, RiskLimitUtilizationModel.class);
    }

    @Test
    public void testIncompleteUrl(TestContext context) {
        JsonObject queryParams = new JsonObject()
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT");

        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("model", "PositionReportModel")
                .put("requestType", "LATEST")
                .mergeIn(queryParams));

        final Async async = context.async();
        HttpClientOptions sslOpts = new HttpClientOptions().setSsl(true)
                .setVerifyHost(false).setPemTrustOptions(TestConfig.HTTP_API_CERTIFICATE.trustOptions());
        vertx.createHttpClient(sslOpts).getNow(port, "localhost", new URIBuilder("/api/v1.0/pr/latest").addParams(queryParams).build(), res -> {
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
    public void testBadDataType(TestContext context) {
        JsonObject queryParams = new JsonObject()
            .put("clearer", "CLEARER")
            .put("member", "MEMBER")
            .put("contractYear", 1234.5d);

        final Async async = context.async();
        HttpClientOptions sslOpts = new HttpClientOptions().setSsl(true)
                .setVerifyHost(false).setPemTrustOptions(TestConfig.HTTP_API_CERTIFICATE.trustOptions());
        vertx.createHttpClient(sslOpts).getNow(port, "localhost", new URIBuilder("/api/v1.0/pr/latest").addParams(queryParams).build(), res -> {
            context.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), res.statusCode());
            async.complete();
        });

        async.awaitSuccess(30000);
    }

    @Test
    public void testUnknownParameter(TestContext context) {
        JsonObject queryParams = new JsonObject()
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("foo", 2016.2);

        final Async async = context.async();
        HttpClientOptions sslOpts = new HttpClientOptions().setSsl(true)
                .setVerifyHost(false).setPemTrustOptions(TestConfig.HTTP_API_CERTIFICATE.trustOptions());
        vertx.createHttpClient(sslOpts).getNow(port, "localhost", new URIBuilder("/api/v1.0/pr/latest").addParams(queryParams).build(), res -> {
            context.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), res.statusCode());
            async.complete();
        });

        async.awaitSuccess(30000);
    }

    private void testCompleteUrl(TestContext context, String uri, RequestType requestType, Class<? extends AbstractModel> modelClazz) {
        JsonObject queryParams = DataHelper.getQueryParams(DataHelper.getLastModelFromFile(modelClazz, 1));

        JsonArray expectedResult = new JsonArray().add(new JsonObject()
                .put("model", modelClazz.getSimpleName())
                .put("requestType", requestType)
                .mergeIn(queryParams)
        );

        final Async async = context.async();
        HttpClientOptions sslOpts = new HttpClientOptions().setSsl(true)
                .setVerifyHost(false).setPemTrustOptions(TestConfig.HTTP_API_CERTIFICATE.trustOptions());
        vertx.createHttpClient(sslOpts).getNow(port, "localhost", new URIBuilder(uri).addParams(queryParams).build(), res -> {
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
