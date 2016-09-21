package com.deutscheboerse.risk.dave;

import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.*;

/**
 * Created by schojak on 19.8.16.
 */
public class MainVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);

    private String mongoDbPersistenceDeployment;
    private String ersDebbugerDeployment;
    private List<String> ersConnectorDeployment = new ArrayList<>();
    private String httpDeployment;
    private String masterdataDeployment;

    @Override
    public void start(Future<Void> startFuture) {
        Future<Void> chainFuture = Future.future();
        deployMongoDBVerticle()
                .compose(this::deployMasterData)
                .compose(this::deployERSDebuggerVerticle)
                .compose(this::deployHttpVerticle)
                .compose(this::deployErsVerticles)
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

    private Future<Void> deployERSDebuggerVerticle(Void unused) {
        Future<Void> ersDebuggerVerticleFuture = Future.future();
        if (config().getJsonObject("ersDebugger", new JsonObject()).getBoolean("enable", false)) {
            DeploymentOptions ersDebuggerOptions = new DeploymentOptions().setConfig(config().getJsonObject("ersDebugger"));
            vertx.deployVerticle(ERSDebuggerVerticle.class.getName(), ersDebuggerOptions, ar -> {
                if (ar.succeeded()) {
                    LOG.info("Deployed ERSDebuggerVerticle with ID {}", ar.result());
                    ersDebbugerDeployment = ar.result();
                    ersDebuggerVerticleFuture.complete();
                } else {
                    ersDebuggerVerticleFuture.fail(ar.cause());
                }
            });
        } else {
            ersDebuggerVerticleFuture.complete();
        }
        return ersDebuggerVerticleFuture;
    }

    private Future<Void> deployHttpVerticle(Void unused) {
        Future<Void> httpVerticleFuture = Future.future();
        DeploymentOptions webOptions = new DeploymentOptions().setConfig(config().getJsonObject("http"));
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

    private Future<Void> deployErsVerticles(Void unused)
    {
        Future<Void> ersConnectorDeploymentFuture = Future.future();
        List<Future> tasks = new ArrayList<>();

        JsonArray ersConnectorOptions = config().getJsonArray("ers");
        ersConnectorOptions.forEach( connectorOptions -> {
            LOG.info("Deploying ERS connector verticle {}", (JsonObject)connectorOptions);

            DeploymentOptions ersDeploymentOptions = new DeploymentOptions().setConfig((JsonObject)connectorOptions);
            Future<String> fut = Future.future();

            vertx.deployVerticle(ERSConnectorVerticle.class.getName(), ersDeploymentOptions, res -> {
                if (res.succeeded()) {
                    LOG.info("Deployed ERSConnectorVerticle with ID {}", res.result());
                    ersConnectorDeployment.add(res.result());
                    fut.complete();
                } else {
                    LOG.error("Failed to deployed ERSConnectorVerticle", res.cause());
                    fut.fail(res.cause());
                }
            });
            tasks.add(fut);
        });

        CompositeFuture.all(tasks).setHandler(ar -> {
            if (ar.succeeded()) {
                LOG.info("Deployed all ERS verticles");
                ersConnectorDeploymentFuture.complete();
            } else {
                LOG.error("Failed to deploy some or all ERS verticles");
                ersConnectorDeploymentFuture.fail(ar.cause());
            }
        });
        return ersConnectorDeploymentFuture;
    }

    private Future<Void> deployMasterData(Void unused) {
        Future<Void> masterdataVerticleFuture = Future.future();
        DeploymentOptions masterdataOptions = new DeploymentOptions().setConfig(config().getJsonObject("masterdata", new JsonObject()));
        vertx.deployVerticle(MasterdataVerticle.class.getName(), masterdataOptions, ar -> {
            if (ar.succeeded()) {
                LOG.info("Deployed MasterdataVerticle with ID {}", ar.result());
                masterdataDeployment = ar.result();
                masterdataVerticleFuture.complete();
            } else {
                masterdataVerticleFuture.fail(ar.cause());
            }
        });
        return masterdataVerticleFuture;
    }

    private void closeAllDeployments() {
        LOG.info("Undeploying verticles");
        Map<String, String> deployments = new HashMap<>();
        ersConnectorDeployment.forEach(id -> {
            deployments.put("ERSConnector", id);
        });
        deployments.put("ERSDebugger", ersDebbugerDeployment);
        deployments.put("HttpInterface", httpDeployment);
        deployments.put("Masterdata", masterdataDeployment);

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
