package com.opnfi.risk.restapi.user;

import com.opnfi.risk.restapi.ers.TradingSessionStatusApi;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by schojak on 29.8.16.
 */
public class UserApi {
    private static final Logger LOG = LoggerFactory.getLogger(UserApi.class);

    public UserApi() {
        // Notrhing so far
    }

    public void logoutUser(RoutingContext routingContext) {
        routingContext.clearUser();
        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end();
    }

    public void loginStatus(RoutingContext routingContext) {
        if (routingContext.user() != null) {
            JsonObject resp = new JsonObject().put("username", routingContext.user().principal().getString("username"));

            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(resp.encodePrettily());
        }
        else
        {
            //routingContext.fail(HttpResponseStatus.FORBIDDEN.code());
            // Return success to avoid triggering HTTP 401 from repeated calls
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(new JsonObject()));
        }
    }
}
