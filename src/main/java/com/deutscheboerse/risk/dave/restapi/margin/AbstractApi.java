package com.deutscheboerse.risk.dave.restapi.margin;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import java.util.List;

public abstract class AbstractApi {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractApi.class);

    private final EventBus eb;
    protected final Vertx vertx;
    private final String latestEbAddress;
    private final String historyEbAddress;
    private final String requestName;

    public AbstractApi(Vertx vertx, String latestAddress, String historyAddress, String requestName) {
        this.eb = vertx.eventBus();
        this.vertx = vertx;
        this.latestEbAddress = latestAddress;
        this.historyEbAddress = historyAddress;
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

    protected void sendRequestToEventBus(RoutingContext routingContext, String ebAddress, String requestName) {
        LOG.trace("Received {} request", requestName);

        eb.send(ebAddress, this.createParamsFromContext(routingContext), ar -> {
            if (ar.succeeded()) {
                LOG.trace("Received response {} request", requestName);
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end((String)ar.result().body());
            } else {
                LOG.error("Failed to query the DB service", ar.cause());
                routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            }
        });
    }

    public void latestCall(RoutingContext routingContext) {
        sendRequestToEventBus(routingContext, latestEbAddress, requestName + "/latest");
    }

    public void historyCall(RoutingContext routingContext) {
        sendRequestToEventBus(routingContext, historyEbAddress, requestName + "/history");
    }
}
