package com.deutscheboerse.risk.dave.restapi.ers;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.Router;

import java.util.Collections;
import java.util.List;

/**
 * Created by schojak on 29.8.16.
 */
public class TradingSessionStatusApi extends AbstractErsApi {
    public TradingSessionStatusApi(Vertx vertx) {
        super(vertx, "query.latestTradingSessionStatus", "query.historyTradingSessionStatus", "tss");
    }

    @Override
    protected List<String> getParameters() {
        return Collections.emptyList();
    }

    public Router getRoutes()
    {
        Router router = Router.router(vertx);
        router.get("/latest").handler(this::latestCall);
        router.get("/history").handler(this::historyCall);

        return router;
    }
}
