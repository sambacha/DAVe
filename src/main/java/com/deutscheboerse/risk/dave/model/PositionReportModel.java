package com.deutscheboerse.risk.dave.model;

import io.vertx.core.json.JsonObject;

import java.util.*;

public class PositionReportModel extends AbstractModel {
    private static final String HISTORY_COLLECTION = "PositionReport";
    private static final String LATEST_COLLECTION = "PositionReport.latest";

    public PositionReportModel() {
        // Empty constructor
    }

    public PositionReportModel(JsonObject json) {
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
        HashMap<String, Class<?>> keys = new LinkedHashMap<>();
        keys.put("clearer", String.class);
        keys.put("member", String.class);
        keys.put("account", String.class);
        keys.put("liquidationGroup", String.class);
        keys.put("liquidationGroupSplit", String.class);
        keys.put("product", String.class);
        keys.put("callPut", String.class);
        keys.put("contractYear", Integer.class);
        keys.put("contractMonth", Integer.class);
        keys.put("expiryDay", Integer.class);
        keys.put("exercisePrice", Double.class);
        keys.put("version", String.class);
        keys.put("flexContractSymbol", String.class);
        return Collections.unmodifiableMap(keys);
    }
}
