package com.deutscheboerse.risk.dave.config;

public class StoreManagerConfig {
    private String hostname = "localhost";
    private int port = 8443;
    private String sslKey = null;
    private String sslCert = null;
    private String [] sslTrustCerts = new String[] {};
    private boolean verifyHost = true;
    private RestApiConfig restApi = new RestApiConfig();
    private String guice_binder = null;

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

    public String[] getSslTrustCerts() {
        return sslTrustCerts;
    }

    public boolean isVerifyHost() {
        return verifyHost;
    }

    public RestApiConfig getRestApi() {
        return restApi;
    }

    public String getGuice_binder() {
        return guice_binder;
    }

    public static class RestApiConfig {
        private String accountMargin = "/api/v1.0/query/am";
        private String liquiGroupMargin = "/api/v1.0/query/lgm";
        private String liquiGroupSplitMargin = "/api/v1.0/query/lgsm";
        private String poolMargin = "/api/v1.0/query/pm";
        private String positionReport = "/api/v1.0/query/pr";
        private String riskLimitUtilization = "/api/v1.0/query/rlu";

        public String getAccountMargin() {
            return accountMargin;
        }

        public String getLiquiGroupMargin() {
            return liquiGroupMargin;
        }

        public String getLiquiGroupSplitMargin() {
            return liquiGroupSplitMargin;
        }

        public String getPoolMargin() {
            return poolMargin;
        }

        public String getPositionReport() {
            return positionReport;
        }

        public String getRiskLimitUtilization() {
            return riskLimitUtilization;
        }
    }
}
