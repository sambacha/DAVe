package com.deutscheboerse.risk.dave;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.deutscheboerse.risk.dave.log.TestAppender;
import com.deutscheboerse.risk.dave.util.UserManagerVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@RunWith(VertxUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserManagerVerticleIT {
    private static final TestAppender testAppender = TestAppender.getAppender(UserManagerVerticle.class);
    private static final Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    private static final String USER_COLLECTION_NAME = "user";
    private static final String SALT = "DAVe";
    private static final String USER = "user1";
    private static final String PASSWORD = "123456";

    private static Vertx vertx;
    private static MongoClient mongoClient;
    private static int mongoPort;
    private static String dbName;

    @BeforeClass
    public static void setUp() {
        UserManagerVerticleIT.vertx = Vertx.vertx();
        UserManagerVerticleIT.mongoPort = Integer.getInteger("mongodb.port", 27017);
        UserManagerVerticleIT.dbName = "DAVe-Test" + UUID.randomUUID().getLeastSignificantBits();

        JsonObject dbConfig = new JsonObject();
        dbConfig.put("db_name", dbName);
        dbConfig.put("useObjectId", true);
        dbConfig.put("connection_string", "mongodb://localhost:" + mongoPort);

        UserManagerVerticleIT.mongoClient = MongoClient.createShared(UserManagerVerticleIT.vertx, dbConfig);

        rootLogger.addAppender(testAppender);
        rootLogger.setLevel(Level.INFO);
    }

    private void deployUserManagerVerticle(TestContext context, Vertx vertx, JsonObject config)
    {
        final Async asyncStart = context.async();

        vertx.deployVerticle(UserManagerVerticle.class.getName(), new DeploymentOptions().setConfig(config),
                context.asyncAssertSuccess(res -> asyncStart.complete()));

        asyncStart.awaitSuccess();
    }

    @Test
    public void test1InsertUser(TestContext context)
    {
        System.setProperty("cmd", "insert");
        System.setProperty("userName", USER);
        System.setProperty("userPassword", PASSWORD);

        JsonObject config = new JsonObject().put("http", new JsonObject().put("auth", new JsonObject().put("dbName", dbName).put("connectionUrl", "mongodb://localhost:" + mongoPort).put("salt", SALT)));
        deployUserManagerVerticle(context, vertx, config);

        mongoClient.find(USER_COLLECTION_NAME, new JsonObject(), context.asyncAssertSuccess(users -> {
            context.assertEquals(1, users.size());
            context.assertEquals(USER, users.get(0).getString("username"));
        }));
    }

    @Test
    public void test2ListUser(TestContext context) throws InterruptedException {
        final Async userExists = context.async();
        mongoClient.find(USER_COLLECTION_NAME, new JsonObject(), context.asyncAssertSuccess(users -> {
            context.assertEquals(1, users.size());
            context.assertEquals(USER, users.get(0).getString("username"));
            userExists.complete();
        }));

        userExists.awaitSuccess();

        testAppender.start();
        System.setProperty("cmd", "list");
        JsonObject config = new JsonObject().put("http", new JsonObject().put("auth", new JsonObject().put("dbName", dbName).put("connectionUrl", "mongodb://localhost:" + mongoPort).put("salt", SALT)));
        deployUserManagerVerticle(context, vertx, config);
        testAppender.stop();

        ILoggingEvent msg = testAppender.findMessage(Level.INFO, "User records stored in the database:").orElse(null);
        context.assertNotNull(msg);
        msg = testAppender.findMessage(Level.INFO, "\"username\" : \"" + USER + "\",").orElse(null);
        context.assertNotNull(msg);
    }

    @Test
    public void test3DeleteUser(TestContext context)
    {
        final Async userExists = context.async();
        mongoClient.find(USER_COLLECTION_NAME, new JsonObject(), context.asyncAssertSuccess(users -> {
            context.assertEquals(1, users.size());
            context.assertEquals(USER, users.get(0).getString("username"));
            userExists.complete();
        }));

        userExists.awaitSuccess();

        System.setProperty("cmd", "delete");
        System.setProperty("userName", USER);

        JsonObject config = new JsonObject().put("http", new JsonObject().put("auth", new JsonObject().put("dbName", dbName).put("connectionUrl", "mongodb://localhost:" + mongoPort).put("salt", SALT)));
        deployUserManagerVerticle(context, vertx, config);

        mongoClient.find(USER_COLLECTION_NAME, new JsonObject(), context.asyncAssertSuccess(users ->
            context.assertEquals(0, users.size())
        ));
    }

    @After
    public void cleanup()
    {
        vertx.deploymentIDs().forEach(id -> vertx.undeploy(id));

        System.clearProperty("cmd");
        System.clearProperty("userName");
        System.clearProperty("userPassword");

        testAppender.clear();
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        UserManagerVerticleIT.vertx.close(context.asyncAssertSuccess());
    }
}
