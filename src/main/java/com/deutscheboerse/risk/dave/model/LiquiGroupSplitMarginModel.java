package com.deutscheboerse.risk.dave.model;

import com.deutscheboerse.risk.dave.grpc.LiquiGroupSplitMargin;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class LiquiGroupSplitMarginModel implements Model<LiquiGroupSplitMargin> {

    public static final FieldDescriptor FIELD_DESCRIPTOR = FieldDescriptor.newBuilder()
            .addField("clearer", String.class)
            .addField("member", String.class)
            .addField("account", String.class)
            .addField("liquidationGroup", String.class)
            .addField("liquidationGroupSplit", String.class)
            .addField("marginCurrency", String.class)
            .build();

    private final LiquiGroupSplitMargin grpc;

    public LiquiGroupSplitMarginModel(LiquiGroupSplitMargin grpc) {
        this.grpc = grpc;
    }

    public LiquiGroupSplitMarginModel(JsonObject json) {
        verifyJson(json);
        this.grpc = json.mapTo(LiquiGroupSplitMargin.class);
    }

    @Override
    public LiquiGroupSplitMargin toGrpc() {
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
                .put("liquidationGroup", grpc.getLiquidationGroup())
                .put("liquidationGroupSplit", grpc.getLiquidationGroupSplit())
                .put("marginCurrency", grpc.getMarginCurrency())
                .put("premiumMargin", grpc.getPremiumMargin())
                .put("marketRisk", grpc.getMarketRisk())
                .put("liquRisk", grpc.getLiquRisk())
                .put("longOptionCredit", grpc.getLongOptionCredit())
                .put("variationPremiumPayment", grpc.getVariationPremiumPayment());
    }
}
