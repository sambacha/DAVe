package com.deutscheboerse.risk.dave.ers;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by jakub on 21.09.16.
 */
public class InitialLoad {
    private static final Logger LOG = LoggerFactory.getLogger(InitialLoad.class);

    final private JsonArray membership;
    final private EventBus eb;

    public InitialLoad(JsonArray membership, EventBus eb)
    {
        this.membership = membership;
        this.eb = eb;
    }

    public void requestTradingSessionStatus()
    {
        LOG.trace("Requesting initial TradingSessionStatus");
        eb.publish("ers.TradingSessionStatusRequest", new JsonObject());
    }

    public void requestTotalMarginRequirement()
    {
        membership.forEach(member -> {
            JsonObject mbr = (JsonObject)member;
            JsonObject request = new JsonObject().put("member", mbr.getString("member")).put("clearer", mbr.getString("clearer"));

            mbr.getJsonArray("accounts").forEach(account -> {
                request.put("account", (String)account);

                LOG.info("Requesting initial TotalMarginRequirementRequest: {}", request);
                eb.publish("ers.TotalMarginRequirementRequest", request);
            });
        });
    }

    public void requestMarginShortfallSurplus()
    {
        membership.forEach(member -> {
            JsonObject mbr = (JsonObject)member;
            JsonObject request = new JsonObject().put("member", mbr.getString("member")).put("clearer", mbr.getString("clearer"));

            LOG.info("Requesting initial MarginShortfallSurplusRequest: {}", request);
            eb.publish("ers.MarginShortfallSurplusRequest", request);
        });
    }

    public void requestRiskLimits()
    {
        membership.forEach(member -> {
            JsonObject mbr = (JsonObject)member;

            JsonObject request1 = new JsonObject().put("member", mbr.getString("member")).put("clearer", mbr.getString("clearer")).put("maintainer", mbr.getString("clearer"));
            LOG.info("Requesting initial MarginShortfallSurplusRequest: {}", request1);
            eb.publish("ers.RiskLimitRequest", request1);

            if (!mbr.getString("clearer").equals(mbr.getString("member"))) {
                JsonObject request2 = new JsonObject().put("member", mbr.getString("member")).put("clearer", mbr.getString("clearer")).put("maintainer", mbr.getString("member"));
                LOG.info("Requesting initial MarginShortfallSurplusRequest: {}", request2);
                eb.publish("ers.RiskLimitRequest", request2);
            }
        });
    }
}
