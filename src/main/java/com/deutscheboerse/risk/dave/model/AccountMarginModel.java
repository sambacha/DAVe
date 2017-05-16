package com.deutscheboerse.risk.dave.model;

import com.deutscheboerse.risk.dave.grpc.AccountMargin;
import com.google.protobuf.InvalidProtocolBufferException;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class AccountMarginModel implements Model<AccountMargin> {

    private final AccountMargin grpc;

    public AccountMarginModel(AccountMargin grpc) {
        this.grpc = grpc;
    }

    public AccountMarginModel(JsonObject json) {
        verifyJson(json);
        try {
            this.grpc = AccountMargin.parseFrom(json.getBinary("grpc"));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AccountMargin toGrpc() {
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
                .put("marginCurrency", grpc.getMarginCurrency())
                .put("clearingCurrency", grpc.getClearingCurrency())
                .put("pool", grpc.getPool())
                .put("marginReqInMarginCurr", grpc.getMarginReqInMarginCurr())
                .put("marginReqInClrCurr", grpc.getMarginReqInClrCurr())
                .put("unadjustedMarginRequirement", grpc.getUnadjustedMarginRequirement())
                .put("variationPremiumPayment", grpc.getVariationPremiumPayment());
    }

    private static KeyDescriptor<AccountMarginModel> keyDescriptor;

    public static KeyDescriptor<AccountMarginModel> getKeyDescriptor() {
        if (keyDescriptor == null) {
            keyDescriptor = KeyDescriptor.newBuilder()
                    .addField("clearer", String.class)
                    .addField("member", String.class)
                    .addField("account", String.class)
                    .addField("marginCurrency", String.class)
                    .addField("clearingCurrency", String.class)
                    .addField("pool", String.class)
                    .build();
        }
        return keyDescriptor;
    }
}
