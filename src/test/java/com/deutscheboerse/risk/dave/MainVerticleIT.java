package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.model.PositionReportModel;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import com.deutscheboerse.risk.dave.utils.MongoFiller;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

@RunWith(VertxUnitRunner.class)
public class MainVerticleIT {
    private Vertx vertx;

    @Before
    public void setUp() {
        this.vertx = Vertx.vertx();
    }

    @Test
    public void testPositionReport(TestContext context) throws InterruptedException, UnsupportedEncodingException {
        DeploymentOptions options = getDeploymentOptions();

        vertx.deployVerticle(MainVerticle.class.getName(), options, context.asyncAssertSuccess());
        PersistenceService persistenceProxy = ProxyHelper.createProxy(PersistenceService.class, vertx, PersistenceService.SERVICE_ADDRESS);

        MongoFiller mongoFiller = new MongoFiller(context, persistenceProxy);

        // Feed the data into the store
        mongoFiller.feedPositionReportCollection(1, 30000);
        PositionReportModel latestModel = (PositionReportModel)mongoFiller.getLastModel().orElse(new PositionReportModel());

        StringBuilder url = new StringBuilder("/api/v1.0/pr/latest");

        for (Map.Entry<String, Object> entry: latestModel.getQueryParams()) {
            String param = entry.getValue().toString();
            param = param.isEmpty() ? "*" : URLEncoder.encode(param, "UTF-8");

            url.append("/").append(param);
        }

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(options.getConfig().getJsonObject("http").getInteger("port"), "localhost", url.toString(), res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                try {
                    JsonArray positions = body.toJsonArray();

                    context.assertEquals(1, positions.size());

                    context.assertEquals(latestModel.getMongoDocument(), positions.getJsonObject(0));
                    asyncRest.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            });
        });
    }

    @Test
    public void testFailedDeployment(TestContext context) {
        DeploymentOptions options = getDeploymentOptions();
        options.getConfig().getJsonObject("http", new JsonObject()).put("port", -1);
        vertx.deployVerticle(MainVerticle.class.getName(), options, context.asyncAssertFailure());
    }

    private DeploymentOptions getDeploymentOptions() {
        int httpPort = Integer.getInteger("http.port", 8080);
        int mongoPort = Integer.getInteger("mongodb.port", 27017);

        return new DeploymentOptions().setConfig(new JsonObject()
            .put("http", new JsonObject().put("port", httpPort))
            .put("mongodb", new JsonObject().put("dbName", "DAVe-MainVerticleTest").put("connectionUrl", "mongodb://localhost:" + mongoPort + "/?waitqueuemultiple=20000"))
        );
    }

    @After
    public void cleanup(TestContext context) {
        this.vertx.close(context.asyncAssertSuccess());
    }
}
