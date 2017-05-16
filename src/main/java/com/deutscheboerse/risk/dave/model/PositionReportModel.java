package com.deutscheboerse.risk.dave.model;

import com.deutscheboerse.risk.dave.PositionReport;
import com.google.protobuf.InvalidProtocolBufferException;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class PositionReportModel implements Model<PositionReport> {

    private final PositionReport grpc;

    public PositionReportModel(PositionReport grpc) {
        this.grpc = grpc;
    }

    public PositionReportModel(JsonObject json) {
        verifyJson(json);
        try {
            this.grpc = PositionReport.parseFrom(json.getBinary("grpc"));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PositionReport toGrpc() {
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
                .put("product", grpc.getProduct())
                .put("callPut", grpc.getCallPut())
                .put("contractYear", grpc.getContractYear())
                .put("contractMonth", grpc.getContractMonth())
                .put("expiryDay", grpc.getExpiryDay())
                .put("exercisePrice", grpc.getExercisePrice())
                .put("version", grpc.getVersion())
                .put("flexContractSymbol", grpc.getFlexContractSymbol())
                .put("netQuantityLs", grpc.getNetQuantityLs())
                .put("netQuantityEa", grpc.getNetQuantityEa())
                .put("clearingCurrency", grpc.getClearingCurrency())
                .put("mVar", grpc.getMVar())
                .put("compVar", grpc.getCompVar())
                .put("compCorrelationBreak", grpc.getCompCorrelationBreak())
                .put("compCompressionError", grpc.getCompCompressionError())
                .put("compLiquidityAddOn", grpc.getCompLiquidityAddOn())
                .put("compLongOptionCredit", grpc.getCompLongOptionCredit())
                .put("productCurrency", grpc.getProductCurrency())
                .put("variationPremiumPayment", grpc.getVariationPremiumPayment())
                .put("premiumMargin", grpc.getPremiumMargin())
                .put("normalizedDelta", grpc.getNormalizedDelta())
                .put("normalizedGamma", grpc.getNormalizedGamma())
                .put("normalizedVega", grpc.getNormalizedVega())
                .put("normalizedRho", grpc.getNormalizedRho())
                .put("normalizedTheta", grpc.getNormalizedTheta())
                .put("underlying", grpc.getUnderlying());
    }

    private static KeyDescriptor keyDescriptor;

    public static KeyDescriptor getKeyDescriptor() {
        if (keyDescriptor == null) {
            keyDescriptor = KeyDescriptor.newBuilder()
                    .addField("clearer", String.class)
                    .addField("member", String.class)
                    .addField("account", String.class)
                    .addField("liquidationGroup", String.class)
                    .addField("liquidationGroupSplit", String.class)
                    .addField("product", String.class)
                    .addField("callPut", String.class)
                    .addField("contractYear", Integer.class)
                    .addField("contractMonth", Integer.class)
                    .addField("expiryDay", Integer.class)
                    .addField("exercisePrice", Double.class)
                    .addField("version", String.class)
                    .addField("flexContractSymbol", String.class)
                    .addField("clearingCurrency", String.class)
                    .addField("productCurrency", String.class)
                    .addField("underlying", String.class)
                    .build();
        }
        return keyDescriptor;
    }
}
