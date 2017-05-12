package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.*;
import com.deutscheboerse.risk.dave.utils.TestConfig;
import com.google.protobuf.MessageLite;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.grpc.GrpcWriteStream;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;

import java.util.function.Function;

public class StoreManagerMock {
    private static final Logger LOG = LoggerFactory.getLogger(StoreManagerMock.class);

    public static final int HISTORY_SNAPSHOT_ID = 1;
    public static final int LATEST_SNAPSHOT_ID = 2;
    public static final int BUSINESS_DATE = 20131218;

    private final Vertx vertx;
    private final VertxServer server;
    private boolean health = true;

    public StoreManagerMock(Vertx vertx) {
        this.vertx = vertx;
        this.server = this.createGrpcServer();
    }

    public StoreManagerMock listen(Handler<AsyncResult<Void>> resultHandler) {
        LOG.info("Starting web server on port {}", TestConfig.STORE_MANAGER_PORT);

        this.server.start(resultHandler);
        return this;
    }

    public StoreManagerMock setHealth(boolean health) {
        this.health = health;
        return this;
    }

    private VertxServer createGrpcServer() {
        return VertxServerBuilder
                .forPort(vertx, TestConfig.STORE_MANAGER_PORT)
                .addService(this.createService())
                .useSsl(options -> options
                        .setSsl(true)
                        .setUseAlpn(true)
                        .setPemKeyCertOptions(TestConfig.HTTP_STORAGE_CERTIFICATE.keyCertOptions())
                        .setPemTrustOptions(TestConfig.HTTP_STORAGE_CERTIFICATE.trustOptions())
                )
                .build();
    }

    private PersistenceServiceGrpc.PersistenceServiceVertxImplBase createService() {
        return new PersistenceServiceGrpc.PersistenceServiceVertxImplBase() {
            @Override
            public void queryAccountMargin(AccountMarginQuery request, GrpcWriteStream<AccountMargin> response) {
                this.query(request, response, StoreManagerMock.this::buildAccountMargin);
            }

            @Override
            public void queryLiquiGroupMargin(LiquiGroupMarginQuery request, GrpcWriteStream<LiquiGroupMargin> response) {
                this.query(request, response, StoreManagerMock.this::buildLiquiGroupMargin);
            }

            @Override
            public void queryLiquiGroupSplitMargin(LiquiGroupSplitMarginQuery request, GrpcWriteStream<LiquiGroupSplitMargin> response) {
                this.query(request, response, StoreManagerMock.this::buildLiquiGroupSplitMargin);
            }

            @Override
            public void queryPoolMargin(PoolMarginQuery request, GrpcWriteStream<PoolMargin> response) {
                this.query(request, response, StoreManagerMock.this::buildPoolMargin);
            }

            @Override
            public void queryPositionReport(PositionReportQuery request, GrpcWriteStream<PositionReport> response) {
                this.query(request, response, StoreManagerMock.this::buildPositionReport);
            }

            @Override
            public void queryRiskLimitUtilization(RiskLimitUtilizationQuery request, GrpcWriteStream<RiskLimitUtilization> response) {
                this.query(request, response, StoreManagerMock.this::buildRiskLimitUtilization);
            }

            private <T extends MessageLite, R extends MessageLite>
            void query(T request, GrpcWriteStream<R> response, Function<T, R> responseBuilder) {
                if (health) {
                    response.write(responseBuilder.apply(request));
                    response.end();
                } else {
                    response.fail(new StatusRuntimeException(Status.INVALID_ARGUMENT));
                }
            }
        };
    }

    public void close(Handler<AsyncResult<Void>> completionHandler) {
        LOG.info("Shutting down webserver");
        server.shutdown(completionHandler);
    }

    private AccountMargin buildAccountMargin(AccountMarginQuery request) {
        return AccountMargin.newBuilder()
                .setSnapshotId(request.getLatest() ? LATEST_SNAPSHOT_ID : HISTORY_SNAPSHOT_ID)
                .setBusinessDate(BUSINESS_DATE)
                .setClearer(request.getClearer())
                .setMember(request.getMember())
                .setAccount(request.getAccount())
                .setMarginCurrency(request.getMarginCurrency())
                .setClearingCurrency(request.getClearingCurrency())
                .setPool(request.getPool())
                .build();
    }

    private LiquiGroupMargin buildLiquiGroupMargin(LiquiGroupMarginQuery request) {
        return LiquiGroupMargin.newBuilder()
                .setSnapshotId(request.getLatest() ? LATEST_SNAPSHOT_ID : HISTORY_SNAPSHOT_ID)
                .setBusinessDate(BUSINESS_DATE)
                .setClearer(request.getClearer())
                .setMember(request.getMember())
                .setAccount(request.getAccount())
                .setMarginClass(request.getMarginClass())
                .setMarginCurrency(request.getMarginCurrency())
                .setMarginGroup(request.getMarginGroup())
                .build();
    }

    private LiquiGroupSplitMargin buildLiquiGroupSplitMargin(LiquiGroupSplitMarginQuery request) {
        return LiquiGroupSplitMargin.newBuilder()
                .setSnapshotId(request.getLatest() ? LATEST_SNAPSHOT_ID : HISTORY_SNAPSHOT_ID)
                .setBusinessDate(BUSINESS_DATE)
                .setClearer(request.getClearer())
                .setMember(request.getMember())
                .setAccount(request.getAccount())
                .setLiquidationGroup(request.getLiquidationGroup())
                .setLiquidationGroupSplit(request.getLiquidationGroupSplit())
                .setMarginCurrency(request.getMarginCurrency())
                .build();
    }

    private PoolMargin buildPoolMargin(PoolMarginQuery request) {
        return PoolMargin.newBuilder()
                .setSnapshotId(request.getLatest() ? LATEST_SNAPSHOT_ID : HISTORY_SNAPSHOT_ID)
                .setBusinessDate(BUSINESS_DATE)
                .setClearer(request.getClearer())
                .setPool(request.getPool())
                .setMarginCurrency(request.getMarginCurrency())
                .setClrRptCurrency(request.getClrRptCurrency())
                .setPoolOwner(request.getPoolOwner())
                .build();
    }

    private PositionReport buildPositionReport(PositionReportQuery request) {
        return PositionReport.newBuilder()
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
                .build();
    }

    private RiskLimitUtilization buildRiskLimitUtilization(RiskLimitUtilizationQuery request) {
        return RiskLimitUtilization.newBuilder()
                .setSnapshotId(request.getLatest() ? LATEST_SNAPSHOT_ID : HISTORY_SNAPSHOT_ID)
                .setBusinessDate(BUSINESS_DATE)
                .setClearer(request.getClearer())
                .setMember(request.getMember())
                .setMaintainer(request.getMaintainer())
                .setLimitType(request.getLimitType())
                .build();
    }
}
