package com.deutscheboerse.risk.dave.utils;

import io.vertx.core.json.JsonObject;

public class TestConfig {

    private static final int STORAGE_PORT = Integer.getInteger("storage.port", 8084);
    public static final int HTTP_PORT = Integer.getInteger("http.port", 8083);

    private TestConfig() {
        // Empty
    }

    public static JsonObject getGlobalConfig() {
        return new JsonObject()
                .put("http", TestConfig.getHttpConfig())
                .put("storeManager", TestConfig.getStorageConfig());
    }

    private static JsonObject getHttpConfig() {
        return new JsonObject()
                .put("port", HTTP_PORT);
    }

    public static JsonObject getStorageConfig() {
        return new JsonObject()
                .put("port", STORAGE_PORT)
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
