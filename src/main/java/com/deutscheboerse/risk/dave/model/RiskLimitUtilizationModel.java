package com.deutscheboerse.risk.dave.model;

import com.deutscheboerse.risk.dave.grpc.RiskLimitUtilization;
import com.google.protobuf.InvalidProtocolBufferException;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class RiskLimitUtilizationModel implements Model<RiskLimitUtilization> {

    private final RiskLimitUtilization grpc;

    public RiskLimitUtilizationModel(RiskLimitUtilization grpc) {
        this.grpc = grpc;
    }

    public RiskLimitUtilizationModel(JsonObject json) {
        verifyJson(json);
        try {
            this.grpc = RiskLimitUtilization.parseFrom(json.getBinary("grpc"));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RiskLimitUtilization toGrpc() {
        return this.grpc;
    }

    @Override
    public JsonObject toApplicationJson() {
        return new JsonObject()
                .put("snapshotID", grpc.getSnapshotId())
                .put("businessDate", grpc.getBusinessDate())
                .put("timestamp", grpc.getTimestamp())
                .put("clearer", grpc.getClearer())
                .put("member", grpc.getMember())
                .put("maintainer", grpc.getMaintainer())
                .put("limitType", grpc.getLimitType())
                .put("utilization", grpc.getUtilization())
                .put("warningLevel", grpc.getWarningLevel())
                .put("throttleLevel", grpc.getThrottleLevel())
                .put("rejectLevel", grpc.getRejectLevel());
    }

    private static KeyDescriptor<RiskLimitUtilizationModel> keyDescriptor;

    public static KeyDescriptor<RiskLimitUtilizationModel> getKeyDescriptor() {
        if (keyDescriptor == null) {
            keyDescriptor = KeyDescriptor.newBuilder()
                    .addField("clearer", String.class)
                    .addField("member", String.class)
                    .addField("maintainer", String.class)
                    .addField("limitType", String.class)
                    .build();
        }
        return keyDescriptor;
    }
}
