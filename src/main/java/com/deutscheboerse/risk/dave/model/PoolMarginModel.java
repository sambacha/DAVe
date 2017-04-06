package com.deutscheboerse.risk.dave.model;

import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PoolMarginModel extends AbstractModel {

    public PoolMarginModel() {
        // Empty constructor
    }

    public PoolMarginModel(JsonObject json) {
        super(json);
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
