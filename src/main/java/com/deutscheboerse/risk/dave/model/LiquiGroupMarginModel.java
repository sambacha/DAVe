package com.deutscheboerse.risk.dave.model;

import io.vertx.core.json.JsonObject;

import java.util.*;

public class LiquiGroupMarginModel extends AbstractModel {

    public LiquiGroupMarginModel() {
        // Empty constructor
    }

    public LiquiGroupMarginModel(JsonObject json) {
        super(json);
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
