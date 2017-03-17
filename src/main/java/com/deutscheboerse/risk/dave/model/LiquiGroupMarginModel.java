package com.deutscheboerse.risk.dave.model;

import io.vertx.core.json.JsonObject;

import java.util.*;

public class LiquiGroupMarginModel extends AbstractModel {
    private static final String HISTORY_COLLECTION = "LiquiGroupMargin";
    private static final String LATEST_COLLECTION = "LiquiGroupMargin.latest";

    public LiquiGroupMarginModel() {
        // Empty constructor
    }

    public LiquiGroupMarginModel(JsonObject json) {
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
        keys.put("account", String.class);
        keys.put("marginClass", String.class);
        keys.put("marginCurrency", String.class);
        return Collections.unmodifiableMap(keys);
    }
}
