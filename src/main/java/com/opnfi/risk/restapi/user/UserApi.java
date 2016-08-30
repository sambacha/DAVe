package com.opnfi.risk.restapi.user;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by schojak on 29.8.16.
 */
public class UserApi {
    private static final Logger LOG = LoggerFactory.getLogger(UserApi.class);
    private final AuthProvider authProvider;

    public UserApi(AuthProvider ap) {
        this.authProvider = ap;
    }

    public void login(RoutingContext routingContext) {
        if (authProvider != null) {
            try {
                JsonObject record = routingContext.getBodyAsJson();
                String username = record.getString("username");
                String password = record.getString("password");
                if (username == null || password == null) {
                    LOG.warn("No username or password provided in login request");
                    routingContext.fail(HttpResponseStatus.BAD_REQUEST.code());
                } else {
                    JsonObject authInfo = new JsonObject().put("username", username).put("password", password);
                    authProvider.authenticate(authInfo, res -> {
                        if (res.succeeded()) {
                            User user = res.result();

                            JsonObject resp = new JsonObject().put("username", user.principal().getString("username"));

                            routingContext.setUser(user);
                            routingContext.response()
                                    .putHeader("content-type", "application/json; charset=utf-8")
                                    .end(Json.encodePrettily(resp));
                            LOG.info("User {} authenticated successfully", username);
                        } else {
                            routingContext.fail(HttpResponseStatus.FORBIDDEN.code());
                            LOG.warn("User {} failed to authenticate!", username);
                        }
                    });
                }
            } catch (DecodeException e) {
                routingContext.fail(HttpResponseStatus.BAD_REQUEST.code());
            }
        }
        else
        {
            JsonObject resp = new JsonObject().put("username", "Annonymous");
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(resp));
        }
    }

    public void logout(RoutingContext routingContext) {
        if (authProvider != null) {
            routingContext.clearUser();
        }
        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end();
    }

    public void loginStatus(RoutingContext routingContext) {
        JsonObject response = new JsonObject();
        if (authProvider != null) {
            if (routingContext.user() != null) {
                response.put("username", routingContext.user().principal().getString("username"));
            }
        } else {
            response.put("username", "Annonymous");
        }
        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(response.encodePrettily());
    }
}
