package com.opnfi.risk.common;

import org.aeonbits.owner.Config;

@Config.Sources({"file:${config}"})
public interface OpnFiConfig extends Config {

    @DefaultValue("8080")
    public int httpPort();

    @DefaultValue("localhost")
    public String ersBrokerHost();

    @DefaultValue("5762")
    public int ersBrokerPort();

    @DefaultValue("alias")
    public String sslCertAlias();

    @DefaultValue("truststore")
    public String truststore();

    @DefaultValue("123456")
    public String truststorePassword();

    @DefaultValue("keystore")
    public String keystore();

    @DefaultValue("123456")
    public String keystorePassword();
}
