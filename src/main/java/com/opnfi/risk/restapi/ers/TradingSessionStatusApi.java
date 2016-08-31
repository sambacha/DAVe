package com.opnfi.risk.restapi.ers;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by schojak on 29.8.16.
 */
public class TradingSessionStatusApi extends AbstractErsApi {

    public TradingSessionStatusApi(EventBus eb) {
        super(eb, "query.latestTradingSessionStatus", "query.historyTradingSessionStatus", "tss");
    }

    @Override protected JsonObject createParamsFromContext(RoutingContext routingContext) {
        return null;
    }
}
