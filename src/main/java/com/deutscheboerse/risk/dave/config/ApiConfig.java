package com.deutscheboerse.risk.dave.config;

public class ApiConfig {
    private int port = 8443;
    private SslConfig ssl = new SslConfig();
    private CorsConfig cors = new CorsConfig();
    private CsrfConfig csrf = new CsrfConfig();
    private AuthConfig auth = new AuthConfig();
    private boolean compression = false;

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
        private boolean enable = true;
        private String sslKey = "";
        private String sslCert = "";
        private boolean sslRequireClientAuth = false;
        private String sslTrustCerts[] = new String[] {};

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
        private boolean enable = false;
        private String origin = "*";

        public boolean isEnable() {
            return enable;
        }

        public String getOrigin() {
            return origin;
        }
    }


    public static class CsrfConfig {
        private boolean enable = false;
        private String secret = "DAVe-CSRF-Secret";

        public boolean isEnable() {
            return enable;
        }

        public String getSecret() {
            return secret;
        }
    }

    public static class AuthConfig {
        private boolean enable = false;
        private String jwtPublicKey = null;
        private String permissionsClaimKey = "realm_access/roles";

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
