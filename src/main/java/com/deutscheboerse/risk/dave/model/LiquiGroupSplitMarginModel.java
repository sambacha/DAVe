package com.deutscheboerse.risk.dave.model;

import io.vertx.core.json.JsonObject;

import java.util.*;

public class LiquiGroupSplitMarginModel extends AbstractModel {

    public LiquiGroupSplitMarginModel() {
        // Empty constructor
    }

    public LiquiGroupSplitMarginModel(JsonObject json) {
        super(json);
    }

    @Override
    public Map<String, Class<?>> getKeysDescriptor() {
        Map<String, Class<?>> keys = new LinkedHashMap<>();
        keys.put("clearer", String.class);
        keys.put("member", String.class);
        keys.put("account", String.class);
        keys.put("liquidationGroup", String.class);
        keys.put("liquidationGroupSplit", String.class);
        keys.put("marginCurrency", String.class);
        return Collections.unmodifiableMap(keys);
    }
}
