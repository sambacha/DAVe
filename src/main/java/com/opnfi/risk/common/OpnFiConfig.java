package com.opnfi.risk.common;

import io.vertx.core.json.JsonObject;

public class OpnFiConfig {

    private final JsonObject config;

    public OpnFiConfig(JsonObject config) {
        this.config = config;
    }

    public int httpPort() {
        return this.config.getJsonObject("web").getInteger("httpPort", 8080);
    };

    public String ersBrokerHost() {
        return this.config.getJsonObject("ers").getString("brokerHost", "localhost");
    };

    public int ersBrokerPort() {
        return this.config.getJsonObject("ers").getInteger("brokerPort", 5672);
    };

    public String sslCertAlias() {
        return this.config.getJsonObject("ers").getString("sslCertAlias", "alias");
    };

    public String truststore() {
        return this.config.getJsonObject("ers").getString("truststore", "truststore");
    };

    public String truststorePassword() {
        return this.config.getJsonObject("ers").getString("truststorePassword", "123456");
    };

    public String keystore() {
        return this.config.getJsonObject("ers").getString("keystore", "keystore");
    };

    public String keystorePassword() {
        return this.config.getJsonObject("ers").getString("keystorePassword", "123456");
    };
}
