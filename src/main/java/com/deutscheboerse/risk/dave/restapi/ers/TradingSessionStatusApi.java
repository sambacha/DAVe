package com.deutscheboerse.risk.dave.restapi.ers;

import io.vertx.core.eventbus.EventBus;
import java.util.Collections;
import java.util.List;

/**
 * Created by schojak on 29.8.16.
 */
public class TradingSessionStatusApi extends AbstractErsApi {

    public TradingSessionStatusApi(EventBus eb) {
        super(eb, "query.latestTradingSessionStatus", "query.historyTradingSessionStatus", "tss");
    }

    @Override
    protected List<String> getParameters() {
        return Collections.emptyList();
    }
}
