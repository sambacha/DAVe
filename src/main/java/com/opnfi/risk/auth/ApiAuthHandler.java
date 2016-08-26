package com.opnfi.risk.auth;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;

/**
 * Created by schojak on 26.8.16.
 */

public interface ApiAuthHandler extends AuthHandler {
    static AuthHandler create(AuthProvider authProvider) {
        return new ApiAuthHandlerImpl(authProvider);
    }
}
