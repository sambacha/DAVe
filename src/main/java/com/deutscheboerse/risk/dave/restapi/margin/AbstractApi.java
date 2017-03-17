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
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

public abstract class AbstractApi {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractApi.class);

    private static final String LATEST_URI = "/latest";
    private static final String HISTORY_URI = "/history";

    protected final Vertx vertx;
    private final String requestName;
    private final PersistenceService persistenceProxy;
    private final AbstractModel model;

    public AbstractApi(Vertx vertx, PersistenceService persistenceProxy, AbstractModel model, String requestName) {
        this.vertx = vertx;
        this.persistenceProxy = persistenceProxy;
        this.model = model;
        this.requestName = requestName;
    }

    private void latestCall(RoutingContext routingContext) {
        LOG.trace("Received {} request", requestName + LATEST_URI);
        persistenceProxy.find(model.getLatestCollection(), this.createParamsFromContext(routingContext),
                responseHandler(routingContext, requestName + LATEST_URI));
    }

    private void historyCall(RoutingContext routingContext) {
        LOG.trace("Received {} request", requestName + HISTORY_URI);
        persistenceProxy.find(model.getHistoryCollection(), this.createParamsFromContext(routingContext),
                responseHandler(routingContext, requestName + HISTORY_URI));
    }

    public Router getRoutes() {
        Router router = Router.router(vertx);

        StringBuilder latestUrl = new StringBuilder(LATEST_URI);
        StringBuilder historyUrl = new StringBuilder(HISTORY_URI);

        router.get(latestUrl.toString()).handler(this::latestCall);
        router.get(historyUrl.toString()).handler(this::historyCall);

        for (String key: model.getKeys()) {
            latestUrl.append("/:").append(key);
            historyUrl.append("/:").append(key);
            router.get(latestUrl.toString()).handler(this::latestCall);
            router.get(historyUrl.toString()).handler(this::historyCall);
        }

        return router;
    }

    private JsonObject createParamsFromContext(RoutingContext routingContext) {
        final JsonObject result = new JsonObject();
        for (Map.Entry<String, Class<?>> entry : model.getKeysDescriptor().entrySet()) {
            String parameterName = entry.getKey();
            String parameterValue = routingContext.request().getParam(parameterName);
            if (parameterValue != null && !"*" .equals(parameterValue)) {
                try {
                    parameterValue = URLDecoder.decode(parameterValue, "UTF-8");
                    Class<?> convertTo = entry.getValue();
                    result.put(parameterName, convertValue(parameterValue, convertTo));
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError(e);
                }
            }
        }
        return result;
    }

    private <T> T convertValue(String value, Class<T> clazz) {
        if (clazz == String.class) {
            return clazz.cast(value);
        } else if (clazz == Integer.class) {
            return clazz.cast(Integer.parseInt(value));
        } else if (clazz == Double.class) {
            return clazz.cast(Double.parseDouble(value));
        } else {
            throw new AssertionError("Unsupported type " + clazz);
        }
    }

    private Handler<AsyncResult<String>> responseHandler(RoutingContext routingContext, String requestName) {
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
}
