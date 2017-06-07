package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.model.*;
import com.deutscheboerse.risk.dave.persistence.EchoPersistenceService;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import com.deutscheboerse.risk.dave.persistence.RequestType;
import com.deutscheboerse.risk.dave.utils.DataHelper;
import com.deutscheboerse.risk.dave.utils.ModelBuilder;
import com.deutscheboerse.risk.dave.utils.TestConfig;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Set;
import java.util.function.Function;

@RunWith(VertxUnitRunner.class)
public class RestApiTest {
    private static Vertx vertx;
    private static PersistenceService persistenceProxy;

    @BeforeClass
    public static void setUp(TestContext context) throws IOException {
        RestApiTest.vertx = Vertx.vertx();

        JsonObject config = TestConfig.getApiConfig();
        vertx.deployVerticle(ApiVerticle.class.getName(), new DeploymentOptions().setConfig(config), context.asyncAssertSuccess());

        ProxyHelper.registerService(PersistenceService.class, vertx, new EchoPersistenceService(vertx), PersistenceService.SERVICE_ADDRESS);
        persistenceProxy = ProxyHelper.createProxy(PersistenceService.class, vertx, PersistenceService.SERVICE_ADDRESS);
        persistenceProxy.initialize(context.asyncAssertSuccess());
    }

    @Test
    public void testAccountMarginLatest(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/am/latest", RequestType.LATEST, DataHelper.ACCOUNT_MARGIN_FOLDER,
                AccountMarginModel.FIELD_DESCRIPTOR, ModelBuilder::buildAccountMarginFromJson);
    }

    @Test
    public void testAccountMarginHistory(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/am/history", RequestType.HISTORY, DataHelper.ACCOUNT_MARGIN_FOLDER,
                AccountMarginModel.FIELD_DESCRIPTOR, ModelBuilder::buildAccountMarginFromJson);
    }

    @Test
    public void testLiquiGroupMarginLatest(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/lgm/latest", RequestType.LATEST, DataHelper.LIQUI_GROUP_MARGIN_FOLDER,
                LiquiGroupMarginModel.FIELD_DESCRIPTOR, ModelBuilder::buildLiquiGroupMarginFromJson);
    }

    @Test
    public void testLiquiGroupMarginHistory(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/lgm/history", RequestType.HISTORY, DataHelper.LIQUI_GROUP_MARGIN_FOLDER,
                LiquiGroupMarginModel.FIELD_DESCRIPTOR, ModelBuilder::buildLiquiGroupMarginFromJson);
    }

    @Test
    public void testLiquiGroupSplitMarginLatest(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/lgsm/latest", RequestType.LATEST, DataHelper.LIQUI_GROUP_SPLIT_MARGIN_FOLDER,
                LiquiGroupSplitMarginModel.FIELD_DESCRIPTOR, ModelBuilder::buildLiquiGroupSplitMarginFromJson);
    }

    @Test
    public void testLiquiGroupSplitMarginHistory(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/lgsm/history", RequestType.HISTORY, DataHelper.LIQUI_GROUP_SPLIT_MARGIN_FOLDER,
                LiquiGroupSplitMarginModel.FIELD_DESCRIPTOR, ModelBuilder::buildLiquiGroupSplitMarginFromJson);
    }

    @Test
    public void testPoolMarginLatest(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/pm/latest", RequestType.LATEST, DataHelper.POOL_MARGIN_FOLDER,
                PoolMarginModel.FIELD_DESCRIPTOR, ModelBuilder::buildPoolMarginFromJson);
    }

    @Test
    public void testPoolMarginHistory(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/pm/history", RequestType.HISTORY, DataHelper.POOL_MARGIN_FOLDER,
                PoolMarginModel.FIELD_DESCRIPTOR, ModelBuilder::buildPoolMarginFromJson);
    }

    @Test
    public void testPositionReportLatest(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/pr/latest", RequestType.LATEST, DataHelper.POSITION_REPORT_FOLDER,
                PositionReportModel.FIELD_DESCRIPTOR, ModelBuilder::buildPositionReportFromJson);
    }

    @Test
    public void testPositionReportHistory(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/pr/history", RequestType.HISTORY, DataHelper.POSITION_REPORT_FOLDER,
                PositionReportModel.FIELD_DESCRIPTOR, ModelBuilder::buildPositionReportFromJson);
    }

    @Test
    public void testRiskLimitUtilizationLatest(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/rlu/latest", RequestType.LATEST, DataHelper.RISK_LIMIT_UTILIZATION_FOLDER,
                RiskLimitUtilizationModel.FIELD_DESCRIPTOR, ModelBuilder::buildRiskLimitUtilizationFromJson);
    }

    @Test
    public void testRiskLimitUtilizationHistory(TestContext context) {
        this.testCompleteUrl(context, "/api/v1.0/rlu/history", RequestType.HISTORY, DataHelper.RISK_LIMIT_UTILIZATION_FOLDER,
                RiskLimitUtilizationModel.FIELD_DESCRIPTOR, ModelBuilder::buildRiskLimitUtilizationFromJson);
    }

    @Test
    public void testIncompleteUrl(TestContext context) {
        JsonObject queryParams = new JsonObject()
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("account", "ACCOUNT");

        JsonArray expectedResult = new JsonArray().add(ModelBuilder.buildPositionReportFromJson(
                new JsonObject().mergeIn(queryParams)
                        .put("snapshotID", ModelBuilder.LATEST_SNAPSHOT_ID)
                        .put("businessDate", ModelBuilder.BUSINESS_DATE)
        ).toApplicationJson());

        final Async async = context.async();
        createSslRequest("/api/v1.0/pr/latest", queryParams)
                .send(context.asyncAssertSuccess(res -> {
                    context.assertEquals(200, res.statusCode());

                    JsonArray bd = res.body().toJsonArray();
                    context.assertEquals(expectedResult, bd);
                    async.complete();
                }));

        async.awaitSuccess(30000);
    }

    @Test
    public void testBadDataType(TestContext context) {
        JsonObject queryParams = new JsonObject()
            .put("clearer", "CLEARER")
            .put("member", "MEMBER")
            .put("contractYear", 1234.5d);

        final Async async = context.async();
        createSslRequest("/api/v1.0/pr/latest", queryParams)
                .send(context.asyncAssertSuccess(res -> {
                    context.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), res.statusCode());
                    async.complete();
                }));

        async.awaitSuccess(30000);
    }

    @Test
    public void testUnknownParameter(TestContext context) {
        JsonObject queryParams = new JsonObject()
                .put("clearer", "CLEARER")
                .put("member", "MEMBER")
                .put("foo", 2016.2);

        final Async async = context.async();
        createSslRequest("/api/v1.0/pr/latest", queryParams)
                .send(context.asyncAssertSuccess(res -> {
                    context.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), res.statusCode());
                    async.complete();
                }));

        async.awaitSuccess(30000);
    }

    private <T extends Model>
    void testCompleteUrl(TestContext context, String uri, RequestType requestType, String dataFolder,
                         FieldDescriptor<T> fieldDescriptor,
                         Function<JsonObject, T> modelBuilder) {
        JsonObject queryParams = DataHelper.getLastJsonFromFile(dataFolder, 1).orElseThrow(RuntimeException::new);

        queryParams = retainJsonFields(queryParams, fieldDescriptor.getUniqueFields().keySet());

        JsonArray expectedResult = new JsonArray().add(modelBuilder.apply(
                new JsonObject().mergeIn(queryParams)
                        .put("snapshotID", requestType == RequestType.LATEST
                                ? ModelBuilder.LATEST_SNAPSHOT_ID
                                : ModelBuilder.HISTORY_SNAPSHOT_ID)
                        .put("businessDate", ModelBuilder.BUSINESS_DATE)
        ).toApplicationJson());

        final Async async = context.async();
        createSslRequest(uri, queryParams)
                .send(context.asyncAssertSuccess(res -> {
                    context.assertEquals(200, res.statusCode());

                    JsonArray bd = res.body().toJsonArray();
                    context.assertEquals(expectedResult, bd);
                    async.complete();
                }));

        async.awaitSuccess(5000);
    }

    private static JsonObject retainJsonFields(JsonObject json, Set<String> retainFields) {
        JsonObject result = new JsonObject();
        json.forEach(entry -> {
            if (retainFields.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        });
        return result;
    }

    private HttpRequest<Buffer> createSslRequest(String uri, JsonObject params) {
        WebClientOptions sslOpts = new WebClientOptions()
                .setSsl(true)
                .setPemTrustOptions(TestConfig.HTTP_API_CERTIFICATE.trustOptions());

        HttpRequest<Buffer> httpRequest = WebClient.create(vertx, sslOpts)
                .get(TestConfig.API_PORT, "localhost", uri);

        params.forEach(entry -> httpRequest.addQueryParam(entry.getKey(), entry.getValue().toString()));

        return httpRequest;
    }

    @AfterClass
    public static void tearDown(TestContext context) throws InterruptedException {
        persistenceProxy.close(context.asyncAssertSuccess(res ->
                vertx.close(context.asyncAssertSuccess())
        ));
    }
}
