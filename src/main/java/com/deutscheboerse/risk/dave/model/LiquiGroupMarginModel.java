package com.deutscheboerse.risk.dave.model;

import com.deutscheboerse.risk.dave.grpc.LiquiGroupMargin;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class LiquiGroupMarginModel implements Model<LiquiGroupMargin> {

    public static final FieldDescriptor<LiquiGroupMarginModel> FIELD_DESCRIPTOR = FieldDescriptor.newBuilder()
            .addField("clearer", String.class)
            .addField("member", String.class)
            .addField("account", String.class)
            .addField("marginClass", String.class)
            .addField("marginCurrency", String.class)
            .addField("marginGroup", String.class)
            .build();

    private final LiquiGroupMargin grpc;

    public LiquiGroupMarginModel(LiquiGroupMargin grpc) {
        this.grpc = grpc;
    }

    public LiquiGroupMarginModel(JsonObject json) {
        verifyJson(json);
        this.grpc = json.mapTo(LiquiGroupMargin.class);
    }

    @Override
    public LiquiGroupMargin toGrpc() {
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
                .put("account", grpc.getAccount())
                .put("marginClass", grpc.getMarginClass())
                .put("marginCurrency", grpc.getMarginCurrency())
                .put("marginGroup", grpc.getMarginGroup())
                .put("premiumMargin", grpc.getPremiumMargin())
                .put("currentLiquidatingMargin", grpc.getCurrentLiquidatingMargin())
                .put("futuresSpreadMargin", grpc.getFuturesSpreadMargin())
                .put("additionalMargin", grpc.getAdditionalMargin())
                .put("unadjustedMarginRequirement", grpc.getUnadjustedMarginRequirement())
                .put("variationPremiumPayment", grpc.getVariationPremiumPayment());
    }
}
