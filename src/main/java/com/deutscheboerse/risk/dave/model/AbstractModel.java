package com.deutscheboerse.risk.dave.model;

import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
