package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.AbstractModel;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.Collections;
import java.util.List;

/**
 * Created by schojak on 29.8.16.
 */
public class TradingSessionStatusApi extends AbstractApi {
    public TradingSessionStatusApi(Vertx vertx, PersistenceService persistenceProxy) {
        super(vertx, persistenceProxy, "tss");
    }

    @Override
    protected List<String> getParameters() {
        return Collections.emptyList();
    }

    @Override
    protected void doProxyCall(AbstractModel.CollectionType type, JsonObject params, Handler<AsyncResult<String>> handler) {
        persistenceProxy.queryTradingSessionStatus(type, params, handler);
    }

    public Router getRoutes()
    {
        Router router = Router.router(vertx);
        router.get("/latest").handler(this::latestCall);
        router.get("/history").handler(this::historyCall);

        return router;
    }
}
