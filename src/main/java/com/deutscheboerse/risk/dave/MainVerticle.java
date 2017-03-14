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

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);

    private Map<String, String> verticleDeployments = new HashMap<>();

    @Override
    public void start(Future<Void> startFuture) {
        Future<Void> chainFuture = Future.future();
        deployPersistenceVerticle()
                .compose(i -> deployHttpVerticle())
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

    private Future<Void> deployPersistenceVerticle() {
        return this.deployVerticle(PersistenceVerticle.class, config().getJsonObject("mongodb", new JsonObject()));
    }

    private Future<Void> deployHttpVerticle() {
        return this.deployVerticle(HttpVerticle.class, config().getJsonObject("http", new JsonObject()));
    }

    private Future<Void> deployVerticle(Class clazz, JsonObject config) {
        Future<Void> verticleFuture = Future.future();
        config.put("guice_binder", config().getString("guice_binder", Binder.class.getName()));
        DeploymentOptions options = new DeploymentOptions().setConfig(config);
        vertx.deployVerticle("java-guice:" + clazz.getName(), options, ar -> {
            if (ar.succeeded()) {
                LOG.info("Deployed {} with ID {}", clazz.getName(), ar.result());
                verticleDeployments.put(clazz.getSimpleName(), ar.result());
                verticleFuture.complete();
            } else {
                verticleFuture.fail(ar.cause());
            }
        });
        return verticleFuture;
    }

    private void closeAllDeployments() {
        LOG.info("Undeploying verticles");

        List<Future> futures = new LinkedList<>();
        this.verticleDeployments.forEach((verticleName, deploymentID) -> {
            if (deploymentID != null && vertx.deploymentIDs().contains(deploymentID)) {
                LOG.info("Undeploying {} with ID: {}", verticleName, deploymentID);
                Future<Void> future = Future.future();
                vertx.undeploy(deploymentID, future.completer());
                futures.add(future);
            }
        });

        CompositeFuture.all(futures).setHandler(ar -> {
            if (ar.succeeded()) {
                LOG.info("Undeployed all verticles");
            } else {
                LOG.error("Failed to undeploy some verticles", ar.cause());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        LOG.info("Stopping main verticle");
        this.closeAllDeployments();
        super.stop();
    }
}
