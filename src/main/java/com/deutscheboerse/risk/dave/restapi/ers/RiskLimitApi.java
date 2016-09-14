package com.deutscheboerse.risk.dave.restapi.ers;

import io.vertx.core.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schojak on 29.8.16.
 */
public class RiskLimitApi extends AbstractErsApi {

    public RiskLimitApi(EventBus eb) {
        super(eb, "query.latestRiskLimit", "query.historyRiskLimit", "rl");
    }

    @Override
    protected List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add("clearer");
        parameters.add("member");
        parameters.add("maintainer");
        parameters.add("limitType");
        return parameters;
    }

}
