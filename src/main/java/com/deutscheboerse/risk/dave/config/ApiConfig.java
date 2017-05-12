package com.deutscheboerse.risk.dave.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiConfig {
    private static final int DEFAULT_PORT = 5672;
    private static final boolean DEFAULT_COMPRESSION = false;
    private final int port;
    private final SslConfig ssl;
    private final CorsConfig cors;
    private final CsrfConfig csrf;
    private final AuthConfig auth;
    private final boolean compression;

    @JsonCreator
    public ApiConfig(@JsonProperty("port") Integer port,
                      @JsonProperty("ssl") SslConfig ssl,
                      @JsonProperty("cors") CorsConfig cors,
                      @JsonProperty("csrf") CsrfConfig csrf,
                      @JsonProperty("auth") AuthConfig auth,
                      @JsonProperty("compression") Boolean compression) {
        this.port = port == null ? DEFAULT_PORT : port;
        this.ssl = ssl;
        this.cors = cors;
        this.csrf = csrf;
        this.auth = auth;
        this.compression = compression == null ? DEFAULT_COMPRESSION : compression;
    }


    public int getPort() {
        return port;
    }

    public SslConfig getSsl() {
        return ssl;
    }

    public CorsConfig getCors() {
        return cors;
    }

    public CsrfConfig getCsrf() {
        return csrf;
    }

    public AuthConfig getAuth() {
        return auth;
    }

    public boolean isCompression() {
        return compression;
    }

    public static class SslConfig {
        private static final boolean DEFAULT_ENABLE = true;
        private static final boolean DEFAULT_SSL_REQUIRE_CLIENT_AUTH = false;
        private static final String[] DEFAULT_SSL_TRUST_CERTS = new String[]{};
        private final boolean enable;
        private final String sslKey;
        private final String sslCert;
        private final boolean sslRequireClientAuth;
        private final String[] sslTrustCerts;

        @JsonCreator
        public SslConfig(@JsonProperty("enable") Boolean enable,
                         @JsonProperty("sslKey") String sslKey,
                         @JsonProperty("sslCert") String sslCert,
                         @JsonProperty("sslRequireClientAuth") Boolean sslRequireClientAuth,
                         @JsonProperty("sslTrustCerts") String[] sslTrustCerts) {
                this.enable = enable == null ? DEFAULT_ENABLE : enable;
                this.sslKey = sslKey;
                this.sslCert = sslCert;
                this.sslRequireClientAuth = sslRequireClientAuth == null ? DEFAULT_SSL_REQUIRE_CLIENT_AUTH : sslRequireClientAuth;
                this.sslTrustCerts = sslTrustCerts == null ? DEFAULT_SSL_TRUST_CERTS : sslTrustCerts;
        }

        public boolean isEnable() {
            return enable;
        }

        public String getSslKey() {
            return sslKey;
        }

        public String getSslCert() {
            return sslCert;
        }

        public boolean isSslRequireClientAuth() {
            return sslRequireClientAuth;
        }

        public String[] getSslTrustCerts() {
            return sslTrustCerts;
        }
    }

    public static class CorsConfig {
        private static final boolean DEFAULT_ENABLE = false;
        private static final String DEFAULT_ORIGIN = "*";
        private final boolean enable;
        private final String origin;

        @JsonCreator
        public CorsConfig(@JsonProperty("enable") Boolean enable,
                          @JsonProperty("origin") String origin) {
            this.enable = enable == null ? DEFAULT_ENABLE : enable;
            this.origin = origin == null ? DEFAULT_ORIGIN : origin;
        }

        public boolean isEnable() {
            return enable;
        }

        public String getOrigin() {
            return origin;
        }
    }


    public static class CsrfConfig {
        private static final boolean DEFAULT_ENABLE = false;
        private static final String DEFAULT_ORIGIN = "DAVe-CSRF-Secret";
        private final boolean enable;
        private final String secret;

        @JsonCreator
        public CsrfConfig(@JsonProperty("enable") Boolean enable,
                          @JsonProperty("secret") String secret) {
            this.enable = enable == null ? DEFAULT_ENABLE : enable;
            this.secret = secret == null ? DEFAULT_ORIGIN : secret;
        }

        public boolean isEnable() {
            return enable;
        }

        public String getSecret() {
            return secret;
        }
    }

    public static class AuthConfig {
        private static final boolean DEFAULT_ENABLE = false;
        private static final String DEFAULT_PERMISSIONS_CLAIM_KEY = "DAVe-CSRF-Secret";
        private boolean enable;
        private String jwtPublicKey;
        private String permissionsClaimKey = "realm_access/roles";

        @JsonCreator
        public AuthConfig(@JsonProperty("enable") Boolean enable,
                          @JsonProperty("jwtPublicKey") String jwtPublicKey,
                          @JsonProperty("permissionsClaimKey") String permissionsClaimKey) {
            this.enable = enable == null ? DEFAULT_ENABLE : enable;
            this.jwtPublicKey = jwtPublicKey;
            this.permissionsClaimKey = permissionsClaimKey == null ? DEFAULT_PERMISSIONS_CLAIM_KEY : permissionsClaimKey;
        }

        public boolean isEnable() {
            return enable;
        }

        public String getJwtPublicKey() {
            return jwtPublicKey;
        }

        public String getPermissionsClaimKey() {
            return permissionsClaimKey;
        }
    }
}
