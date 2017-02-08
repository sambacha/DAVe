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
    private LocalMap healthCheck;

    public HealthCheck(Vertx vertx)
    {
        LOG.trace("Constructing {} object", HealthCheck.class.getCanonicalName());

        this.vertx = vertx;
        healthCheck = vertx.sharedData().getLocalMap(MAP_NAME);
    }

    public HealthCheck initialize() {
        LOG.info("Initializing {}", HealthCheck.class.getCanonicalName());

        if (healthCheck.get(HTTP_KEY) == null) {
            LOG.trace("Initializing {} to false", HTTP_KEY);
            healthCheck.put(HTTP_KEY, false);
        }

        if (healthCheck.get(MONGO_KEY) == null) {
            LOG.trace("Initializing {} to false", MONGO_KEY);
            healthCheck.put(MONGO_KEY, false);
        }

        return this;
    }

    public Boolean ready() {
        LOG.trace("Received readyness query");

        if ((Boolean) healthCheck.get(MONGO_KEY) && (Boolean) healthCheck.get(HTTP_KEY))
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
        LOG.info("Setting {} readyness to {}", MONGO_KEY, state);

        healthCheck.put(MONGO_KEY, state);
        return this;
    }

    public Boolean getMongoState()
    {
        LOG.trace("Received readyness query for {}", MONGO_KEY);

        return (Boolean) healthCheck.get(MONGO_KEY);
    }

    public HealthCheck setHttpState(Boolean state)
    {
        LOG.info("Setting {} readyness to {}", HTTP_KEY, state);

        healthCheck.put(HTTP_KEY, state);
        return this;
    }

    public Boolean getHttpState()
    {
        LOG.trace("Received readyness query for {}", HTTP_KEY);

        return (Boolean) healthCheck.get(HTTP_KEY);
    }
}
