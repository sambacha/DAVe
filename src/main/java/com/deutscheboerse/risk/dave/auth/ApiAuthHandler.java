package com.deutscheboerse.risk.dave.auth;

import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.handler.AuthHandler;

/**
 * Created by schojak on 26.8.16.
 */

public interface ApiAuthHandler extends AuthHandler {
    static AuthHandler create(AuthProvider authProvider) {
        return new ApiAuthHandlerImpl(authProvider);
    }
}
