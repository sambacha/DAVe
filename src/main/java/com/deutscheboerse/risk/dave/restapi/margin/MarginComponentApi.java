package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.AbstractModel.CollectionType;
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
public class MarginComponentApi extends AbstractApi {

    public MarginComponentApi(Vertx vertx, PersistenceService persistenceProxy) {
        super(vertx, persistenceProxy, "mc");
    }

    @Override
    protected List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add("clearer");
        parameters.add("member");
        parameters.add("account");
        parameters.add("clss");
        parameters.add("ccy");
        return parameters;
    }

    @Override
    protected void doProxyCall(CollectionType type, JsonObject params, Handler<AsyncResult<String>> handler) {
        persistenceProxy.queryMarginComponent(type, params, handler);
    }

    public Router getRoutes()
    {
        Router router = Router.router(vertx);

        router.get("/latest").handler(this::latestCall);
        router.get("/latest/:clearer").handler(this::latestCall);
        router.get("/latest/:clearer/:member").handler(this::latestCall);
        router.get("/latest/:clearer/:member/:account").handler(this::latestCall);
        router.get("/latest/:clearer/:member/:account/:clss").handler(this::latestCall);
        router.get("/latest/:clearer/:member/:account/:clss/:ccy").handler(this::latestCall);
        router.get("/history").handler(this::historyCall);
        router.get("/history/:clearer").handler(this::historyCall);
        router.get("/history/:clearer/:member").handler(this::historyCall);
        router.get("/history/:clearer/:member/:account").handler(this::historyCall);
        router.get("/history/:clearer/:member/:account/:clss").handler(this::historyCall);
        router.get("/history/:clearer/:member/:account/:clss/:ccy").handler(this::historyCall);
        
        return router;
    }
}
