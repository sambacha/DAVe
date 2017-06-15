package com.deutscheboerse.risk.dave.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HealthCheckConfig {
    private static final int DEFAULT_PORT = 8080;
    private final int port;

    @JsonCreator
    public HealthCheckConfig(@JsonProperty("port") Integer port) {
        this.port = port == null ? DEFAULT_PORT : port;
    }

    public int getPort() {
        return port;
    }
}
