package com.deutscheboerse.risk.dave.model;

import com.deutscheboerse.risk.dave.grpc.PoolMargin;
import com.google.protobuf.InvalidProtocolBufferException;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class PoolMarginModel implements Model<PoolMargin> {

    private final PoolMargin grpc;

    public PoolMarginModel(PoolMargin grpc) {
        this.grpc = grpc;
    }

    public PoolMarginModel(JsonObject json) {
        verifyJson(json);
        try {
            this.grpc = PoolMargin.parseFrom(json.getBinary("grpc"));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
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

    private static KeyDescriptor keyDescriptor;

    public static KeyDescriptor getKeyDescriptor() {
        if (keyDescriptor == null) {
            keyDescriptor = KeyDescriptor.newBuilder()
                    .addField("clearer", String.class)
                    .addField("pool", String.class)
                    .addField("marginCurrency", String.class)
                    .addField("clrRptCurrency", String.class)
                    .addField("poolOwner", String.class)
                    .build();
        }
        return keyDescriptor;
    }
}
