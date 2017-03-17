package com.deutscheboerse.risk.dave.model;

import io.vertx.core.json.JsonObject;

import java.util.*;

public class PoolMarginModel extends AbstractModel {
    private static final String HISTORY_COLLECTION = "PoolMargin";
    private static final String LATEST_COLLECTION = "PoolMargin.latest";

    public PoolMarginModel() {
        // Empty constructor
    }

    public PoolMarginModel(JsonObject json) {
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
        keys.put("pool", String.class);
        keys.put("marginCurrency", String.class);
        return Collections.unmodifiableMap(keys);
    }
}
