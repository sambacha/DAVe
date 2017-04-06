package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.model.PositionReportModel;
import com.deutscheboerse.risk.dave.utils.DataHelper;
import com.deutscheboerse.risk.dave.utils.MongoFiller;
import com.deutscheboerse.risk.dave.utils.URIBuilder;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;

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

        // Create mongo client
        JsonObject mongoConfig = options.getConfig().getJsonObject("mongodb");
        JsonObject mongoClientConfig = new JsonObject()
            .put("db_name", mongoConfig.getString("dbName"))
            .put("connection_string", mongoConfig.getString("connectionUrl"));

        MongoClient mongoClient = MongoClient.createShared(this.vertx, mongoClientConfig);
        MongoFiller mongoFiller = new MongoFiller(context, mongoClient);

        // Feed the data into the store
        mongoFiller.feedPositionReportCollection(1, 30000);
        PositionReportModel latestModel = (PositionReportModel)mongoFiller.getLastModel().orElse(new PositionReportModel());

        mongoClient.close();

        // Deploy MainVerticle
        final Async deployAsync = context.async();
        vertx.deployVerticle(MainVerticle.class.getName(), options, context.asyncAssertSuccess(res -> deployAsync.complete()));

        deployAsync.awaitSuccess(30000);

        String uri = new URIBuilder("/api/v1.0/pr/latest")
                .addParams(DataHelper.getQueryParams(latestModel))
                .build();

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(options.getConfig().getJsonObject("http").getInteger("port"), "localhost", uri, res -> {
            context.assertEquals(200, res.statusCode());
            res.bodyHandler(body -> {
                try {
                    JsonArray positions = body.toJsonArray();
                    context.assertEquals(1, positions.size());
                    this.assertDocumentsEquals(context, DataHelper.getMongoDocument(latestModel), positions.getJsonObject(0));
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

    @Test
    public void testFailedDeploymentWrongConfig(TestContext context) {
        Async mainVerticleAsync = context.async();
        DeploymentOptions options = getDeploymentOptions();
        System.setProperty("dave.configurationFile", "nonexisting");
        this.vertx.deployVerticle(MainVerticle.class.getName(), options, ar -> {
            System.clearProperty("dave.configurationFile");
            if (ar.succeeded()) {
                context.fail(ar.cause());
            } else {
                mainVerticleAsync.complete();
            }
        });
    }

    private DeploymentOptions getDeploymentOptions() {
        int httpPort = Integer.getInteger("http.port", 8080);
        int mongoPort = Integer.getInteger("mongodb.port", 27017);

        return new DeploymentOptions().setConfig(new JsonObject()
            .put("http", new JsonObject().put("port", httpPort))
            .put("mongodb", new JsonObject().put("dbName", "DAVe-MainVerticleTest").put("connectionUrl", "mongodb://localhost:" + mongoPort + "/?waitqueuemultiple=20000"))
        );
    }

    private void assertDocumentsEquals(TestContext context, JsonObject expected, JsonObject document) {
        context.assertEquals(expected.remove("_id"), document.remove("id"));
    }

    @After
    public void cleanup(TestContext context) {
        this.vertx.close(context.asyncAssertSuccess());
    }
}
