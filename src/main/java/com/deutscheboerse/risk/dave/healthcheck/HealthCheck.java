package com.deutscheboerse.risk.dave.healthcheck;

import com.deutscheboerse.risk.dave.MainVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;

/**
 * Created by schojak on 8.2.17.
 */
public class HealthCheck {
    private static final Logger LOG = LoggerFactory.getLogger(HealthCheck.class);

    private final static String MAP_NAME = "healthCheck";
    private final static String HTTP_KEY = "httpReady";
    private final static String MONGO_KEY = "mongoReady";

    private final Vertx vertx;
    private LocalMap<String, Boolean> healthCheck;

    public HealthCheck(Vertx vertx)
    {
        LOG.trace("Constructing {} object", HealthCheck.class.getCanonicalName());

        this.vertx = vertx;
        healthCheck = vertx.sharedData().getLocalMap(MAP_NAME);
        healthCheck.putIfAbsent(HTTP_KEY, false);
        healthCheck.putIfAbsent(MONGO_KEY, false);
    }

    public boolean ready() {
        LOG.trace("Received readiness query");

        if (getMongoState() && getHttpState())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public HealthCheck setMongoState(Boolean state)
    {
        LOG.info("Setting {} state to {}", MONGO_KEY, state);

        healthCheck.put(MONGO_KEY, state);
        return this;
    }

    public boolean getMongoState()
    {
        LOG.trace("Received state query for {}", MONGO_KEY);

        return healthCheck.get(MONGO_KEY);
    }

    public HealthCheck setHttpState(Boolean state)
    {
        LOG.info("Setting {} state to {}", HTTP_KEY, state);

        healthCheck.put(HTTP_KEY, state);
        return this;
    }

    public boolean getHttpState()
    {
        LOG.trace("Received state query for {}", HTTP_KEY);

        return healthCheck.get(HTTP_KEY);
    }
}
