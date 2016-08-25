package com.opnfi.risk.util;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.mongo.HashSaltStyle;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import java.util.Collections;

public class UserManagerVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(UserManagerVerticle.class);
    private static final String DEFAULT_DB_NAME = "OpnFi-Risk";
    private static final String DEFAULT_CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DEFAULT_USER_COLLECTION_NAME = "user";
    private static final String DEFAULT_SALT = "OpnFiRisk";
    private MongoClient mongo;

    @Override
    public void start(Future<Void> fut) throws Exception {
        LOG.info("Starting {} with configuration: {}", UserManagerVerticle.class.getSimpleName(), config().encodePrettily());
        Future<String> chainFuture = Future.future();

        Future<Void> connectDbFuture = Future.future();
        connectDb(connectDbFuture.completer());

        connectDbFuture.compose(v -> {
            LOG.info("Connected to MongoDB");
            Future<Void> initDbFuture = Future.future();
            initDb(initDbFuture.completer());
            return initDbFuture;
        }).compose(v -> {
            LOG.info("Initialized MongoDB");
            Future<String> executeCommandFuture = Future.future();
            executeCommand(executeCommandFuture.completer());
            return executeCommandFuture;
        }).compose(v -> {
            LOG.info("Command executed");
            chainFuture.complete();
        }, chainFuture);

        chainFuture.setHandler(ar -> {
            if (ar.succeeded()) {
                fut.complete();
            } else {
                LOG.error("Unable to deploy {}", UserManagerVerticle.class.getSimpleName());
                fut.fail(chainFuture.cause());
            }
            vertx.close();
        });
    }

    private void connectDb(Handler<AsyncResult<Void>> completer) {
        JsonObject config = new JsonObject();
        config.put("db_name", config().getJsonObject("web").getJsonObject("auth").getString("db_name", UserManagerVerticle.DEFAULT_DB_NAME));
        config.put("useObjectId", true);
        config.put("connection_string", config().getJsonObject("web").getJsonObject("auth").getString("connection_string", UserManagerVerticle.DEFAULT_CONNECTION_STRING));

        mongo = MongoClient.createShared(vertx, config);
        completer.handle(Future.succeededFuture());
    }

    private void initDb(Handler<AsyncResult<Void>> completer) {
        mongo.getCollections(ar -> {
            if (ar.succeeded()) {
                if (!ar.result().contains(UserManagerVerticle.DEFAULT_USER_COLLECTION_NAME)) {
                    mongo.createCollection(UserManagerVerticle.DEFAULT_USER_COLLECTION_NAME, completer);
                } else {
                    completer.handle(Future.succeededFuture());
                }
            } else {
                LOG.error("Failed to get collection list", ar.cause());
                completer.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    private void executeCommand(Handler<AsyncResult<String>> completer) {
        JsonObject authProperties = new JsonObject();
        MongoAuth authProvider = MongoAuth.create(mongo, authProperties);
        authProvider.getHashStrategy().setSaltStyle(HashSaltStyle.EXTERNAL);
        authProvider.getHashStrategy().setExternalSalt(config().getJsonObject("web").getJsonObject("auth").getString("salt", UserManagerVerticle.DEFAULT_SALT));
        String userName = System.getProperty("userName");
        String userPassword = System.getProperty("userPassword");
        if (userName == null || userName.isEmpty()) {
            completer.handle(Future.failedFuture("User name not provided"));
            return;
        }
        if (userPassword == null || userPassword.isEmpty()) {
            completer.handle(Future.failedFuture("User password not provided"));
            return;
        }
        authProvider.insertUser(userName, userPassword, Collections.emptyList(), Collections.emptyList(), completer);
    }

    @Override
    public void stop() throws Exception {
        LOG.info("{} is being stopped", UserManagerVerticle.class.getSimpleName());
        this.mongo.close();
    }

}
