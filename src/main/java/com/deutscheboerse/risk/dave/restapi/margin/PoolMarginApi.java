package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.PoolMarginModel;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import io.vertx.core.Vertx;

public class PoolMarginApi extends AbstractApi {
    public PoolMarginApi(Vertx vertx, PersistenceService persistenceProxy) {
        super(vertx, persistenceProxy, new PoolMarginModel(), "pm");
    }
}
