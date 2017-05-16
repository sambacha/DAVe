package com.deutscheboerse.risk.dave.restapi;

import com.deutscheboerse.risk.dave.model.KeyDescriptor;
import com.deutscheboerse.risk.dave.model.Model;
import com.deutscheboerse.risk.dave.persistence.RequestType;
import com.google.common.base.Preconditions;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

public class MarginApi<T extends Model> {
    private static final Logger LOG = LoggerFactory.getLogger(MarginApi.class);

    private final Vertx vertx;
    private String requestName;
    private KeyDescriptor keyDescriptor;
    private ProxyFunction<T> proxyFunction;

    private MarginApi(Vertx vertx) {
        this.vertx = vertx;
    }

    public Router getRoutes() {
        Router router = Router.router(vertx);

        router.get(this.getLatestUri()).handler(this::latestCall);
        router.get(this.getHistoryUri()).handler(this::historyCall);

        return router;
    }

    private String getLatestUri() {
        return String.format("/%s/%s", this.requestName, "latest");
    }

    private String getHistoryUri() {
        return String.format("/%s/%s", this.requestName, "history");
    }

    private void latestCall(RoutingContext routingContext) {
        LOG.trace("Received {} request", this.getLatestUri());
        this.doCall(routingContext, RequestType.LATEST);
    }

    private void historyCall(RoutingContext routingContext) {
        LOG.trace("Received {} request", this.getHistoryUri());
        this.doCall(routingContext, RequestType.HISTORY);
    }

    private void doCall(RoutingContext routingContext, RequestType requestType) {
        try {
            this.proxyFunction.query(requestType, this.createParamsFromContext(routingContext), responseHandler(routingContext));
        } catch (IllegalArgumentException e) {
            LOG.error("Bad request: {}", e.getMessage(), e);
            routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
    }

    private JsonObject createParamsFromContext(RoutingContext routingContext) {
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
        Map<String, Class<?>> parameterDescriptor = keyDescriptor.getUniqueFields();

        Preconditions.checkArgument(parameterDescriptor.containsKey(parameterName),
                "Unknown parameter '%s'", parameterName);
        return parameterDescriptor.get(parameterName);
    }

    private <U> U convertValue(String value, Class<U> clazz) {
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

    private Handler<AsyncResult<List<T>>> responseHandler(RoutingContext routingContext) {
        return ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response {} request", this.requestName);
                JsonArray result = new JsonArray();
                ar.result().forEach(model -> result.add(model.toApplicationJson()));
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(result.toString());
            } else {
                LOG.error("Failed to query the DB service", ar.cause());
                routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            }
        };
    }

    public static MarginApiBuilder newBuilder(Vertx vertx) {
        return new MarginApiBuilder(vertx);
    }

    @FunctionalInterface
    public interface ProxyFunction<T> {
        void query(RequestType type, JsonObject query, Handler<AsyncResult<List<T>>> resultHandler);
    }

    static final class MarginApiBuilder {
        private Vertx vertx;
        private String requestName;
        private KeyDescriptor keyDescriptor;
        private ProxyFunction proxyFunction;

        private MarginApiBuilder(Vertx vertx) {
            this.vertx = vertx;
        }

        MarginApiBuilder setRequestName(String requestName) {
            this.requestName = requestName;
            return this;
        }

        MarginApiBuilder setKeyDescriptor(KeyDescriptor keyDescriptor) {
            this.keyDescriptor = keyDescriptor;
            return this;
        }

        MarginApiBuilder setProxyFunction(ProxyFunction proxyFunction) {
            this.proxyFunction = proxyFunction;
            return this;
        }

        MarginApi build() {
            MarginApi result = new MarginApi(this.vertx);
            result.requestName = this.requestName;
            result.keyDescriptor = this.keyDescriptor;
            result.proxyFunction = this.proxyFunction;
            return result;
        }
    }
}
