package com.opnfi.risk;

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
        Future<String> chainFuture = Future.future();
        DeploymentOptions mongoDbPersistenceOptions = new DeploymentOptions().setConfig(config().getJsonObject("mongodb"));
        Future<String> mongoDbVerticleFuture = Future.future();
        vertx.deployVerticle(MongoDBPersistenceVerticle.class.getName(), mongoDbPersistenceOptions, mongoDbVerticleFuture.completer());
        mongoDbVerticleFuture.compose(v -> {
            LOG.info("Deployed MongoDBPersistenceVerticle with ID {}", v);
            mongoDbPersistenceDeployment = v;

            Future<String> ersDebuggerVerticleFuture = Future.future();

            if (config().getJsonObject("ersDebugger", new JsonObject()).getBoolean("enable", false)) {
                DeploymentOptions ersDebuggerOptions = new DeploymentOptions().setConfig(config().getJsonObject("ersDebugger"));
                vertx.deployVerticle(ERSDebuggerVerticle.class.getName(), ersDebuggerOptions, ersDebuggerVerticleFuture.completer());
            }
            else
            {
                ersDebuggerVerticleFuture.complete();
            }

            return ersDebuggerVerticleFuture;
        }).compose(v -> {
            if (config().getJsonObject("ersDebugger", new JsonObject()).getBoolean("enable", false)) {
                LOG.info("Deployed ERSDebuggerVerticle with ID {}", v);
                ersDebbugerDeployment = v;
            }

            DeploymentOptions webOptions = new DeploymentOptions().setConfig(config().getJsonObject("http"));
            Future<String> httpVerticleFuture = Future.future();
            vertx.deployVerticle(HttpVerticle.class.getName(), webOptions, httpVerticleFuture.completer());
            return httpVerticleFuture;
        }).compose(v -> {
            LOG.info("Deployed HttpVerticle with ID {}", v);
            httpDeployment = v;

            JsonArray ersConnectorOptions = config().getJsonArray("ers");
            Future<Void> ersConnectorDeploymentFuture = Future.future();
            deployErsVerticles(ersConnectorOptions, ersConnectorDeploymentFuture.completer());
            return ersConnectorDeploymentFuture;
        }).compose(v -> {
            DeploymentOptions masterdataOptions = new DeploymentOptions().setConfig(config().getJsonObject("masterdata", new JsonObject()));
            Future<String> masterdataVerticleFuture = Future.future();
            vertx.deployVerticle(MasterdataVerticle.class.getName(), masterdataOptions, masterdataVerticleFuture.completer());
            return masterdataVerticleFuture;
        }).compose(v -> {
            LOG.info("Deployed MasterdataVerticle with ID {}", v);
            masterdataDeployment = v;

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

    private void deployErsVerticles(JsonArray ersOptions, Handler<AsyncResult<Void>> handler)
    {
        List<Future> tasks = new ArrayList<>();

        ersOptions.forEach( connectorOptions -> {
            LOG.info("Deploying ERS connector verticle {}", (JsonObject)connectorOptions);

            DeploymentOptions ersDeploymentOptions = new DeploymentOptions().setConfig((JsonObject)connectorOptions);
            Future<String> fut = Future.future();

            vertx.deployVerticle(ERSConnectorVerticle.class.getName(), ersDeploymentOptions, res -> {
                if (res.succeeded())
                {
                    LOG.info("Deployed ERSConnectorVerticle with ID {}", res.result());
                    ersConnectorDeployment.add(res.result());
                    fut.complete();
                }
                else
                {
                    LOG.error("Failed to deployed ERSConnectorVerticle", res.cause());
                    fut.fail(res.cause());
                }
            });
            tasks.add(fut);
        });

        CompositeFuture.all(tasks).setHandler(res -> {
            if (res.succeeded())
            {
                LOG.info("Deployed all ERS verticles");
                handler.handle(Future.succeededFuture());
            }
            else
            {
                LOG.error("Failed to deploy some or all ERS verticles");
                handler.handle(Future.failedFuture(res.cause()));
            }
        });
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
