package com.deutscheboerse.risk.dave.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.List;

@JsonIgnoreProperties({"guice_binder"})
public class StoreManagerConfig {
    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT= 8443;
    private static final String[] DEFAULT_SSL_TRUST_CERTS = new String[]{};
    private final String hostname;
    private final int port;
    private final String sslKey;
    private final String sslCert;
    private final String[] sslTrustCerts;

    @JsonCreator
    public StoreManagerConfig(@JsonProperty("hostname") String hostname,
                              @JsonProperty("port") Integer port,
                              @JsonProperty("sslKey") String sslKey,
                              @JsonProperty("sslCert") String sslCert,
                              @JsonProperty("sslTrustCerts") String[] sslTrustCerts) {
        this.hostname = hostname == null ? DEFAULT_HOSTNAME : hostname;
        this.port = port == null ? DEFAULT_PORT : port;
        this.sslKey = sslKey;
        this.sslCert = sslCert;
        this.sslTrustCerts = sslTrustCerts == null ? DEFAULT_SSL_TRUST_CERTS : sslTrustCerts;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getSslKey() {
        return sslKey;
    }

    public String getSslCert() {
        return sslCert;
    }

    public List<String> getSslTrustCerts() {
        return ImmutableList.copyOf(sslTrustCerts);
    }
}
