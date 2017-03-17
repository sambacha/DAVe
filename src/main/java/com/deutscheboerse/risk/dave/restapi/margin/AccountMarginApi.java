package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.AccountMarginModel;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import io.vertx.core.Vertx;

public class AccountMarginApi extends AbstractApi {
    public AccountMarginApi(Vertx vertx, PersistenceService persistenceProxy) {
        super(vertx, persistenceProxy, new AccountMarginModel(), "am");
    }
}
