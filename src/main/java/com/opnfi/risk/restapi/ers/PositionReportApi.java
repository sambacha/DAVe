package com.opnfi.risk.restapi.ers;

import io.vertx.core.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;

public class PositionReportApi extends AbstractErsApi {

    public PositionReportApi(EventBus eb) {
        super(eb, "query.latestPositionReport", "query.historyPositionReport", "pr");
    }

    @Override
    protected List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add("clearer");
        parameters.add("member");
        parameters.add("account");
        parameters.add("symbol");
        parameters.add("putCall");
        parameters.add("strikePrice");
        parameters.add("optAttribute");
        parameters.add("maturityMonthYear");
        return parameters;
    }

}
