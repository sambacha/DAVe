package com.deutscheboerse.risk.dave.model;

import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;

public abstract class AbstractModel extends JsonObject {

    AbstractModel() {
    }

    AbstractModel(JsonObject json) {
        this.mergeIn(json);
    }

    public abstract String getLatestCollection();

    public abstract String getHistoryCollection();

    public abstract Map<String, Class<?>> getKeysDescriptor();

    public Collection<String> getKeys() {
        return getKeysDescriptor().keySet();
    }

    public JsonObject getQueryParams() {
        JsonObject queryParams = new JsonObject();
        this.getKeys().forEach(key -> queryParams.put(key, this.getValue(key)));
        return queryParams;
    }

    public JsonObject getMongoDocument() {
        return this.copy()
                .put("timestamp", new JsonObject().put("$date", this.milliToIsoDateTime(this.getLong("timestamp"))));
    }

    private String milliToIsoDateTime(long milli) {
        Instant instant = Instant.ofEpochMilli(milli);
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
