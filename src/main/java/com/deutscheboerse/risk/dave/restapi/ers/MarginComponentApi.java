package com.deutscheboerse.risk.dave.restapi.ers;

import io.vertx.core.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by schojak on 29.8.16.
 */
public class MarginComponentApi extends AbstractErsApi {

    public MarginComponentApi(EventBus eb) {
        super(eb, "query.latestMarginComponent", "query.historyMarginComponent", "mc");
    }

    @Override
    protected List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add("clearer");
        parameters.add("member");
        parameters.add("account");
        parameters.add("clss");
        parameters.add("ccy");
        return parameters;
    }

}
