package com.deutscheboerse.risk.dave.restapi.ers;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import java.util.List;

public abstract class AbstractErsApi {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractErsApi.class);

    private final EventBus eb;
    protected final Vertx vertx;
    private final String latestEbAddress;
    private final String historyEbAddress;
    private final String requestName;

    public AbstractErsApi(Vertx vertx, String latestAddress, String historyAddress, String requestName) {
        this.eb = vertx.eventBus();
        this.vertx = vertx;
        this.latestEbAddress = latestAddress;
        this.historyEbAddress = historyAddress;
        this.requestName = requestName;
    }

    protected abstract List<String> getParameters();

    protected JsonObject createParamsFromContext(RoutingContext routingContext) {
        final JsonObject result = new JsonObject();
        if (routingContext.request().params().get("countOnly") != null) {
            result.put("countOnly", Boolean.TRUE);
        }
        if (routingContext.request().params().get("page") != null) {
            result.put("page", Integer.parseInt(routingContext.request().params().get("page")));
        }
        if (routingContext.request().params().get("pageSize") != null) {
            result.put("pageSize", Integer.parseInt(routingContext.request().params().get("pageSize")));
        }
        this.addMatchToParams(routingContext, result);
        this.addFilterToParams(routingContext, result);
        this.addSortToParams(routingContext, result);
        return result;
    }

    private void addMatchToParams(RoutingContext routingContext, JsonObject params) {
        final JsonObject match = new JsonObject();
        for (String parameterName : getParameters()) {
            if (routingContext.request().getParam(parameterName) != null && !"*".equals(routingContext.request().getParam(parameterName))) {
                match.put(parameterName, routingContext.request().getParam(parameterName));
            }
        }
        params.put("match", match);
    }

    private void addFilterToParams(RoutingContext routingContext, JsonObject params) {
        final JsonObject filter = new JsonObject();
        if (routingContext.request().params().get("filter") != null) {
            final String filterString = routingContext.request().params().get("filter").trim();
            JsonArray filterOrArray = new JsonArray();
            for (String parameterName : getParameters()) {
                filterOrArray.add(new JsonObject().put(parameterName, new JsonObject().put("$regex", String.format(".*%s.*", filterString)).put("$options", "i")));
            }
            filter.put("$or", filterOrArray);
        }
        params.put("filter", filter);
    }

    private void addSortToParams(RoutingContext routingContext, JsonObject params) {
        final JsonObject sort = new JsonObject();
        String sortColumn = "_id";
        Integer sortOrder = 1;
        if (routingContext.request().params().get("sortColumn") != null && routingContext.request().params().get("sortOrder") != null) {
            String sortColumnInContext = routingContext.request().params().get("sortColumn").trim();
            String sortOrderInContext = routingContext.request().params().get("sortOrder").trim();
            if (!sortColumnInContext.isEmpty() && !sortOrderInContext.isEmpty()) {
                sortColumn = sortColumnInContext;
                sortOrder = Integer.parseInt(sortOrderInContext);
            }
        }
        sort.put(sortColumn, sortOrder);
        params.put("sort", sort);
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
