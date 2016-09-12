package com.opnfi.risk;

import com.opnfi.risk.util.UserManagerVerticle;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.mongo.HashSaltStyle;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RunWith(VertxUnitRunner.class)
public class UserManagerVerticleIT {
    private static final String USER_COLLECTION_NAME = "user";
    private static final String SALT = "OpnFiRisk";
    private static final String USER = "user1";
    private static final String PASSWORD = "123456";

    private static Vertx vertx;
    private static MongoClient mongoClient;
    private static int mongoPort;
    private static String dbName;

    @BeforeClass
    public static void setUp(TestContext context) {
        UserManagerVerticleIT.vertx = Vertx.vertx();
        UserManagerVerticleIT.mongoPort = Integer.getInteger("mongodb.port", 27017);
        UserManagerVerticleIT.dbName = "OpnFi-Risk-Test" + UUID.randomUUID().getLeastSignificantBits();

        JsonObject dbConfig = new JsonObject();
        dbConfig.put("db_name", dbName);
        dbConfig.put("useObjectId", true);
        dbConfig.put("connection_string", "mongodb://localhost:" + mongoPort);

        UserManagerVerticleIT.mongoClient = MongoClient.createShared(UserManagerVerticleIT.vertx, dbConfig);
    }

    private void deployUserManagerVerticle(TestContext context, Vertx vertx, JsonObject config)
    {
        final Async asyncStart = context.async();

        vertx.deployVerticle(UserManagerVerticle.class.getName(), new DeploymentOptions().setConfig(config), res -> {
            if (res.succeeded()) {
                asyncStart.complete();
            }
            else
            {
                context.fail(res.cause());
            }
        });

        asyncStart.awaitSuccess();
    }

    @Test
    public void testInsertUser(TestContext context)
    {
        System.setProperty("cmd", "insert");
        System.setProperty("userName", USER);
        System.setProperty("userPassword", PASSWORD);

        JsonObject config = new JsonObject().put("http", new JsonObject().put("auth", new JsonObject().put("enable", true).put("db_name", dbName).put("connection_string", "mongodb://localhost:" + mongoPort).put("salt", SALT)));
        deployUserManagerVerticle(context, vertx, config);

        final Async query = context.async();
        mongoClient.find(USER_COLLECTION_NAME, new JsonObject(), res -> {
            if (res.succeeded()) {
                List<JsonObject> users = res.result();
                context.assertEquals(1, users.size());
                context.assertEquals(USER, users.get(0).getString("username"));
                query.complete();
            } else {
                context.fail(res.cause());
            }
        });
    }

    @Test
    public void testDeleteUser(TestContext context)
    {
        final Async userExists = context.async();
        mongoClient.find(USER_COLLECTION_NAME, new JsonObject(), res -> {
            if (res.succeeded()) {
                System.out.println("Step UA");
                List<JsonObject> users = res.result();
                context.assertEquals(1, users.size());
                context.assertEquals(USER, users.get(0).getString("username"));
                userExists.complete();
            } else {
                System.out.println("Step UB");
                context.fail(res.cause());
            }
        });

        System.out.println("Step V");
        userExists.awaitSuccess();
        System.out.println("Step W");
        System.setProperty("cmd", "delete");
        System.setProperty("userName", USER);

        JsonObject config = new JsonObject().put("http", new JsonObject().put("auth", new JsonObject().put("enable", true).put("db_name", dbName).put("connection_string", "mongodb://localhost:" + mongoPort).put("salt", SALT)));
        System.out.println("Step X");
        deployUserManagerVerticle(context, vertx, config);

        System.out.println("Step Y");
        final Async query = context.async();
        mongoClient.find(USER_COLLECTION_NAME, new JsonObject(), res -> {
            if (res.succeeded()) {
                System.out.println("Step YA");
                List<JsonObject> users = res.result();
                System.out.println("Step YAA");
                context.assertEquals(0, users.size());
                System.out.println("Step YAB");
                query.complete();
                System.out.println("Step YAC");
            } else {
                System.out.println("Step YB");
                context.fail(res.cause());
            }
        });
        System.out.println("Step Z");
    }

    @After
    public void cleanup(TestContext context)
    {
        vertx.deploymentIDs().forEach(id -> {
            vertx.undeploy(id);
        });

        System.clearProperty("cmd");
        System.clearProperty("userName");
        System.clearProperty("userPassword");
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        UserManagerVerticleIT.vertx.close(context.asyncAssertSuccess());
    }
}
