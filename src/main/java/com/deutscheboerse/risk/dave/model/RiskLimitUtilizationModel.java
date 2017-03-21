package com.deutscheboerse.risk.dave.model;

import io.vertx.core.json.JsonObject;

import java.util.*;

public class RiskLimitUtilizationModel extends AbstractModel {

    public RiskLimitUtilizationModel() {
        // Empty constructor
    }

    public RiskLimitUtilizationModel(JsonObject json) {
        super(json);
    }

    @Override
    public Map<String, Class<?>> getKeysDescriptor() {
        Map<String, Class<?>> keys = new LinkedHashMap<>();
        keys.put("clearer", String.class);
        keys.put("member", String.class);
        keys.put("maintainer", String.class);
        keys.put("limitType", String.class);
        return Collections.unmodifiableMap(keys);
    }
}
