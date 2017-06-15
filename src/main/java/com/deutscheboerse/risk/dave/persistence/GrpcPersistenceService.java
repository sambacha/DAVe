package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.config.StoreManagerConfig;
import com.deutscheboerse.risk.dave.grpc.*;
import com.deutscheboerse.risk.dave.healthcheck.HealthCheck;
import com.deutscheboerse.risk.dave.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.MessageLite;
import io.grpc.ManagedChannel;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
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
import java.util.ArrayList;
import java.util.List;
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
    public void queryAccountMargin(RequestType type, JsonObject json, Handler<AsyncResult<List<AccountMarginModel>>> resultHandler) {
        AccountMarginQuery request = AccountMarginQuery.newBuilder()
                .setLatest(type == RequestType.LATEST)
                .setClearer(json.getString("clearer", "*"))
                .setMember(json.getString("member", "*"))
                .setAccount(json.getString("account", "*"))
                .setMarginCurrency(json.getString("marginCurrency", "*"))
                .setClearingCurrency(json.getString("clearingCurrency", "*"))
                .setPool(json.getString("pool", "*"))
                .build();

        this.query(request, getOrCreateGrpcService()::queryAccountMargin, AccountMarginModel::new, resultHandler);
    }

    @Override
    public void queryLiquiGroupMargin(RequestType type, JsonObject json, Handler<AsyncResult<List<LiquiGroupMarginModel>>> resultHandler) {
        LiquiGroupMarginQuery request = LiquiGroupMarginQuery.newBuilder()
                .setLatest(type == RequestType.LATEST)
                .setClearer(json.getString("clearer", "*"))
                .setMember(json.getString("member", "*"))
                .setAccount(json.getString("account", "*"))
                .setMarginClass(json.getString("marginClass", "*"))
                .setMarginCurrency(json.getString("marginCurrency", "*"))
                .setMarginGroup(json.getString("marginGroup", "*"))
                .build();

        this.query(request, getOrCreateGrpcService()::queryLiquiGroupMargin, LiquiGroupMarginModel::new, resultHandler);
    }

    @Override
    public void queryLiquiGroupSplitMargin(RequestType type, JsonObject json, Handler<AsyncResult<List<LiquiGroupSplitMarginModel>>> resultHandler) {
        LiquiGroupSplitMarginQuery request = LiquiGroupSplitMarginQuery.newBuilder()
                .setLatest(type == RequestType.LATEST)
                .setClearer(json.getString("clearer", "*"))
                .setMember(json.getString("member", "*"))
                .setAccount(json.getString("account", "*"))
                .setLiquidationGroup(json.getString("liquidationGroup", "*"))
                .setLiquidationGroupSplit(json.getString("liquidationGroupSplit", "*"))
                .setMarginCurrency(json.getString("marginCurrency", "*"))
                .build();

        this.query(request, getOrCreateGrpcService()::queryLiquiGroupSplitMargin, LiquiGroupSplitMarginModel::new, resultHandler);
    }

    @Override
    public void queryPoolMargin(RequestType type, JsonObject json, Handler<AsyncResult<List<PoolMarginModel>>> resultHandler) {
        PoolMarginQuery request = PoolMarginQuery.newBuilder()
                .setLatest(type == RequestType.LATEST)
                .setClearer(json.getString("clearer", "*"))
                .setPool(json.getString("pool", "*"))
                .setMarginCurrency(json.getString("marginCurrency", "*"))
                .setClrRptCurrency(json.getString("clrRptCurrency", "*"))
                .setPoolOwner(json.getString("poolOwner", "*"))
                .build();

        this.query(request, getOrCreateGrpcService()::queryPoolMargin, PoolMarginModel::new, resultHandler);
    }

    @Override
    public void queryPositionReport(RequestType type, JsonObject json, Handler<AsyncResult<List<PositionReportModel>>> resultHandler) {
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

        this.query(request, getOrCreateGrpcService()::queryPositionReport, PositionReportModel::new, resultHandler);
    }

    @Override
    public void queryRiskLimitUtilization(RequestType type, JsonObject json, Handler<AsyncResult<List<RiskLimitUtilizationModel>>> resultHandler) {
        RiskLimitUtilizationQuery request = RiskLimitUtilizationQuery.newBuilder()
                .setLatest(type == RequestType.LATEST)
                .setClearer(json.getString("clearer", "*"))
                .setMember(json.getString("member", "*"))
                .setMaintainer(json.getString("maintainer", "*"))
                .setLimitType(json.getString("limitType", "*"))
                .build();

        this.query(request, getOrCreateGrpcService()::queryRiskLimitUtilization, RiskLimitUtilizationModel::new, resultHandler);
    }

    @Override
    public void close(Handler<AsyncResult<Void>> resultHandler) {
        if (this.grpcService != null) {
            this.grpcService = null;
            this.channel.shutdown();
            this.channel = null;
        }
        resultHandler.handle(Future.succeededFuture());
    }

    private <GrpcType extends MessageLite, QueryType extends MessageLite, ModelType extends Model>
    void query(QueryType request,
               BiConsumer<QueryType, Handler<GrpcReadStream<GrpcType>>> queryFunction,
               Function<GrpcType, ModelType> modelFactory,
               Handler<AsyncResult<List<ModelType>>> resultHandler) {

        List<ModelType> result = new ArrayList<>();
        queryFunction.accept(request, stream ->
                stream.handler(grpc -> {
                    result.add(modelFactory.apply(grpc));
                }).endHandler(v -> {
                    resultHandler.handle(Future.succeededFuture(result));
                }).exceptionHandler(ex -> {
                    LOG.error(ex);
                    this.channel.shutdown();
                    this.grpcService = null;
                    resultHandler.handle(Future.failedFuture(ex));
                })
        );
    }
}
