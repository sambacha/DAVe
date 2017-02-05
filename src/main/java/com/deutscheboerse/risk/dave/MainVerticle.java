package com.deutscheboerse.risk.dave;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by schojak on 19.8.16.
 */
public class MainVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);

    private String mongoDbPersistenceDeployment;
    private String httpDeployment;

    @Override
    public void start(Future<Void> startFuture) {
        Future<Void> chainFuture = Future.future();
        deployMongoDBVerticle()
                .compose(this::deployHttpsVerticle)
                .compose(this::deployHttpVerticle)
                .compose(chainFuture::complete, chainFuture);

        chainFuture.setHandler(ar -> {
            if (ar.succeeded()) {
                LOG.info("All verticles deployed");
                startFuture.complete();
            } else {
                LOG.error("Fail to deploy some verticle");
                closeAllDeployments();
                startFuture.fail(chainFuture.cause());
            }
        });
    }

    private Future<Void> deployMongoDBVerticle() {
        Future<Void> mongoDbVerticleFuture = Future.future();
        DeploymentOptions mongoDbPersistenceOptions = new DeploymentOptions().setConfig(config().getJsonObject("mongodb"));
        vertx.deployVerticle(MongoDBPersistenceVerticle.class.getName(), mongoDbPersistenceOptions, ar -> {
            if (ar.succeeded()) {
                LOG.info("Deployed MongoDBPersistenceVerticle with ID {}", ar.result());
                mongoDbPersistenceDeployment = ar.result();
                mongoDbVerticleFuture.complete();
            } else {
                mongoDbVerticleFuture.fail(ar.cause());
            }
        });
        return mongoDbVerticleFuture;
    }

    private Future<Void> deployHttpsVerticle(Void unused) {
        DeploymentOptions webOptions = new DeploymentOptions().setConfig(config().getJsonObject("http"));
        if (!webOptions.getConfig().getJsonObject("ssl", new JsonObject()).getBoolean("enable", false)) {
            return Future.succeededFuture();
        }
        Future<Void> httpVerticleFuture = Future.future();
        webOptions.getConfig().put("mode", HttpVerticle.Mode.HTTPS);
        vertx.deployVerticle(HttpVerticle.class.getName(), webOptions, ar -> {
            if (ar.succeeded()) {
                LOG.info("Deployed HttpsVerticle with ID {}", ar.result());
                httpDeployment = ar.result();
                httpVerticleFuture.complete();
            } else  {
                httpVerticleFuture.fail(ar.cause());
            }
        });
        return httpVerticleFuture;
    }

    private Future<Void> deployHttpVerticle(Void unused) {
        Future<Void> httpVerticleFuture = Future.future();
        DeploymentOptions webOptions = new DeploymentOptions().setConfig(config().getJsonObject("http"));
        if (webOptions.getConfig().getJsonObject("ssl", new JsonObject()).getBoolean("enable", false) && webOptions.getConfig().getJsonObject("ssl", new JsonObject()).getBoolean("redirectHttp", true)) {
            webOptions.getConfig().put("mode", HttpVerticle.Mode.HTTP_REDIRECT);
        } else {
            webOptions.getConfig().put("mode", HttpVerticle.Mode.HTTP);
        }
        vertx.deployVerticle(HttpVerticle.class.getName(), webOptions, ar -> {
            if (ar.succeeded()) {
                LOG.info("Deployed HttpVerticle with ID {}", ar.result());
                httpDeployment = ar.result();
                httpVerticleFuture.complete();
            } else  {
                httpVerticleFuture.fail(ar.cause());
            }
        });
        return httpVerticleFuture;
    }

    private void closeAllDeployments() {
        LOG.info("Undeploying verticles");
        Map<String, String> deployments = new HashMap<>();
        deployments.put("HttpInterface", httpDeployment);

        List<Future> futures = new LinkedList<>();
        deployments.forEach((verticleName, deploymentID) -> {
            if (deploymentID != null && vertx.deploymentIDs().contains(deploymentID)) {
                LOG.info("Undeploying {} with ID: {}", verticleName, deploymentID);
                Future<Void> future = Future.future();
                vertx.undeploy(deploymentID, future.completer());
                futures.add(future);
            }
        });

        CompositeFuture.all(futures).setHandler(ar -> {
            if (ar.succeeded()) {
                LOG.info("Undeployed most verticles ... ready to undeploy database");
            } else {
                LOG.error("Failed to undeploy some verticles", ar.cause());
            }
            if (mongoDbPersistenceDeployment != null) {
                LOG.info("Undeploying Database " + mongoDbPersistenceDeployment);
                vertx.undeploy(mongoDbPersistenceDeployment);
            }
        });
    }

    @Override
    public void stop() throws Exception {
        LOG.info("Stopping main verticle");
        this.closeAllDeployments();
    }
}
