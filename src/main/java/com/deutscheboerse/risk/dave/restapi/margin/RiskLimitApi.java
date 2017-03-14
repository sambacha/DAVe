package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.AbstractModel;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schojak on 29.8.16.
 */
public class RiskLimitApi extends AbstractApi {

    public RiskLimitApi(Vertx vertx, PersistenceService persistenceProxy) {
        super(vertx, persistenceProxy, "rl");
    }

    @Override
    protected List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add("clearer");
        parameters.add("member");
        parameters.add("maintainer");
        parameters.add("limitType");
        return parameters;
    }

    @Override
    protected void doProxyCall(AbstractModel.CollectionType type, JsonObject params, Handler<AsyncResult<String>> handler) {
        persistenceProxy.queryRiskLimit(type, params, handler);
    }

    public Router getRoutes()
    {
        Router router = Router.router(vertx);

        router.get("/latest").handler(this::latestCall);
        router.get("/latest/:clearer").handler(this::latestCall);
        router.get("/latest/:clearer/:member").handler(this::latestCall);
        router.get("/latest/:clearer/:member/:maintainer").handler(this::latestCall);
        router.get("/latest/:clearer/:member/:maintainer/:limitType").handler(this::latestCall);
        router.get("/history").handler(this::historyCall);
        router.get("/history/:clearer").handler(this::historyCall);
        router.get("/history/:clearer/:member").handler(this::historyCall);
        router.get("/history/:clearer/:member/:maintainer").handler(this::historyCall);
        router.get("/history/:clearer/:member/:maintainer/:limitType").handler(this::historyCall);

        return router;
    }
}
