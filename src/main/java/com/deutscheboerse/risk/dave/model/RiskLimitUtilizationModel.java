package com.deutscheboerse.risk.dave.model;

import io.vertx.core.json.JsonObject;

import java.util.*;

public class RiskLimitUtilizationModel extends AbstractModel {
    private static final String HISTORY_COLLECTION = "RiskLimitUtilization";
    private static final String LATEST_COLLECTION = "RiskLimitUtilization.latest";

    public RiskLimitUtilizationModel() {
        // Empty constructor
    }

    public RiskLimitUtilizationModel(JsonObject json) {
        super(json);
    }

    @Override
    public String getLatestCollection() {
        return LATEST_COLLECTION;
    }

    @Override
    public String getHistoryCollection() {
        return HISTORY_COLLECTION;
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
