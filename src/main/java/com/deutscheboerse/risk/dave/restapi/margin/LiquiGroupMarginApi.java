package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.LiquiGroupMarginModel;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import io.vertx.core.Vertx;

public class LiquiGroupMarginApi extends AbstractApi {
    public LiquiGroupMarginApi(Vertx vertx, PersistenceService persistenceProxy) {
        super(vertx, persistenceProxy, new LiquiGroupMarginModel(), "lgm");
    }
}
