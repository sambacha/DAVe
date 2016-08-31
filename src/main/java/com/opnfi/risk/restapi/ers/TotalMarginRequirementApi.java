package com.opnfi.risk.restapi.ers;

import io.vertx.core.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by schojak on 29.8.16.
 */
public class TotalMarginRequirementApi extends AbstractErsApi {

    public TotalMarginRequirementApi(EventBus eb) {
        super(eb, "query.latestTotalMarginRequirement", "query.historyTotalMarginRequirement", "tmr");
    }

    @Override
    protected List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add("clearer");
        parameters.add("pool");
        parameters.add("member");
        parameters.add("account");
        parameters.add("ccy");
        return parameters;
    }
}
