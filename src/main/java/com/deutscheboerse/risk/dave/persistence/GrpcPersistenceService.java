package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.*;
import com.deutscheboerse.risk.dave.config.StoreManagerConfig;
import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.MessageLite;
import io.grpc.ManagedChannel;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.core.net.TCPSSLOptions;
import io.vertx.grpc.GrpcReadStream;
import io.vertx.grpc.VertxChannelBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;

public class GrpcPersistenceService implements PersistenceService {
    private static final Logger LOG = LoggerFactory.getLogger(GrpcPersistenceService.class);
    private static final java.util.logging.Logger GRPC_LOG = java.util.logging.Logger.getLogger("io.grpc");

    private final Vertx vertx;
    private final StoreManagerConfig config;
    private final HealthCheck healthCheck;
    private ManagedChannel channel;
    private PersistenceServiceGrpc.PersistenceServiceVertxStub grpcService;

    static {
        // Disable grpc info logs
        GRPC_LOG.setLevel(Level.WARNING);
    }

    @Inject
    public GrpcPersistenceService(Vertx vertx, @Named("storeManager.conf") JsonObject config) throws IOException {
        this.vertx = vertx;
        this.config = (new ObjectMapper()).readValue(config.toString(), StoreManagerConfig.class);
        this.healthCheck = new HealthCheck(vertx);
    }

    private ManagedChannel createGrpcChannel() {
         return VertxChannelBuilder
                    .forAddress(vertx, config.getHostname(), config.getPort())
                    .useSsl(this::setGrpcSslOptions)
                    .build();
    }

    private PersistenceServiceGrpc.PersistenceServiceVertxStub getOrCreateGrpcService() {
        if (this.grpcService == null) {
            this.channel = createGrpcChannel();
            this.grpcService = PersistenceServiceGrpc.newVertxStub(this.channel);
        }
        return this.grpcService;
    }

    private void setGrpcSslOptions(TCPSSLOptions sslOptions) {
        PemTrustOptions pemTrustOptions = new PemTrustOptions();
        this.config.getSslTrustCerts()
                .forEach(trustKey -> pemTrustOptions.addCertValue(Buffer.buffer(trustKey)));
        sslOptions
                .setSsl(true)
                .setUseAlpn(true)
                .setPemTrustOptions(pemTrustOptions);
        final String sslCert = this.config.getSslCert();
        final String sslKey = this.config.getSslKey();
        if (sslKey != null && sslCert != null) {
            PemKeyCertOptions pemKeyCertOptions = new PemKeyCertOptions()
                    .setKeyValue(Buffer.buffer(sslKey))
                    .setCertValue(Buffer.buffer(sslCert));
            sslOptions.setPemKeyCertOptions(pemKeyCertOptions);
        }
    }

    @Override
    public void initialize(Handler<AsyncResult<Void>> resultHandler) {
        healthCheck.setComponentReady(HealthCheck.Component.PERSISTENCE_SERVICE);
        resultHandler.handle(Future.succeededFuture());
    }

    @Override
    public void queryAccountMargin(RequestType type, JsonObject json, Handler<AsyncResult<String>> resultHandler) {
        AccountMarginQuery request = AccountMarginQuery.newBuilder()
                .setLatest(type == RequestType.LATEST)
                .setClearer(json.getString("clearer", "*"))
                .setMember(json.getString("member", "*"))
                .setAccount(json.getString("account", "*"))
                .setMarginCurrency(json.getString("marginCurrency", "*"))
                .setClearingCurrency(json.getString("clearingCurrency", "*"))
                .setPool(json.getString("pool", "*"))
                .build();

        this.query(request, getOrCreateGrpcService()::queryAccountMargin, this::accountMarginToJson, resultHandler);
    }

    @Override
    public void queryLiquiGroupMargin(RequestType type, JsonObject json, Handler<AsyncResult<String>> resultHandler) {
        LiquiGroupMarginQuery request = LiquiGroupMarginQuery.newBuilder()
                .setLatest(type == RequestType.LATEST)
                .setClearer(json.getString("clearer", "*"))
                .setMember(json.getString("member", "*"))
                .setAccount(json.getString("account", "*"))
                .setMarginClass(json.getString("marginClass", "*"))
                .setMarginCurrency(json.getString("marginCurrency", "*"))
                .setMarginGroup(json.getString("marginGroup", "*"))
                .build();

        this.query(request, getOrCreateGrpcService()::queryLiquiGroupMargin, this::liquiGroupMarginToJson, resultHandler);
    }

    @Override
    public void queryLiquiGroupSplitMargin(RequestType type, JsonObject json, Handler<AsyncResult<String>> resultHandler) {
        LiquiGroupSplitMarginQuery request = LiquiGroupSplitMarginQuery.newBuilder()
                .setLatest(type == RequestType.LATEST)
                .setClearer(json.getString("clearer", "*"))
                .setMember(json.getString("member", "*"))
                .setAccount(json.getString("account", "*"))
                .setLiquidationGroup(json.getString("liquidationGroup", "*"))
                .setLiquidationGroupSplit(json.getString("liquidationGroupSplit", "*"))
                .setMarginCurrency(json.getString("marginCurrency", "*"))
                .build();

        this.query(request, getOrCreateGrpcService()::queryLiquiGroupSplitMargin, this::liquiGroupSplitMarginToJson, resultHandler);
    }

    @Override
    public void queryPoolMargin(RequestType type, JsonObject json, Handler<AsyncResult<String>> resultHandler) {
        PoolMarginQuery request = PoolMarginQuery.newBuilder()
                .setLatest(type == RequestType.LATEST)
                .setClearer(json.getString("clearer", "*"))
                .setPool(json.getString("pool", "*"))
                .setMarginCurrency(json.getString("marginCurrency", "*"))
                .setClrRptCurrency(json.getString("clrRptCurrency", "*"))
                .setPoolOwner(json.getString("poolOwner", "*"))
                .build();

        this.query(request, getOrCreateGrpcService()::queryPoolMargin, this::poolMarginToJson, resultHandler);
    }

    @Override
    public void queryPositionReport(RequestType type, JsonObject json, Handler<AsyncResult<String>> resultHandler) {
        PositionReportQuery request = PositionReportQuery.newBuilder()
                .setLatest(type == RequestType.LATEST)
                .setClearer(json.getString("clearer", "*"))
                .setMember(json.getString("member", "*"))
                .setAccount(json.getString("account", "*"))
                .setLiquidationGroup(json.getString("liquidationGroup", "*"))
                .setLiquidationGroupSplit(json.getString("liquidationGroupSplit", "*"))
                .setProduct(json.getString("product", "*"))
                .setCallPut(json.getString("callPut", "*"))
                .setContractYear(json.getInteger("contractYear", -1))
                .setContractMonth(json.getInteger("contractMonth", -1))
                .setExpiryDay(json.getInteger("expiryDay", -1))
                .setExercisePrice(json.getDouble("exercisePrice", -1.0))
                .setVersion(json.getString("version", "*"))
                .setFlexContractSymbol(json.getString("flexContractSymbol", "*"))
                .setClearingCurrency(json.getString("clearingCurrency", "*"))
                .setProductCurrency(json.getString("productCurrency", "*"))
                .setUnderlying(json.getString("underlying", "*"))
                .build();

        this.query(request, getOrCreateGrpcService()::queryPositionReport, this::positionReportToJson, resultHandler);
    }

    @Override
    public void queryRiskLimitUtilization(RequestType type, JsonObject json, Handler<AsyncResult<String>> resultHandler) {
        RiskLimitUtilizationQuery request = RiskLimitUtilizationQuery.newBuilder()
                .setLatest(type == RequestType.LATEST)
                .setClearer(json.getString("clearer", "*"))
                .setMember(json.getString("member", "*"))
                .setMaintainer(json.getString("maintainer", "*"))
                .setLimitType(json.getString("limitType", "*"))
                .build();

        this.query(request, getOrCreateGrpcService()::queryRiskLimitUtilization, this::riskLimitUtilizationToJson, resultHandler);
    }

    @Override
    public void close() {
        // Empty
    }

    private <GrpcType extends MessageLite, QueryType extends MessageLite>
    void query(QueryType request,
               BiConsumer<QueryType, Handler<GrpcReadStream<GrpcType>>> queryFunction,
               Function<GrpcType, JsonObject> convertToJson,
               Handler<AsyncResult<String>> resultHandler) {

        JsonArray result = new JsonArray();
        queryFunction.accept(request, stream ->
                stream.handler(grpc -> {
                    result.add(convertToJson.apply(grpc));
                }).endHandler(v -> {
                    resultHandler.handle(Future.succeededFuture(result.toString()));
                }).exceptionHandler(ex -> {
                    LOG.error(ex);
                    this.channel.shutdown();
                    this.grpcService = null;
                    resultHandler.handle(Future.failedFuture(ex));
                })
        );
    }

    private JsonObject accountMarginToJson(AccountMargin grpc) {
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

    private JsonObject liquiGroupMarginToJson(LiquiGroupMargin grpc) {
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

    private JsonObject liquiGroupSplitMarginToJson(LiquiGroupSplitMargin grpc) {
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

    private JsonObject poolMarginToJson(PoolMargin grpc) {
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

    private JsonObject positionReportToJson(PositionReport grpc) {
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

    private JsonObject riskLimitUtilizationToJson(RiskLimitUtilization grpc) {
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
