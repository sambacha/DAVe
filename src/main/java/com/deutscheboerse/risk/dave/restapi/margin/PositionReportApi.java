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

public class PositionReportApi extends AbstractApi {

    public PositionReportApi(Vertx vertx, PersistenceService persistenceProxy) {
        super(vertx, persistenceProxy, "pr");
    }

    @Override
    protected List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add("clearer");
        parameters.add("member");
        parameters.add("account");
        parameters.add("clss");
        parameters.add("symbol");
        parameters.add("putCall");
        parameters.add("strikePrice");
        parameters.add("optAttribute");
        parameters.add("maturityMonthYear");
        return parameters;
    }

    @Override
    protected void doProxyCall(AbstractModel.CollectionType type, JsonObject params, Handler<AsyncResult<String>> handler) {
        persistenceProxy.queryPositionReport(type, params, handler);
    }

    public Router getRoutes()
    {
        Router router = Router.router(vertx);

        router.get("/latest").handler(this::latestCall);
        router.get("/latest/:clearer").handler(this::latestCall);
        router.get("/latest/:clearer/:member").handler(this::latestCall);
        router.get("/latest/:clearer/:member/:account").handler(this::latestCall);
        router.get("/latest/:clearer/:member/:account/:clss").handler(this::latestCall);
        router.get("/latest/:clearer/:member/:account/:clss/:symbol").handler(this::latestCall);
        router.get("/latest/:clearer/:member/:account/:clss/:symbol/:putCall").handler(this::latestCall);
        router.get("/latest/:clearer/:member/:account/:clss/:symbol/:putCall/:strikePrice").handler(this::latestCall);
        router.get("/latest/:clearer/:member/:account/:clss/:symbol/:putCall/:strikePrice/:optAttribute").handler(this::latestCall);
        router.get("/latest/:clearer/:member/:account/:clss/:symbol/:putCall/:strikePrice/:optAttribute/:maturityMonthYear").handler(this::latestCall);
        router.get("/history").handler(this::historyCall);
        router.get("/history/:clearer").handler(this::historyCall);
        router.get("/history/:clearer/:member").handler(this::historyCall);
        router.get("/history/:clearer/:member/:account").handler(this::historyCall);
        router.get("/history/:clearer/:member/:account/:clss").handler(this::historyCall);
        router.get("/history/:clearer/:member/:account/:clss/:symbol").handler(this::historyCall);
        router.get("/history/:clearer/:member/:account/:clss/:symbol/:putCall").handler(this::historyCall);
        router.get("/history/:clearer/:member/:account/:clss/:symbol/:putCall/:strikePrice").handler(this::historyCall);
        router.get("/history/:clearer/:member/:account/:clss/:symbol/:putCall/:strikePrice/:optAttribute").handler(this::historyCall);
        router.get("/history/:clearer/:member/:account/:clss/:symbol/:putCall/:strikePrice/:optAttribute/:maturityMonthYear").handler(this::historyCall);

        return router;
    }
}
