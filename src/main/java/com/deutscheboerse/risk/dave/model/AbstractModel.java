package com.deutscheboerse.risk.dave.model;

import io.vertx.core.json.JsonObject;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractModel extends JsonObject {

    AbstractModel() {
    }

    AbstractModel(JsonObject json) {
        this.mergeIn(json);
    }

    public abstract Map<String, Class<?>> getKeysDescriptor();

    public Collection<String> getKeys() {
        return getKeysDescriptor().keySet();
    }
}
