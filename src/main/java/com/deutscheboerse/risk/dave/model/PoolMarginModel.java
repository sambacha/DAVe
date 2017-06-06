package com.deutscheboerse.risk.dave.model;

import com.deutscheboerse.risk.dave.grpc.PoolMargin;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class PoolMarginModel implements Model<PoolMargin> {

    public static final FieldDescriptor<PoolMarginModel> FIELD_DESCRIPTOR = FieldDescriptor.newBuilder()
            .addField("clearer", String.class)
            .addField("pool", String.class)
            .addField("marginCurrency", String.class)
            .addField("clrRptCurrency", String.class)
            .addField("poolOwner", String.class)
            .build();

    private final PoolMargin grpc;

    public PoolMarginModel(PoolMargin grpc) {
        this.grpc = grpc;
    }

    public PoolMarginModel(JsonObject json) {
        verifyJson(json);
        this.grpc = json.mapTo(PoolMargin.class);
    }

    @Override
    public PoolMargin toGrpc() {
        return this.grpc;
    }

    @Override
    public JsonObject toApplicationJson() {
        return new JsonObject()
                .put("snapshotID", grpc.getSnapshotId())
                .put("businessDate", grpc.getBusinessDate())
                .put("timestamp", grpc.getTimestamp())
                .put("clearer", grpc.getClearer())
                .put("pool", grpc.getPool())
                .put("marginCurrency", grpc.getMarginCurrency())
                .put("clrRptCurrency", grpc.getClrRptCurrency())
                .put("requiredMargin", grpc.getRequiredMargin())
                .put("cashCollateralAmount", grpc.getCashCollateralAmount())
                .put("adjustedSecurities", grpc.getAdjustedSecurities())
                .put("adjustedGuarantee", grpc.getAdjustedGuarantee())
                .put("overUnderInMarginCurr", grpc.getOverUnderInMarginCurr())
                .put("overUnderInClrRptCurr", grpc.getOverUnderInClrRptCurr())
                .put("variPremInMarginCurr", grpc.getVariPremInMarginCurr())
                .put("adjustedExchangeRate", grpc.getAdjustedExchangeRate())
                .put("poolOwner", grpc.getPoolOwner());
    }
}
