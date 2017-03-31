package com.deutscheboerse.risk.dave.restapi.margin;

import com.deutscheboerse.risk.dave.model.AbstractModel;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import com.deutscheboerse.risk.dave.persistence.RequestType;
import com.google.common.base.Preconditions;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ProxyHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public abstract class AbstractApi {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractApi.class);

    protected final Vertx vertx;
    private final AbstractModel model;
    protected final PersistenceService persistenceProxy;

    AbstractApi(Vertx vertx, AbstractModel model) {
        this.vertx = vertx;
        this.model = model;
        this.persistenceProxy = ProxyHelper.createProxy(PersistenceService.class, vertx, PersistenceService.SERVICE_ADDRESS);
    }

    protected abstract String getRequestName();
    protected abstract void proxyFind(RoutingContext routingContext, RequestType requestType);

    private String getLatestUri() {
        return String.format("/%s/%s", this.getRequestName(), "latest");
    }

    private String getHistoryUri() {
        return String.format("/%s/%s", this.getRequestName(), "history");
    }

    private void latestCall(RoutingContext routingContext) {
        LOG.trace("Received {} request", this.getLatestUri());
        try {
            this.proxyFind(routingContext, RequestType.LATEST);
        } catch (IllegalArgumentException e) {
            LOG.error("Bad request: {}", e.getMessage(), e);
            routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
    }

    private void historyCall(RoutingContext routingContext) {
        LOG.trace("Received {} request", this.getHistoryUri());
        try {
            this.proxyFind(routingContext, RequestType.HISTORY);
        } catch (IllegalArgumentException e) {
            LOG.error("Bad request: {}", e.getMessage(), e);
            routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
    }

    public Router getRoutes() {
        Router router = Router.router(vertx);

        router.get(this.getLatestUri()).handler(this::latestCall);
        router.get(this.getHistoryUri()).handler(this::historyCall);

        return router;
    }

    JsonObject createParamsFromContext(RoutingContext routingContext) {
        final JsonObject result = new JsonObject();
        routingContext.request().params().entries()
                .forEach(entry -> {
                    final String parameterName = entry.getKey();
                    final String parameterValue = entry.getValue();
                    try {
                        String decodedValue = URLDecoder.decode(parameterValue, "UTF-8");
                        Class<?> parameterType = getParameterType(parameterName);
                        result.put(parameterName, convertValue(decodedValue, parameterType));
                    } catch (UnsupportedEncodingException e) {
                        throw new AssertionError(e);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(String.format("Cannot convert '%s' (%s) to %s",
                                parameterName, parameterValue, getParameterType(parameterName).getSimpleName()));
                    }
                });
        return result;
    }

    private Class<?> getParameterType(String parameterName) {
        Preconditions.checkArgument(model.getKeysDescriptor().containsKey(parameterName),
                "Unknown parameter '%s'", parameterName);

        return model.getKeysDescriptor().get(parameterName);
    }

    private <T> T convertValue(String value, Class<T> clazz) {
        if (clazz.equals(String.class)) {
            return clazz.cast(value);
        } else if (clazz.equals(Integer.class)) {
            return clazz.cast(Integer.parseInt(value));
        } else if (clazz.equals(Double.class)) {
            return clazz.cast(Double.parseDouble(value));
        } else {
            throw new AssertionError("Unsupported type " + clazz);
        }
    }

    Handler<AsyncResult<String>> responseHandler(RoutingContext routingContext) {
        return ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response {} request", this.getRequestName());
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
