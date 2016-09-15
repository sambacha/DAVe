package com.deutscheboerse.risk.dave.model.processor;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import com.deutscheboerse.risk.dave.model.jaxb.MarginAmountBlockT;
import io.vertx.core.json.JsonObject;

public class AbstractProcessor {
    protected final DateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    
    protected void processMarginBlocks(List<MarginAmountBlockT> margins, Set<String> typs, JsonObject data) {
        for (MarginAmountBlockT margin : margins) {
            String marginTyp = margin.getTyp();
            if (!typs.contains(marginTyp)) continue;
            switch (marginTyp) {
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
}
