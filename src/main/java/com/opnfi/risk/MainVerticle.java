package com.opnfi.risk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by schojak on 19.8.16.
 */
public class MainVerticle extends AbstractVerticle {
    final static private Logger LOG = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> fut) {
        vertx.deployVerticle("com.opnfi.risk.ERSConnectorVerticle", res -> {
            if (res.succeeded()) {
                LOG.info("Deployed ERSConnectorVerticle");
            } else {
                LOG.info("Deployment of ERSConnectorVerticle failed!");
            }
        });

        vertx.deployVerticle("com.opnfi.risk.ERSDebuggerVerticle", res -> {
            if (res.succeeded()) {
                LOG.info("Deployed ERSDebuggerVerticle");
            } else {
                LOG.info("Deployment of ERSDebuggerVerticle failed!");
            }
        });

        vertx.deployVerticle("com.opnfi.risk.DBPersistenceVerticle", res -> {
            if (res.succeeded()) {
                LOG.info("Deployed DBPersistenceVerticle");
            } else {
                LOG.info("Deployment of DBPersistenceVerticle failed!");
            }
        });

        vertx.deployVerticle("com.opnfi.risk.WebVerticle", res -> {
            if (res.succeeded()) {
                LOG.info("Deployed WebVerticle");
            } else {
                LOG.info("Deployment of WebVerticle failed!");
            }
        });
    }

    @Override
    public void stop() throws Exception {
        vertx.undeploy("com.opnfi.risk.ERSConnectorVerticle", res -> {
            if (res.succeeded()) {
                LOG.info("Undeployed ERSConnectorVerticle");
            } else {
                LOG.info("Undeploy of ERSConnectorVerticle failed!");
            }
        });

        vertx.undeploy("com.opnfi.risk.ERSDebuggerVerticle", res -> {
            if (res.succeeded()) {
                LOG.info("Undeployed ERSDebuggerVerticle");
            } else {
                LOG.info("Undeploy of ERSDebuggerVerticle failed!");
            }
        });

        vertx.undeploy("com.opnfi.risk.DBPersistenceVerticle", res -> {
            if (res.succeeded()) {
                LOG.info("Undeployed DBPersistenceVerticle");
            } else {
                LOG.info("Undeploy of DBPersistenceVerticle failed!");
            }
        });

        vertx.undeploy("com.opnfi.risk.WebVerticle", res -> {
            if (res.succeeded()) {
                LOG.info("Undeployed WebVerticle");
            } else {
                LOG.info("Undeploy of WebVerticle failed!");
            }
        });

    }
}
