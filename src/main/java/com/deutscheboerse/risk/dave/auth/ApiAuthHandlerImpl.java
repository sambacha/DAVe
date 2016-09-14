package com.deutscheboerse.risk.dave.auth;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;

/**
 * Created by schojak on 26.8.16.
 */
public class ApiAuthHandlerImpl extends AuthHandlerImpl {
    private static final Logger LOG = LoggerFactory.getLogger(ApiAuthHandlerImpl.class);

    public ApiAuthHandlerImpl(AuthProvider authProvider) {
        super (authProvider);
    }

    @Override
    public void handle(RoutingContext context) {
        Session session = context.session();
        if (session != null) {
            User user = context.user();
            if (user != null) {
                // Already logged in, just authorise
                authorise(user, context);
            } else {
                LOG.error("Unauthorized access to url {} from {}", context.request().uri(), context.request().remoteAddress().toString());
                context.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
            }
        } else {
            LOG.error("Unauthorized access to url {} from {}", context.request().uri(), context.request().remoteAddress().toString());
            context.fail(new NullPointerException("No session - did you forget to include a SessionHandler?"));
        }

    }
}
