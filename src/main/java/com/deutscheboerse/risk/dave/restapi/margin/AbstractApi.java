package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.AbstractModel;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

import static com.deutscheboerse.risk.dave.model.AbstractModel.CollectionType.HISTORY;
import static com.deutscheboerse.risk.dave.model.AbstractModel.CollectionType.LATEST;

public abstract class AbstractApi {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractApi.class);

    protected final Vertx vertx;
    private final String requestName;
    protected final PersistenceService persistenceProxy;

    public AbstractApi(Vertx vertx, PersistenceService persistenceProxy, String requestName) {
        this.vertx = vertx;
        this.persistenceProxy = persistenceProxy;
        this.requestName = requestName;
    }

    protected abstract List<String> getParameters();

    protected JsonObject createParamsFromContext(RoutingContext routingContext) {
        final JsonObject result = new JsonObject();
        for (String parameterName : getParameters()) {
            if (routingContext.request().getParam(parameterName) != null && !"*".equals(routingContext.request().getParam(parameterName))) {
                result.put(parameterName, routingContext.request().getParam(parameterName));
            }
        }
        return result;
    }

    protected Handler<AsyncResult<String>> responseHandler(RoutingContext routingContext, String requestName) {
        return ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response {} request", requestName);
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(ar.result());
            } else {
                LOG.error("Failed to query the DB service", ar.cause());
                routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            }
        };
    }

    public void latestCall(RoutingContext routingContext) {
        LOG.trace("Received {} request", requestName + "/latest");
        doProxyCall(LATEST, this.createParamsFromContext(routingContext),
                responseHandler(routingContext, requestName + "/latest"));
    }

    public void historyCall(RoutingContext routingContext) {
        LOG.trace("Received {} request", requestName + "/history");
        doProxyCall(HISTORY, this.createParamsFromContext(routingContext),
                responseHandler(routingContext, requestName + "/history"));
    }

    protected abstract void doProxyCall(AbstractModel.CollectionType type, JsonObject params, Handler<AsyncResult<String>> handler);
}
