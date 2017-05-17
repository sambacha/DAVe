package com.deutscheboerse.risk.dave.persistence;

import com.deutscheboerse.risk.dave.grpc.*;
import com.deutscheboerse.risk.dave.model.Model;
import com.deutscheboerse.risk.dave.utils.ModelBuilder;
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
                this.query(request, response, ModelBuilder::buildAccountMarginFromQuery);
            }

            @Override
            public void queryLiquiGroupMargin(LiquiGroupMarginQuery request, GrpcWriteStream<LiquiGroupMargin> response) {
                this.query(request, response, ModelBuilder::buildLiquiGroupMarginFromQuery);
            }

            @Override
            public void queryLiquiGroupSplitMargin(LiquiGroupSplitMarginQuery request, GrpcWriteStream<LiquiGroupSplitMargin> response) {
                this.query(request, response, ModelBuilder::buildLiquiGroupSplitMarginFromQuery);
            }

            @Override
            public void queryPoolMargin(PoolMarginQuery request, GrpcWriteStream<PoolMargin> response) {
                this.query(request, response, ModelBuilder::buildPoolMarginFromQuery);
            }

            @Override
            public void queryPositionReport(PositionReportQuery request, GrpcWriteStream<PositionReport> response) {
                this.query(request, response, ModelBuilder::buildPositionReportFromQuery);
            }

            @Override
            public void queryRiskLimitUtilization(RiskLimitUtilizationQuery request, GrpcWriteStream<RiskLimitUtilization> response) {
                this.query(request, response, ModelBuilder::buildRiskLimitUtilizationFromQuery);
            }

            private <T extends MessageLite, R extends MessageLite>
            void query(T request, GrpcWriteStream<R> response, Function<T, Model<R>> responseBuilder) {
                if (health) {
                    response.write(responseBuilder.apply(request).toGrpc());
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
}
