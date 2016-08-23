package com.opnfi.risk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
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
    private String ersConnectorDeployment;
    private String ersDebbugerDeployment;
    private String webInterfaceDeployment;

    @Override
    public void start(Future<Void> startFuture) {
        Future<String> chainFuture = Future.future();
        DeploymentOptions mongoDbPersistenceOptions = new DeploymentOptions().setConfig(config().getJsonObject("mongodb"));
        Future<String> mongoDbVerticleFuture = Future.future();
        vertx.deployVerticle(MongoDBPersistenceVerticle.class.getName(), mongoDbPersistenceOptions, mongoDbVerticleFuture.completer());
        mongoDbVerticleFuture.compose(v -> {
            LOG.info("Deployed MongoDBPersistenceVerticle with ID {}", v);
            mongoDbPersistenceDeployment = v;
            DeploymentOptions ersDebuggerOptions = new DeploymentOptions().setConfig(config().getJsonObject("debug"));
            Future<String> ersDebuggerVerticleFuture = Future.future();
            vertx.deployVerticle(ERSDebuggerVerticle.class.getName(), ersDebuggerOptions, ersDebuggerVerticleFuture.completer());
            return ersDebuggerVerticleFuture;
        }).compose(v -> {
            LOG.info("Deployed ERSDebuggerVerticle with ID {}", v);
            ersDebbugerDeployment = v;
            DeploymentOptions webOptions = new DeploymentOptions().setConfig(config().getJsonObject("web"));
            Future<String> webVerticleFuture = Future.future();
            vertx.deployVerticle(WebVerticle.class.getName(), webOptions, webVerticleFuture.completer());
            return webVerticleFuture;
        }).compose(v -> {
            LOG.info("Deployed WebVerticle with ID {}", v);
            webInterfaceDeployment = v;
            DeploymentOptions ersConnectorOptions = new DeploymentOptions().setConfig(config().getJsonObject("ers"));
            Future<String> ersConnectorVerticleFuture = Future.future();
            vertx.deployVerticle(ERSConnectorVerticle.class.getName(), ersConnectorOptions, ersConnectorVerticleFuture.completer());
            return ersConnectorVerticleFuture;
        }).compose(v -> {
            LOG.info("Deployed ERSConnectorVerticle with ID {}", v);
            ersConnectorDeployment = v;
            chainFuture.complete();
        }, chainFuture);

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

    private void closeAllDeployments() {
        LOG.info("Undeploying verticles");
        Map<String, String> deployments = new HashMap<>();
        deployments.put("ERSConnector", ersConnectorDeployment);
        deployments.put("ERSDebugger", ersDebbugerDeployment);
        deployments.put("WebInterface", webInterfaceDeployment);

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
