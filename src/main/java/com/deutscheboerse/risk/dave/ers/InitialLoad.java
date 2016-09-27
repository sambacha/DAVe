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
    final private JsonArray products;
    final private EventBus eb;
    final private String memberId;

    public InitialLoad(String memberId, JsonArray membership, JsonArray products, EventBus eb)
    {
        this.memberId = memberId;
        this.membership = membership;
        this.products = products;
        this.eb = eb;
    }

    public void requestTradingSessionStatus()
    {
        LOG.trace("Requesting initial TradingSessionStatus");
        eb.publish("ers.TradingSessionStatusRequest", new JsonObject());
    }

    public void requestPositionReports()
    {
        membership.forEach(member -> {
            JsonObject mbr = (JsonObject)member;
            JsonObject request = new JsonObject().put("member", mbr.getString("member")).put("clearer", mbr.getString("clearer"));

            mbr.getJsonArray("accounts").forEach(account -> {
                request.put("account", (String)account);

                products.forEach(product -> {
                    request.put("product", (String)product);

                    LOG.trace("Requesting initial TotalMarginRequirementRequest: {}", request);
                    eb.publish("ers.PositionReportRequest", request);
                });
            });
        });
    }

    public void requestTotalMarginRequirement()
    {
        membership.forEach(member -> {
            JsonObject mbr = (JsonObject)member;
            JsonObject request = new JsonObject().put("member", mbr.getString("member")).put("clearer", mbr.getString("clearer")).put("pool", "");

            mbr.getJsonArray("accounts").forEach(account -> {
                request.put("account", (String)account);

                LOG.trace("Requesting initial TotalMarginRequirementRequest: {}", request);
                eb.publish("ers.TotalMarginRequirementRequest", request);
            });
        });
    }

    public void requestMarginShortfallSurplus()
    {
        membership.forEach(member -> {
            JsonObject mbr = (JsonObject)member;
            JsonObject request = new JsonObject().put("member", mbr.getString("member")).put("clearer", mbr.getString("clearer")).put("pool", "");

            LOG.trace("Requesting initial MarginShortfallSurplusRequest: {}", request);
            eb.publish("ers.MarginShortfallSurplusRequest", request);
        });
    }

    public void requestRiskLimits()
    {
        membership.forEach(member -> {
            JsonObject mbr = (JsonObject)member;

            JsonObject request1 = new JsonObject().put("member", mbr.getString("member")).put("clearer", mbr.getString("clearer")).put("maintainer", memberId);
            LOG.trace("Requesting initial RiskLimitRequest: {}", request1);
            eb.publish("ers.RiskLimitRequest", request1);
        });
    }
}
