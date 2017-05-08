package com.deutscheboerse.risk.dave.model;

import io.vertx.core.json.JsonObject;

import java.util.*;

public class AccountMarginModel extends AbstractModel {

    public AccountMarginModel() {
        // Empty constructor
    }

    public AccountMarginModel(JsonObject json) {
        super(json);
    }

    @Override
    public Map<String, Class<?>> getKeysDescriptor() {
        Map<String, Class<?>> keys = new LinkedHashMap<>();
        keys.put("clearer", String.class);
        keys.put("member", String.class);
        keys.put("account", String.class);
        keys.put("marginCurrency", String.class);
        return Collections.unmodifiableMap(keys);
    }

    @Override
    public Map<String, Class<?>> getUniqueFieldsDescriptor() {
        Map<String, Class<?>> uniqueFields = new LinkedHashMap<>();
        uniqueFields.put("clearingCurrency", String.class);
        uniqueFields.put("pool", String.class);
        return Collections.unmodifiableMap(uniqueFields);
    }
}
