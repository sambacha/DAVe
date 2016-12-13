package com.deutscheboerse.risk.dave;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
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

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@RunWith(VertxUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserManagerVerticleIT {
    private static final String USER_COLLECTION_NAME = "user";
    private static final String SALT = "DAVe";
    private static final String USER = "user1";
    private static final String PASSWORD = "123456";

    private static Vertx vertx;
    private static MongoClient mongoClient;
    private static int mongoPort;
    private static String dbName;
    private static Appender<ILoggingEvent> testAppender;

    @BeforeClass
    public static void setUp(TestContext context) {
        UserManagerVerticleIT.vertx = Vertx.vertx();
        UserManagerVerticleIT.mongoPort = Integer.getInteger("mongodb.port", 27017);
        UserManagerVerticleIT.dbName = "DAVe-Test" + UUID.randomUUID().getLeastSignificantBits();

        JsonObject dbConfig = new JsonObject();
        dbConfig.put("db_name", dbName);
        dbConfig.put("useObjectId", true);
        dbConfig.put("connection_string", "mongodb://localhost:" + mongoPort);

        UserManagerVerticleIT.mongoClient = MongoClient.createShared(UserManagerVerticleIT.vertx, dbConfig);

        testAppender = new TestAppender<>();
        testAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        Logger logger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(testAppender);
        logger.setLevel(Level.INFO);
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
    public void test1InsertUser(TestContext context)
    {
        System.setProperty("cmd", "insert");
        System.setProperty("userName", USER);
        System.setProperty("userPassword", PASSWORD);

        JsonObject config = new JsonObject().put("http", new JsonObject().put("auth", new JsonObject().put("enable", true).put("jwtKeystorePath", getClass().getResource("jwt-keystore.jceks").getPath()).put("jwtKeystorePassword", "secret").put("jwtKeystoreType", "jceks").put("db_name", dbName).put("connection_string", "mongodb://localhost:" + mongoPort).put("salt", SALT)));
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
    public void test2ListUser(TestContext context) throws InterruptedException {
        final Async userExists = context.async();
        mongoClient.find(USER_COLLECTION_NAME, new JsonObject(), res -> {
            if (res.succeeded()) {
                List<JsonObject> users = res.result();
                context.assertEquals(1, users.size());
                context.assertEquals(USER, users.get(0).getString("username"));
                userExists.complete();
            } else {
                context.fail(res.cause());
            }
        });

        userExists.awaitSuccess();

        testAppender.start();
        System.setProperty("cmd", "list");
        JsonObject config = new JsonObject().put("http", new JsonObject().put("auth", new JsonObject().put("enable", true).put("jwtKeystorePath", getClass().getResource("jwt-keystore.jceks").getPath()).put("jwtKeystorePassword", "secret").put("jwtKeystoreType", "jceks").put("db_name", dbName).put("connection_string", "mongodb://localhost:" + mongoPort).put("salt", SALT)));
        deployUserManagerVerticle(context, vertx, config);
        testAppender.stop();

        ILoggingEvent msg = ((TestAppender)testAppender).findMessage("User records stored in the database:");
        context.assertNotNull(msg);
        context.assertEquals(Level.INFO, msg.getLevel());
        msg = ((TestAppender)testAppender).findMessage("\"username\" : \"" + USER + "\",");
        context.assertNotNull(msg);
        context.assertEquals(Level.INFO, msg.getLevel());
    }

    @Test
    public void test3DeleteUser(TestContext context)
    {
        final Async userExists = context.async();
        mongoClient.find(USER_COLLECTION_NAME, new JsonObject(), res -> {
            if (res.succeeded()) {
                List<JsonObject> users = res.result();
                context.assertEquals(1, users.size());
                context.assertEquals(USER, users.get(0).getString("username"));
                userExists.complete();
            } else {
                context.fail(res.cause());
            }
        });

        userExists.awaitSuccess();

        System.setProperty("cmd", "delete");
        System.setProperty("userName", USER);

        JsonObject config = new JsonObject().put("http", new JsonObject().put("auth", new JsonObject().put("enable", true).put("jwtKeystorePath", getClass().getResource("jwt-keystore.jceks").getPath()).put("jwtKeystorePassword", "secret").put("jwtKeystoreType", "jceks").put("db_name", dbName).put("connection_string", "mongodb://localhost:" + mongoPort).put("salt", SALT)));
        deployUserManagerVerticle(context, vertx, config);

        final Async query = context.async();
        mongoClient.find(USER_COLLECTION_NAME, new JsonObject(), res -> {
            if (res.succeeded()) {
                List<JsonObject> users = res.result();
                context.assertEquals(0, users.size());
                query.complete();
            } else {
                context.fail(res.cause());
            }
        });
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

    static class TestAppender<E> extends UnsynchronizedAppenderBase<E>
    {
        private String name = "TestLogger";
        private List<ILoggingEvent> messages = new LinkedList<>();

        @Override
        protected void append(Object o) {
            if (o instanceof ILoggingEvent) {
                ILoggingEvent event = (ILoggingEvent)o;

                if (event.getLoggerName().equals(UserManagerVerticle.class.getName())) {
                    System.out.println("Received: " + event.getFormattedMessage());
                    messages.add(event);
                }
            }
        }

        public ILoggingEvent findMessage(String s) {
            for (ILoggingEvent event : messages)
            {
                System.out.println("Analyzing: " + event.getFormattedMessage().replace("\n", ""));
                if (event.getFormattedMessage().replace("\n", "").contains(s))
                {
                    return event;
                }
            }

            return null;
        }

        @Override
        public void start() {
            messages.clear();
            super.start();
        }

        @Override
        public void stop() {
            super.stop();
        }
    }
}
