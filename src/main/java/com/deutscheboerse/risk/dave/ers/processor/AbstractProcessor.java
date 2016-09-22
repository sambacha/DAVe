package com.deutscheboerse.risk.dave.ers.processor;

import com.deutscheboerse.risk.dave.ers.jaxb.MarginAmountBlockT;
import com.deutscheboerse.risk.dave.ers.jaxb.PartiesBlockT;
import com.deutscheboerse.risk.dave.ers.jaxb.PtysSubGrpBlockT;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class AbstractProcessor {
    protected final DateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    protected void processParties(List<PartiesBlockT> parties, JsonObject data) {
        Map<Integer, String> partyKeys = new HashMap<>();
        partyKeys.put(1, "member");
        partyKeys.put(4, "clearer");
        parties.stream()
                .filter(party -> partyKeys.containsKey(party.getR().intValue()))
                .forEach(party -> {
                    data.put(partyKeys.get(party.getR().intValue()), party.getID());
                    processSubParties(party.getSub(), data);
                });
    }

    private void processSubParties(List<PtysSubGrpBlockT> subParties, JsonObject data) {
        if (subParties == null) return;
        Map<String, String> subPartyKeys = new HashMap<>();
        subPartyKeys.put("26", "account");
        subPartyKeys.put("4000", "pool");
        subPartyKeys.put("4001", "poolType");
        subParties.stream()
                .filter(subParty -> subPartyKeys.containsKey(subParty.getTyp()))
                .forEach(subParty -> data.put(subPartyKeys.get(subParty.getTyp()), subParty.getID()));
    }

    protected void processMarginBlocks(List<MarginAmountBlockT> margins, Set<String> typs, JsonObject data) {
        margins.stream()
                .filter(margin -> typs.contains(margin.getTyp()))
                .forEach(margin -> processMarginBlock(margin, data));
    }

    private void processMarginBlock(MarginAmountBlockT margin, JsonObject data) {
        switch (margin.getTyp()) {
            case "1":
                data.put("additionalMargin", margin.getAmt().doubleValue());
                data.put("ccy", margin.getCcy());
                break;
            case "2":
                data.put("adjustedMargin", margin.getAmt().doubleValue());
                data.put("ccy", margin.getCcy());
                break;
            case "3":
                data.put("unadjustedMargin", margin.getAmt().doubleValue());
                data.put("ccy", margin.getCcy());
                break;
            case "5":
                data.put("cashBalance", margin.getAmt().doubleValue());
                data.put("ccy", margin.getCcy());
                break;
            case "10":
                data.put("spreadMargin", margin.getAmt().doubleValue());
                data.put("ccy", margin.getCcy());
                break;
            case "12":
                data.put("liquiMargin", margin.getAmt().doubleValue());
                data.put("ccy", margin.getCcy());
                break;
            case "13":
                data.put("marginCall", margin.getAmt().doubleValue());
                break;
            case "14":
                data.put("shortfallSurplus", margin.getAmt().multiply(BigDecimal.valueOf(-1)).doubleValue());
                data.put("ccy", margin.getCcy());
                break;
            case "15":
                data.put("shortfallSurplus", margin.getAmt().doubleValue());
                data.put("ccy", margin.getCcy());
                break;
            case "17":
                data.put("premiumMargin", margin.getAmt().doubleValue());
                data.put("ccy", margin.getCcy());
                break;
            case "19":
                data.put("securityCollateral", margin.getAmt().doubleValue());
                data.put("ccy", margin.getCcy());
                break;
            case "22":
                data.put("marginRequirement", margin.getAmt().doubleValue());
                data.put("ccy", margin.getCcy());
                break;
            case "23":
                data.put("variationMargin", margin.getAmt().doubleValue());
                data.put("ccy", margin.getCcy());
                break;
        }
    }
}
