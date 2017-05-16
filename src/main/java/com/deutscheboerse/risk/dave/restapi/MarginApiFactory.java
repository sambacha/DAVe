package com.deutscheboerse.risk.dave.restapi;

import com.deutscheboerse.risk.dave.model.*;
import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import io.vertx.core.Vertx;

public class MarginApiFactory {

    private MarginApiFactory() {
        throw new Error("no instances");
    }

    public static MarginApi accountMarginApi(Vertx vertx, PersistenceService persistenceProxy) {
        return MarginApi.<AccountMarginModel>newBuilder(vertx)
                .setRequestName("am")
                .setFieldDescriptor(AccountMarginModel.FIELD_DESCRIPTOR)
                .setProxyFunction(persistenceProxy::queryAccountMargin)
                .build();
    }

    public static MarginApi liquiGroupMarginApi(Vertx vertx, PersistenceService persistenceProxy) {
        return MarginApi.<LiquiGroupMarginModel>newBuilder(vertx)
                .setRequestName("lgm")
                .setFieldDescriptor(LiquiGroupMarginModel.FIELD_DESCRIPTOR)
                .setProxyFunction(persistenceProxy::queryLiquiGroupMargin)
                .build();
    }

    public static MarginApi liquiGroupSplitMarginApi(Vertx vertx, PersistenceService persistenceProxy) {
        return MarginApi.<LiquiGroupSplitMarginModel>newBuilder(vertx)
                .setRequestName("lgsm")
                .setFieldDescriptor(LiquiGroupSplitMarginModel.FIELD_DESCRIPTOR)
                .setProxyFunction(persistenceProxy::queryLiquiGroupSplitMargin)
                .build();
    }

    public static MarginApi poolMarginApi(Vertx vertx, PersistenceService persistenceProxy) {
        return MarginApi.<PoolMarginModel>newBuilder(vertx)
                .setRequestName("pm")
                .setFieldDescriptor(PoolMarginModel.FIELD_DESCRIPTOR)
                .setProxyFunction(persistenceProxy::queryPoolMargin)
                .build();
    }

    public static MarginApi positionReportApi(Vertx vertx, PersistenceService persistenceProxy) {
        return MarginApi.<PositionReportModel>newBuilder(vertx)
                .setRequestName("pr")
                .setFieldDescriptor(PositionReportModel.FIELD_DESCRIPTOR)
                .setProxyFunction(persistenceProxy::queryPositionReport)
                .build();
    }

    public static MarginApi riskLimitUtilizationApi(Vertx vertx, PersistenceService persistenceProxy) {
        return MarginApi.<RiskLimitUtilizationModel>newBuilder(vertx)
                .setRequestName("rlu")
                .setFieldDescriptor(RiskLimitUtilizationModel.FIELD_DESCRIPTOR)
                .setProxyFunction(persistenceProxy::queryRiskLimitUtilization)
                .build();
    }
}
