package com.deutscheboerse.risk.dave.auth;

import com.auth0.jwk.GuavaCachedJwkProvider;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.deutscheboerse.risk.dave.config.ApiConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.auth.jwt.impl.JWTUser;
import io.vertx.ext.jwt.JWT;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class JWKSAuthProviderImpl implements JWTAuth {
    private static final Logger LOG = LoggerFactory.getLogger(JWKSAuthProviderImpl.class);

    private static final String PROXY_HOST = System.getProperty("http.proxyHost", "none");
    private static final int PROXY_PORT = Integer.getInteger("http.proxyPort", 8080);

    private final Cache<String, JWT> cache;
    private final String permissionsClaimKey;
    private final String clientId;
    private Optional<JwkProvider> jwkProvider = Optional.empty();
    private String issuer;

    public JWKSAuthProviderImpl(Vertx vertx, ApiConfig.AuthConfig config) {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(5)
                .expireAfterWrite(10, TimeUnit.HOURS)
                .build();

        this.permissionsClaimKey = "permissions";
        this.clientId = config.getClientId();
        this.loadWellKnownFields(vertx, config.getWellKnownUrl());
    }

    private void loadWellKnownFields(Vertx vertx, String wellKnownUrl) {
        WebClientOptions options = new WebClientOptions();
        if (!"none".equalsIgnoreCase(PROXY_HOST)) {
            options.setProxyOptions(new ProxyOptions()
                    .setType(ProxyType.HTTP)
                    .setHost(PROXY_HOST)
                    .setPort(PROXY_PORT));
        }
        WebClient.create(vertx, options)
                .getAbs(wellKnownUrl)
                .ssl(wellKnownUrl.startsWith("https://") ? true : false)
                .send(ar -> {
                    if (ar.succeeded()) {
                        JsonObject response = ar.result().bodyAsJsonObject();
                        this.parseAndStoreWellKnownFields(Optional.ofNullable(response));
                    } else {
                        LOG.error("Unable to retrieve well known fields from {}", wellKnownUrl, ar.cause());
                    }
                });
    }

    private void parseAndStoreWellKnownFields(Optional<JsonObject> wellKnownFields) {
        this.issuer = wellKnownFields.orElseGet(JsonObject::new).getString("issuer", "");
        String jwksUri = wellKnownFields.orElseGet(JsonObject::new).getString("jwks_uri", "");
        try {
            JwkProvider urlJwkProvider = new UrlJwkProvider(new URL(jwksUri));
            this.jwkProvider = Optional.of(new GuavaCachedJwkProvider(urlJwkProvider));
            LOG.info("Initializing Jwks Provider with jwks_uri: {}, issuer: {}", jwksUri, this.issuer);
        } catch (MalformedURLException e) {
            LOG.error("Unable to create Jwk Provider on '{}'", jwksUri, e);
        }
    }

    private String getKid(String token) {
        String[] segments = token.split("\\.");
        if (segments.length != 3) {
            throw new RuntimeException("Not enough or too many segments");
        }

        // All segment should be base64
        String headerSeg = segments[0];

        // base64 decode and parse JSON
        byte[] decodedHeaderBytes = Base64.getUrlDecoder().decode(headerSeg.getBytes(StandardCharsets.UTF_8));
        JsonObject decodedHeader = new JsonObject(new String(decodedHeaderBytes, StandardCharsets.UTF_8));
        return decodedHeader.getString("kid", "");
    }

    private JWT getJwt(String jwtToken) throws ExecutionException {
        String kid = this.getKid(jwtToken);
        LOG.debug("Retrieved kid '{}' from token", kid);
        return this.cache.get(kid, () -> {
            Jwk jwk = this.jwkProvider.orElseThrow(() -> new RuntimeException("JwkProvider not constructed")).get(kid);
            byte[] encodedPublicKey = jwk.getPublicKey().getEncoded();
            String b64PublicKey = Base64.getEncoder().encodeToString(encodedPublicKey);
                LOG.info("Retrieved jwk key: {}", jwk.toString());
                return new JWT(b64PublicKey, false);
            }
        );
    }

    @Override
    public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
        try {
            String jwtToken = authInfo.getString("jwt");
            final JsonObject payload = this.getJwt(jwtToken).decode(jwtToken);

            final JsonObject options = authInfo.getJsonObject("options", new JsonObject());

            // All dates in JWT are of type NumericDate
            // a NumericDate is: numeric value representing the number of seconds from 1970-01-01T00:00:00Z UTC until
            // the specified UTC date/time, ignoring leap seconds
            final long now = System.currentTimeMillis() / 1000;

            if (payload.containsKey("exp") && !options.getBoolean("ignoreExpiration", false)) {
                if (now >= payload.getLong("exp")) {
                    resultHandler.handle(Future.failedFuture("Expired JWT token: exp <= now"));
                    return;
                }
            }

            JsonArray target;
            if (payload.getValue("aud") instanceof String) {
                target = new JsonArray().add(payload.getValue("aud", ""));
            } else {
                target = payload.getJsonArray("aud", new JsonArray());
                if(!payload.getString("azp", "").equals(this.clientId)) {
                    resultHandler.handle(Future.failedFuture("Invalid JWT audient. expected: " + this.clientId));
                    return;
                }
            }

            if (!target.getList().contains(this.clientId)) {
                resultHandler.handle(Future.failedFuture("Invalid JWT audient. expected: " + this.clientId));
                return;
            }

            if (!this.issuer.equals(payload.getString("iss"))) {
                resultHandler.handle(Future.failedFuture("Invalid JWT issuer"));
                return;
            }

            resultHandler.handle(Future.succeededFuture(new JWTUser(payload, permissionsClaimKey)));

        } catch (RuntimeException | ExecutionException e) {
            resultHandler.handle(Future.failedFuture(e));
        }
    }

    @Override
    public String generateToken(JsonObject claims, JWTOptions options) {
        throw new UnsupportedOperationException("Cannot generate token");
    }
}
