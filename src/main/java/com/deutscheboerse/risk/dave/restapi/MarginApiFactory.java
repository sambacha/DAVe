package com.deutscheboerse.risk.dave.restapi;

import com.deutscheboerse.risk.dave.model.*;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import io.vertx.core.Vertx;

public class MarginApiFactory {

    private MarginApiFactory() {
        throw new Error("no instances");
    }

    public static MarginApi accountMarginApi(Vertx vertx, PersistenceService persistenceProxy) {
        return MarginApi.newBuilder(vertx)
                .setRequestName("am")
                .setKeyDescriptor(AccountMarginModel.getKeyDescriptor())
                .setProxyFunction(persistenceProxy::queryAccountMargin)
                .build();
    }

    public static MarginApi liquiGroupMarginApi(Vertx vertx, PersistenceService persistenceProxy) {
        return MarginApi.newBuilder(vertx)
                .setRequestName("lgm")
                .setKeyDescriptor(LiquiGroupMarginModel.getKeyDescriptor())
                .setProxyFunction(persistenceProxy::queryLiquiGroupMargin)
                .build();
    }

    public static MarginApi liquiGroupSplitMarginApi(Vertx vertx, PersistenceService persistenceProxy) {
        return MarginApi.newBuilder(vertx)
                .setRequestName("lgsm")
                .setKeyDescriptor(LiquiGroupSplitMarginModel.getKeyDescriptor())
                .setProxyFunction(persistenceProxy::queryLiquiGroupSplitMargin)
                .build();
    }

    public static MarginApi poolMarginApi(Vertx vertx, PersistenceService persistenceProxy) {
        return MarginApi.newBuilder(vertx)
                .setRequestName("pm")
                .setKeyDescriptor(PoolMarginModel.getKeyDescriptor())
                .setProxyFunction(persistenceProxy::queryPoolMargin)
                .build();
    }

    public static MarginApi positionReportApi(Vertx vertx, PersistenceService persistenceProxy) {
        return MarginApi.newBuilder(vertx)
                .setRequestName("pr")
                .setKeyDescriptor(PositionReportModel.getKeyDescriptor())
                .setProxyFunction(persistenceProxy::queryPositionReport)
                .build();
    }

    public static MarginApi riskLimitUtilizationApi(Vertx vertx, PersistenceService persistenceProxy) {
        return MarginApi.newBuilder(vertx)
                .setRequestName("rlu")
                .setKeyDescriptor(RiskLimitUtilizationModel.getKeyDescriptor())
                .setProxyFunction(persistenceProxy::queryRiskLimitUtilization)
                .build();
    }
}
