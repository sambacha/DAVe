package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.RiskLimitUtilizationModel;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import io.vertx.core.Vertx;

public class RiskLimitUtilizationApi extends AbstractApi {
    public RiskLimitUtilizationApi(Vertx vertx, PersistenceService persistenceProxy) {
        super(vertx, persistenceProxy, new RiskLimitUtilizationModel(), "rlu");
    }
}
