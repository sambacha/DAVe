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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

@RunWith(VertxUnitRunner.class)
public class MainVerticleIT {
    private static Vertx vertx;
    private static int httpPort;
    private static int mongoPort;
    private static PersistenceService persistenceProxy;

    @BeforeClass
    public static void setUp(TestContext context) {
        vertx = Vertx.vertx();

        httpPort = Integer.getInteger("http.port", 8080);
        mongoPort = Integer.getInteger("mongodb.port", 27017);

        JsonObject config = new JsonObject();
        config.put("http", new JsonObject().put("port", httpPort));
        config.put("mongodb", new JsonObject().put("dbName", "DAVe-MainVerticleTest").put("connectionUrl", "mongodb://localhost:" + mongoPort + "/?waitqueuemultiple=20000"));
        vertx.deployVerticle(MainVerticle.class.getName(), new DeploymentOptions().setConfig(config), context.asyncAssertSuccess());

        MainVerticleIT.persistenceProxy = ProxyHelper.createProxy(PersistenceService.class, vertx, PersistenceService.SERVICE_ADDRESS);
    }

    @Test
    public void testPositionReport(TestContext context) throws InterruptedException, UnsupportedEncodingException {
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
        vertx.createHttpClient().getNow(httpPort, "localhost", url.toString(), res -> {
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

    @AfterClass
    public static void tearDown(TestContext context) {
        MainVerticleIT.vertx.close(context.asyncAssertSuccess());
    }
}
