package com.opnfi.risk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by schojak on 19.8.16.
 */
public class MainVerticle extends AbstractVerticle {
    final static private Logger LOG = LoggerFactory.getLogger(MainVerticle.class);

    private String dbDeployment;
    private String ersConnectorDeployment;
    private String ersDebbugerDeployment;
    private String webInterfaceDeployment;

    @Override
    public void start(Future<Void> fut) {
        DeploymentOptions dbPersistenceOptions = new DeploymentOptions().setConfig(config().getJsonObject("db"));
        vertx.deployVerticle(DBPersistenceVerticle.class.getName(), dbPersistenceOptions, r1 -> {
            if (r1.succeeded()) {
                LOG.info("Deployed DBPersistenceVerticle");
                dbDeployment = r1.result();

                DeploymentOptions ersDebuggerOptions = new DeploymentOptions().setConfig(config().getJsonObject("debug"));
                vertx.deployVerticle(ERSDebuggerVerticle.class.getName(), ersDebuggerOptions, r2 -> {
                    if (r2.succeeded())
                    {
                        LOG.info("Deployed ERSDebuggerVerticle");
                        ersDebbugerDeployment = r2.result();

                        DeploymentOptions webOptions = new DeploymentOptions().setConfig(config().getJsonObject("web"));
                        vertx.deployVerticle(WebVerticle.class.getName(), webOptions, r3 -> {
                            if (r3.succeeded())
                            {
                                LOG.info("Deployed WebVerticle");
                                webInterfaceDeployment = r3.result();

                                DeploymentOptions ersConnectorOptions = new DeploymentOptions().setConfig(config().getJsonObject("ers"));
                                vertx.deployVerticle(ERSConnectorVerticle.class.getName(), ersConnectorOptions, r4 -> {
                                    if (r4.succeeded())
                                    {
                                        LOG.info("Deployed ERSConnectorVerticle");
                                        ersConnectorDeployment = r4.result();

                                        LOG.info("All verticles successfully deployed, application is ready");
                                        fut.complete();
                                    }
                                    else
                                    {
                                        LOG.error("Failed to deploy ERSConnectorVerticle verticle", r4.cause());
                                        fut.fail(r4.cause());
                                        closeAllDeployments();
                                    }
                                });
                            }
                            else
                            {
                                LOG.error("Failed to deploy WebVerticle verticle", r3.cause());
                                fut.fail(r3.cause());
                                closeAllDeployments();
                            }
                        });
                    }
                    else
                    {
                        LOG.error("Failed to deploy ERSDebuggerVerticle verticle", r2.cause());
                        fut.fail(r2.cause());
                        closeAllDeployments();
                    }
                });
            } else {
                LOG.error("Deployment of DBPersistenceVerticle failed!", r1.cause());
                fut.fail(r1.cause());
                closeAllDeployments();
            }
        });
    }

    private void closeAllDeployments()
    {
        Set<String> depIds = vertx.deploymentIDs();

        for (String id : depIds)
        {
            vertx.undeploy(id);
        }
    }


    @Override
    public void stop() throws Exception {
        LOG.info("Stopping main verticle");

        List<Future> futures = new LinkedList<>();

        if (ersConnectorDeployment != null)
        {
            LOG.info("Undeploying ERSConnector " + ersConnectorDeployment);
            Future<Void> fut1 = Future.future();
            vertx.undeploy(ersConnectorDeployment, fut1.completer());
            futures.add(fut1);
        }

        if (ersDebbugerDeployment != null)
        {
            LOG.info("Undeploying ERSDebbuger " + ersDebbugerDeployment);
            Future<Void> fut2 = Future.future();
            vertx.undeploy(ersDebbugerDeployment, fut2.completer());
            futures.add(fut2);
        }

        if (webInterfaceDeployment != null)
        {
            LOG.info("Undeploying WebInterface " + webInterfaceDeployment);
            Future<Void> fut3 = Future.future();
            vertx.undeploy(webInterfaceDeployment, fut3.completer());
            futures.add(fut3);
        }

        CompositeFuture.all(futures).setHandler(ar -> {
            if (ar.succeeded())
            {
                LOG.info("Undeployed most verticles ... ready to undeploy database");

                if (dbDeployment != null) {
                    LOG.info("Undeploying Database " + dbDeployment);
                    vertx.undeploy(dbDeployment);
                }
            }
            else
            {
                LOG.error("Failed to undeploy some verticles", ar.cause());

                if (dbDeployment != null) {
                    LOG.info("Undeploying Database " + dbDeployment);
                    vertx.undeploy(dbDeployment);
                }
            }
        });
    }
}
