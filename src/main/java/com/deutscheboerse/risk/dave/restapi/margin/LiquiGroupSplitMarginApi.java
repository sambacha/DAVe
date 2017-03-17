package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.LiquiGroupSplitMarginModel;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import io.vertx.core.Vertx;

public class LiquiGroupSplitMarginApi extends AbstractApi {
    public LiquiGroupSplitMarginApi(Vertx vertx, PersistenceService persistenceProxy) {
        super(vertx, persistenceProxy, new LiquiGroupSplitMarginModel(), "lgsm");
    }
}
