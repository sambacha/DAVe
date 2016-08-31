package com.opnfi.risk.restapi.ers;

import io.vertx.core.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by schojak on 29.8.16.
 */
public class MarginShortfallSurplusApi extends AbstractErsApi {

    public MarginShortfallSurplusApi(EventBus eb) {
        super(eb, "query.latestMarginShortfallSurplus", "query.historyMarginShortfallSurplus", "mss");
    }

    @Override
    protected List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add("clearer");
        parameters.add("pool");
        parameters.add("member");
        parameters.add("clearingCcy");
        parameters.add("ccy");
        return parameters;
    }

}
