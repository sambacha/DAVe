package com.deutscheboerse.risk.dave.utils;

import com.deutscheboerse.risk.dave.*;
import com.deutscheboerse.risk.dave.model.*;
import io.vertx.core.json.JsonObject;

public class ModelBuilder {

    public static final int HISTORY_SNAPSHOT_ID = 1;
    public static final int LATEST_SNAPSHOT_ID = 2;
    public static final int BUSINESS_DATE = 20131218;

    public static AccountMarginModel buildAccountMarginFromJson(JsonObject json) {
        return new AccountMarginModel(AccountMargin.newBuilder()
                .setSnapshotId(json.getInteger("snapshotID", 0))
                .setBusinessDate(json.getInteger("businessDate", 0))
                .setTimestamp(json.getLong("timestamp", 0L))
                .setClearer(json.getString("clearer", ""))
                .setMember(json.getString("member", ""))
                .setAccount(json.getString("account", ""))
                .setMarginCurrency(json.getString("marginCurrency", ""))
                .setClearingCurrency(json.getString("clearingCurrency", ""))
                .setPool(json.getString("pool", ""))
                .setMarginReqInMarginCurr(json.getDouble("marginReqInMarginCurr", 0.0))
                .setMarginReqInClrCurr(json.getDouble("marginReqInClrCurr", 0.0))
                .setUnadjustedMarginRequirement(json.getDouble("unadjustedMarginRequirement", 0.0))
                .setVariationPremiumPayment(json.getDouble("variationPremiumPayment", 0.0))
                .build());
    }

    public static  LiquiGroupMarginModel buildLiquiGroupMarginFromJson(JsonObject json) {
        return new LiquiGroupMarginModel(LiquiGroupMargin.newBuilder()
                .setSnapshotId(json.getInteger("snapshotID", 0))
                .setBusinessDate(json.getInteger("businessDate", 0))
                .setTimestamp(json.getLong("timestamp", 0L))
                .setClearer(json.getString("clearer", ""))
                .setMember(json.getString("member", ""))
                .setAccount(json.getString("account", ""))
                .setMarginClass(json.getString("marginClass", ""))
                .setMarginCurrency(json.getString("marginCurrency", ""))
                .setMarginGroup(json.getString("marginGroup", ""))
                .setPremiumMargin(json.getDouble("premiumMargin", 0.0))
                .setCurrentLiquidatingMargin(json.getDouble("currentLiquidatingMargin", 0.0))
                .setFuturesSpreadMargin(json.getDouble("futuresSpreadMargin", 0.0))
                .setAdditionalMargin(json.getDouble("additionalMargin", 0.0))
                .setUnadjustedMarginRequirement(json.getDouble("unadjustedMarginRequirement", 0.0))
                .setVariationPremiumPayment(json.getDouble("variationPremiumPayment", 0.0))
                .build());
    }

    public static  LiquiGroupSplitMarginModel buildLiquiGroupSplitMarginFromJson(JsonObject json) {
        return new LiquiGroupSplitMarginModel(LiquiGroupSplitMargin.newBuilder()
                .setSnapshotId(json.getInteger("snapshotID", 0))
                .setBusinessDate(json.getInteger("businessDate", 0))
                .setTimestamp(json.getLong("timestamp", 0L))
                .setClearer(json.getString("clearer", ""))
                .setMember(json.getString("member", ""))
                .setAccount(json.getString("account", ""))
                .setLiquidationGroup(json.getString("liquidationGroup", ""))
                .setLiquidationGroupSplit(json.getString("liquidationGroupSplit", ""))
                .setMarginCurrency(json.getString("marginCurrency", ""))
                .setPremiumMargin(json.getDouble("premiumMargin", 0.0))
                .setMarketRisk(json.getDouble("marketRisk", 0.0))
                .setLiquRisk(json.getDouble("liquRisk", 0.0))
                .setLongOptionCredit(json.getDouble("longOptionCredit", 0.0))
                .setVariationPremiumPayment(json.getDouble("variationPremiumPayment", 0.0))
                .build());
    }

    public static  PoolMarginModel buildPoolMarginFromJson(JsonObject json) {
        return new PoolMarginModel(PoolMargin.newBuilder()
                .setSnapshotId(json.getInteger("snapshotID", 0))
                .setBusinessDate(json.getInteger("businessDate", 0))
                .setTimestamp(json.getLong("timestamp", 0L))
                .setClearer(json.getString("clearer", ""))
                .setPool(json.getString("pool", ""))
                .setMarginCurrency(json.getString("marginCurrency", ""))
                .setClrRptCurrency(json.getString("clrRptCurrency", ""))
                .setRequiredMargin(json.getDouble("requiredMargin", 0.0))
                .setCashCollateralAmount(json.getDouble("cashCollateralAmount", 0.0))
                .setAdjustedSecurities(json.getDouble("adjustedSecurities", 0.0))
                .setAdjustedGuarantee(json.getDouble("adjustedGuarantee", 0.0))
                .setOverUnderInMarginCurr(json.getDouble("overUnderInMarginCurr", 0.0))
                .setOverUnderInClrRptCurr(json.getDouble("overUnderInClrRptCurr", 0.0))
                .setVariPremInMarginCurr(json.getDouble("variPremInMarginCurr", 0.0))
                .setAdjustedExchangeRate(json.getDouble("adjustedExchangeRate", 0.0))
                .setPoolOwner(json.getString("poolOwner", ""))
                .build());
    }

    public static  PositionReportModel buildPositionReportFromJson(JsonObject json) {
        return new PositionReportModel(PositionReport.newBuilder()
                .setSnapshotId(json.getInteger("snapshotID", 0))
                .setBusinessDate(json.getInteger("businessDate", 0))
                .setTimestamp(json.getLong("timestamp", 0L))
                .setClearer(json.getString("clearer", ""))
                .setMember(json.getString("member", ""))
                .setAccount(json.getString("account", ""))
                .setLiquidationGroup(json.getString("liquidationGroup", ""))
                .setLiquidationGroupSplit(json.getString("liquidationGroupSplit", ""))
                .setProduct(json.getString("product", ""))
                .setCallPut(json.getString("callPut", ""))
                .setContractYear(json.getInteger("contractYear", 0))
                .setContractMonth(json.getInteger("contractMonth", 0))
                .setExpiryDay(json.getInteger("expiryDay", 0))
                .setExercisePrice(json.getDouble("exercisePrice", 0.0))
                .setVersion(json.getString("version", ""))
                .setFlexContractSymbol(json.getString("flexContractSymbol", ""))
                .setNetQuantityLs(json.getDouble("netQuantityLs", 0.0))
                .setNetQuantityEa(json.getDouble("netQuantityEa", 0.0))
                .setClearingCurrency(json.getString("clearingCurrency", ""))
                .setMVar(json.getDouble("mVar", 0.0))
                .setCompVar(json.getDouble("compVar", 0.0))
                .setCompCorrelationBreak(json.getDouble("compCorrelationBreak", 0.0))
                .setCompCompressionError(json.getDouble("compCompressionError", 0.0))
                .setCompLiquidityAddOn(json.getDouble("compLiquidityAddOn", 0.0))
                .setCompLongOptionCredit(json.getDouble("compLongOptionCredit", 0.0))
                .setProductCurrency(json.getString("productCurrency", ""))
                .setVariationPremiumPayment(json.getDouble("variationPremiumPayment", 0.0))
                .setPremiumMargin(json.getDouble("premiumMargin", 0.0))
                .setNormalizedDelta(json.getDouble("normalizedDelta", 0.0))
                .setNormalizedGamma(json.getDouble("normalizedGamma", 0.0))
                .setNormalizedVega(json.getDouble("normalizedVega", 0.0))
                .setNormalizedRho(json.getDouble("normalizedRho", 0.0))
                .setNormalizedTheta(json.getDouble("normalizedTheta", 0.0))
                .setUnderlying(json.getString("underlying", ""))
                .build());
    }

    public static RiskLimitUtilizationModel buildRiskLimitUtilizationFromJson(JsonObject json) {
        return new RiskLimitUtilizationModel(RiskLimitUtilization.newBuilder()
                .setSnapshotId(json.getInteger("snapshotID", 0))
                .setBusinessDate(json.getInteger("businessDate", 0))
                .setTimestamp(json.getLong("timestamp", 0L))
                .setClearer(json.getString("clearer", ""))
                .setMember(json.getString("member", ""))
                .setMaintainer(json.getString("maintainer", ""))
                .setLimitType(json.getString("limitType", ""))
                .setUtilization(json.getDouble("utilization", 0.0))
                .setWarningLevel(json.getDouble("warningLevel", 0.0))
                .setThrottleLevel(json.getDouble("throttleLevel", 0.0))
                .setRejectLevel(json.getDouble("rejectLevel", 0.0))
                .build());
    }


    public static AccountMarginModel buildAccountMarginFromQuery(AccountMarginQuery request) {
        return new AccountMarginModel(AccountMargin.newBuilder()
                .setSnapshotId(request.getLatest() ? LATEST_SNAPSHOT_ID : HISTORY_SNAPSHOT_ID)
                .setBusinessDate(BUSINESS_DATE)
                .setClearer(request.getClearer())
                .setMember(request.getMember())
                .setAccount(request.getAccount())
                .setMarginCurrency(request.getMarginCurrency())
                .setClearingCurrency(request.getClearingCurrency())
                .setPool(request.getPool())
                .build());
    }

    public static LiquiGroupMarginModel buildLiquiGroupMarginFromQuery(LiquiGroupMarginQuery request) {
        return new LiquiGroupMarginModel(LiquiGroupMargin.newBuilder()
                .setSnapshotId(request.getLatest() ? LATEST_SNAPSHOT_ID : HISTORY_SNAPSHOT_ID)
                .setBusinessDate(BUSINESS_DATE)
                .setClearer(request.getClearer())
                .setMember(request.getMember())
                .setAccount(request.getAccount())
                .setMarginClass(request.getMarginClass())
                .setMarginCurrency(request.getMarginCurrency())
                .setMarginGroup(request.getMarginGroup())
                .build());
    }

    public static LiquiGroupSplitMarginModel buildLiquiGroupSplitMarginFromQuery(LiquiGroupSplitMarginQuery request) {
        return new LiquiGroupSplitMarginModel(LiquiGroupSplitMargin.newBuilder()
                .setSnapshotId(request.getLatest() ? LATEST_SNAPSHOT_ID : HISTORY_SNAPSHOT_ID)
                .setBusinessDate(BUSINESS_DATE)
                .setClearer(request.getClearer())
                .setMember(request.getMember())
                .setAccount(request.getAccount())
                .setLiquidationGroup(request.getLiquidationGroup())
                .setLiquidationGroupSplit(request.getLiquidationGroupSplit())
                .setMarginCurrency(request.getMarginCurrency())
                .build());
    }

    public static PoolMarginModel buildPoolMarginFromQuery(PoolMarginQuery request) {
        return new PoolMarginModel(PoolMargin.newBuilder()
                .setSnapshotId(request.getLatest() ? LATEST_SNAPSHOT_ID : HISTORY_SNAPSHOT_ID)
                .setBusinessDate(BUSINESS_DATE)
                .setClearer(request.getClearer())
                .setPool(request.getPool())
                .setMarginCurrency(request.getMarginCurrency())
                .setClrRptCurrency(request.getClrRptCurrency())
                .setPoolOwner(request.getPoolOwner())
                .build());
    }

    public static PositionReportModel buildPositionReportFromQuery(PositionReportQuery request) {
        return new PositionReportModel(PositionReport.newBuilder()
                .setSnapshotId(request.getLatest() ? LATEST_SNAPSHOT_ID : HISTORY_SNAPSHOT_ID)
                .setBusinessDate(BUSINESS_DATE)
                .setClearer(request.getClearer())
                .setMember(request.getMember())
                .setAccount(request.getAccount())
                .setLiquidationGroup(request.getLiquidationGroup())
                .setLiquidationGroupSplit(request.getLiquidationGroupSplit())
                .setProduct(request.getProduct())
                .setCallPut(request.getCallPut())
                .setContractYear(request.getContractYear())
                .setContractMonth(request.getContractMonth())
                .setExpiryDay(request.getExpiryDay())
                .setExercisePrice(request.getExercisePrice())
                .setVersion(request.getVersion())
                .setFlexContractSymbol(request.getFlexContractSymbol())
                .setClearingCurrency(request.getClearingCurrency())
                .setProductCurrency(request.getProductCurrency())
                .setUnderlying(request.getUnderlying())
                .build());
    }

    public static RiskLimitUtilizationModel buildRiskLimitUtilizationFromQuery(RiskLimitUtilizationQuery request) {
        return new RiskLimitUtilizationModel(RiskLimitUtilization.newBuilder()
                .setSnapshotId(request.getLatest() ? LATEST_SNAPSHOT_ID : HISTORY_SNAPSHOT_ID)
                .setBusinessDate(BUSINESS_DATE)
                .setClearer(request.getClearer())
                .setMember(request.getMember())
                .setMaintainer(request.getMaintainer())
                .setLimitType(request.getLimitType())
                .build());
    }
}
