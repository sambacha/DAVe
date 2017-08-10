package com.deutscheboerse.risk.dave.utils;

import com.deutscheboerse.risk.dave.MainVerticleTest;
import com.deutscheboerse.risk.dave.auth.JWKSAuthProviderImpl;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SelfSignedCertificate;

import java.nio.file.Paths;

public class TestConfig {

    public static final int STORE_MANAGER_PORT = Integer.getInteger("storage.port", 8444);
    public static final int API_PORT = Integer.getInteger("api.port", 8443);
    public static final int HEALTHCHECK_PORT = Integer.getInteger("healthcheck.port", 8080);
    public static final SelfSignedCertificate HTTP_STORAGE_CERTIFICATE = SelfSignedCertificate.create("localhost");
    public static final SelfSignedCertificate HTTP_API_CERTIFICATE = SelfSignedCertificate.create("localhost");
    public static final SelfSignedCertificate HTTP_CLIENT_CERTIFICATE = SelfSignedCertificate.create("localhost");
    public static final String WELL_KNOWN_FILE_PATH = Paths.get(JWKSAuthProviderImpl.class.getResource(".").getPath(), "well_known").toString();

    private TestConfig() {
        // Empty
    }

    public static JsonObject getGlobalConfig() {
        return new JsonObject()
                .put("api", TestConfig.getApiConfig())
                .put("healthCheck", TestConfig.getHealthCheckConfig())
                .put("storeManager", TestConfig.getStoreManagerConfig());
    }

    public static JsonObject getApiConfig() {
        JsonArray sslTrustCerts = new JsonArray();
        HTTP_CLIENT_CERTIFICATE.trustOptions().getCertPaths().forEach(certPath -> {
            Buffer certBuffer = Vertx.vertx().fileSystem().readFileBlocking(certPath);
            sslTrustCerts.add(certBuffer.toString());
        });
        Buffer pemKeyBuffer = Vertx.vertx().fileSystem().readFileBlocking(HTTP_API_CERTIFICATE.keyCertOptions().getKeyPath());
        Buffer pemCertBuffer = Vertx.vertx().fileSystem().readFileBlocking(HTTP_API_CERTIFICATE.keyCertOptions().getCertPath());
        return new JsonObject()
                .put("port", API_PORT)
                .put("ssl", new JsonObject()
                        .put("enable", true)
                        .put("sslKey", pemKeyBuffer.toString())
                        .put("sslCert", pemCertBuffer.toString())
                        .put("sslRequireClientAuth", false)
                        .put("sslTrustCerts", sslTrustCerts))
                .put("cors", new JsonObject()
                        .put("enable", false)
                        .put("origin", "*"))
                .put("csrf", new JsonObject()
                        .put("enable", false)
                        .put("secret", "DAVe-CSRF-Secret"))
                .put("auth", new JsonObject()
                        .put("enable", false)
                        .put("clientId", "dave-ui")
                        .put("wellKnownUrl", WELL_KNOWN_FILE_PATH))
                .put("compression", true);
    }

    private static JsonObject getHealthCheckConfig() {
        return new JsonObject()
                .put("port", HEALTHCHECK_PORT);
    }

    public static JsonObject getStoreManagerConfig() {
        JsonArray sslTrustCerts = new JsonArray();
        HTTP_STORAGE_CERTIFICATE.trustOptions().getCertPaths().forEach(certPath -> {
            Buffer certBuffer = Vertx.vertx().fileSystem().readFileBlocking(certPath);
            sslTrustCerts.add(certBuffer.toString());
        });
        Buffer pemKeyBuffer = Vertx.vertx().fileSystem().readFileBlocking(HTTP_API_CERTIFICATE.keyCertOptions().getKeyPath());
        Buffer pemCertBuffer = Vertx.vertx().fileSystem().readFileBlocking(HTTP_API_CERTIFICATE.keyCertOptions().getCertPath());
        return new JsonObject()
                .put("port", STORE_MANAGER_PORT)
                .put("sslKey", pemKeyBuffer.toString())
                .put("sslCert", pemCertBuffer.toString())
                .put("sslTrustCerts", sslTrustCerts);
    }
}
