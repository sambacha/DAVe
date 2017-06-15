package com.deutscheboerse.risk.dave.model;

import com.deutscheboerse.risk.dave.grpc.RiskLimitUtilization;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class RiskLimitUtilizationModel implements Model<RiskLimitUtilization> {

    public static final FieldDescriptor<RiskLimitUtilizationModel> FIELD_DESCRIPTOR = FieldDescriptor.newBuilder()
            .addField("clearer", String.class)
            .addField("member", String.class)
            .addField("maintainer", String.class)
            .addField("limitType", String.class)
            .build();

    private final RiskLimitUtilization grpc;

    public RiskLimitUtilizationModel(RiskLimitUtilization grpc) {
        this.grpc = grpc;
    }

    public RiskLimitUtilizationModel(JsonObject json) {
        verifyJson(json);
        this.grpc = json.mapTo(RiskLimitUtilization.class);
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
}
