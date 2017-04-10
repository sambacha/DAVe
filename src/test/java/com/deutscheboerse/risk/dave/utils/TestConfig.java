package com.deutscheboerse.risk.dave.utils;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SelfSignedCertificate;

public class TestConfig {

    private static final int STORAGE_PORT = Integer.getInteger("storage.port", 8444);
    public static final int API_PORT = Integer.getInteger("http.port", 8443);
    public static final int HEALTHCHECK_PORT = Integer.getInteger("healthcheck.port", 8080);
    public static final SelfSignedCertificate HTTP_STORAGE_CERTIFICATE = SelfSignedCertificate.create();
    public static final SelfSignedCertificate HTTP_API_CERTIFICATE = SelfSignedCertificate.create();
    public static final SelfSignedCertificate HTTP_CLIENT_CERTIFICATE = SelfSignedCertificate.create();

    private TestConfig() {
        // Empty
    }

    public static JsonObject getGlobalConfig() {
        return new JsonObject()
                .put("http", TestConfig.getHttpConfig())
                .put("healthCheck", TestConfig.getHealthCheckConfig())
                .put("storeManager", TestConfig.getStorageConfig());
    }

    public static JsonObject getHttpConfig() {
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
                .put("CORS", new JsonObject()
                        .put("enable", false)
                        .put("origin", "*"))
                .put("CSRF", new JsonObject()
                        .put("enable", false)
                        .put("secret", "DAVe-CSRF-Secret"))
                .put("auth", new JsonObject()
                        .put("enable", false)
                        .put("jwtPublicKey", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA33TqqLR3eeUmDtHS89qF3p4MP7Wfqt2Zjj3lZjLjjCGDvwr9cJNlNDiuKboODgUiT4ZdPWbOiMAfDcDzlOxA04DDnEFGAf+kDQiNSe2ZtqC7bnIc8+KSG/qOGQIVaay4Ucr6ovDkykO5Hxn7OU7sJp9TP9H0JH8zMQA6YzijYH9LsupTerrY3U6zyihVEDXXOv08vBHk50BMFJbE9iwFwnxCsU5+UZUZYw87Uu0n4LPFS9BT8tUIvAfnRXIEWCha3KbFWmdZQZlyrFw0buUEf0YN3/Q0auBkdbDR/ES2PbgKTJdkjc/rEeM0TxvOUf7HuUNOhrtAVEN1D5uuxE1WSwIDAQAB")
                        .put("permissionsClaimKey", "realm_access/roles"))
                .put("compression", true);
    }

    private static JsonObject getHealthCheckConfig() {
        return new JsonObject()
                .put("port", HEALTHCHECK_PORT);
    }

    public static JsonObject getStorageConfig() {
        JsonArray sslTrustCerts = new JsonArray();
        HTTP_STORAGE_CERTIFICATE.trustOptions().getCertPaths().forEach(certPath -> {
            Buffer certBuffer = Vertx.vertx().fileSystem().readFileBlocking(certPath);
            sslTrustCerts.add(certBuffer.toString());
        });
        Buffer pemKeyBuffer = Vertx.vertx().fileSystem().readFileBlocking(HTTP_API_CERTIFICATE.keyCertOptions().getKeyPath());
        Buffer pemCertBuffer = Vertx.vertx().fileSystem().readFileBlocking(HTTP_API_CERTIFICATE.keyCertOptions().getCertPath());
        return new JsonObject()
                .put("port", STORAGE_PORT)
                .put("sslKey", pemKeyBuffer.toString())
                .put("sslCert", pemCertBuffer.toString())
                .put("sslRequireClientAuth", true)
                .put("sslTrustCerts", sslTrustCerts)
                .put("verifyHost", false)
                .put("restApi", new JsonObject()
                        .put("accountMargin", "/api/v1.0/query/am")
                        .put("liquiGroupMargin", "/api/v1.0/query/lgm")
                        .put("liquiGroupSplitMargin", "/api/v1.0/query/lgsm")
                        .put("poolMargin", "/api/v1.0/query/pm")
                        .put("positionReport", "/api/v1.0/query/pr")
                        .put("riskLimitUtilization", "/api/v1.0/query/rlu")
                        .put("healthz", "/healthz"));
    }
}
