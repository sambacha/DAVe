package com.opnfi.risk.common;

import org.aeonbits.owner.Config;

@Config.Sources({"file:${config}"})
public interface OpnFiConfig extends Config {

    @DefaultValue("8080")
    public int httpPort();

}
