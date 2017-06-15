package com.deutscheboerse.risk.dave.utils;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schojak on 8.9.16.
 */
public class DummyData {
    public final static List<JsonObject> tradingSessionStatusJson;
    static {
        tradingSessionStatusJson = new ArrayList<>();
        tradingSessionStatusJson.add(new JsonObject("{ \"reqId\" : null, \"sesId\" : \"1\", \"stat\" : \"1\", \"statRejRsn\" : null, \"txt\" : null, \"received\" : { \"$date\": \"2016-09-01T11:39:28.088Z\" } }"));
        tradingSessionStatusJson.add(new JsonObject("{ \"reqId\" : null, \"sesId\" : \"1\", \"stat\" : \"2\", \"statRejRsn\" : null, \"txt\" : null, \"received\" : { \"$date\": \"2016-09-01T11:40:28.088Z\" } }"));
    }

    public final static List<JsonObject> positionReportJson;
    static {
        positionReportJson = new ArrayList<>();
        positionReportJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"member\" : \"DEFFR\", \"account\" : \"PP\", \"reqId\" : null, \"rptId\" : \"13198434645154\", \"bizDt\" : \"2013-12-17\", \"lastReportRequested\" : null, \"settlSesId\" : \"ITD\", \"symbol\" : \"FGBL\", \"putCall\" : null, \"maturityMonthYear\" : \"201312\", \"strikePrice\" : null, \"optAttribute\" : null, \"crossMarginLongQty\" : 0, \"crossMarginShortQty\" : 600, \"optionExcerciseQty\" : null, \"optionAssignmentQty\" : null, \"allocationTradeQty\" : 0, \"deliveryNoticeQty\" : 0, \"received\" : { \"$date\": \"2016-09-02T08:36:27.771Z\"} }"));
        positionReportJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"member\" : \"ABCFR\", \"account\" : \"A1\", \"reqId\" : null, \"rptId\" : \"13198434645156\", \"bizDt\" : \"2013-12-17\", \"lastReportRequested\" : null, \"settlSesId\" : \"ITD\", \"symbol\" : \"OGBL\", \"putCall\" : \"C\", \"maturityMonthYear\" : \"201401\", \"strikePrice\" : \"10800\", \"optAttribute\" : \"0\", \"crossMarginLongQty\" : 600, \"crossMarginShortQty\" : 900, \"optionExcerciseQty\" : 0, \"optionAssignmentQty\" : 0, \"allocationTradeQty\" : null, \"deliveryNoticeQty\" : null, \"received\" : { \"$date\": \"2016-09-02T08:36:27.771Z\"} }"));
        positionReportJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"member\" : \"DEFFR\", \"account\" : \"PP\", \"reqId\" : null, \"rptId\" : \"13198434645154\", \"bizDt\" : \"2013-12-17\", \"lastReportRequested\" : null, \"settlSesId\" : \"ITD\", \"symbol\" : \"FGBL\", \"putCall\" : null, \"maturityMonthYear\" : \"201312\", \"strikePrice\" : null, \"optAttribute\" : null, \"crossMarginLongQty\" : 100, \"crossMarginShortQty\" : 700, \"optionExcerciseQty\" : null, \"optionAssignmentQty\" : null, \"allocationTradeQty\" : 0, \"deliveryNoticeQty\" : 0, \"received\" : { \"$date\": \"2016-09-02T08:37:27.771Z\"} }"));
        positionReportJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"member\" : \"ABCFR\", \"account\" : \"A1\", \"reqId\" : null, \"rptId\" : \"13198434645156\", \"bizDt\" : \"2013-12-17\", \"lastReportRequested\" : null, \"settlSesId\" : \"ITD\", \"symbol\" : \"OGBL\", \"putCall\" : \"C\", \"maturityMonthYear\" : \"201401\", \"strikePrice\" : \"10800\", \"optAttribute\" : \"0\", \"crossMarginLongQty\" : 700, \"crossMarginShortQty\" : 800, \"optionExcerciseQty\" : 0, \"optionAssignmentQty\" : 0, \"allocationTradeQty\" : null, \"deliveryNoticeQty\" : null, \"received\" : { \"$date\": \"2016-09-02T08:37:27.771Z\"} }"));
    }

    public final static List<JsonObject> marginComponentJson;
    static {
        marginComponentJson = new ArrayList<>();
        marginComponentJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"member\" : \"DEFFR\", \"account\" : \"PP\", \"clss\" : \"BMW\", \"ccy\" : \"EUR\", \"txnTm\" : { \"$date\" : \"2013-12-18T09:36:00.010Z\" } , \"bizDt\" : \"2013-12-17\", \"reqId\" : null, \"rptId\" : \"13198434644732\", \"sesId\" : \"ITD\", \"variationMargin\" : 0, \"premiumMargin\" : 0, \"liquiMargin\" : 40.1, \"spreadMargin\" : 0, \"additionalMargin\" : 291.9, \"received\" : { \"$date\" : \"2016-09-02T08:36:26.828Z\" } }"));
        marginComponentJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"member\" : \"ABCFR\", \"account\" : \"A1\", \"clss\" : \"BMW\", \"ccy\" : \"EUR\", \"txnTm\" : { \"$date\" : \"2013-12-18T09:36:00.010Z\" } , \"bizDt\" : \"2013-12-17\", \"reqId\" : null, \"rptId\" : \"13198434644734\", \"sesId\" : \"ITD\", \"variationMargin\" : 0, \"premiumMargin\" : 0, \"liquiMargin\" : 600.8, \"spreadMargin\" : 0, \"additionalMargin\" : 3212.3, \"received\" : { \"$date\" : \"2016-09-02T08:36:26.829Z\" } }"));
        marginComponentJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"member\" : \"DEFFR\", \"account\" : \"PP\", \"clss\" : \"BMW\", \"ccy\" : \"EUR\", \"txnTm\" : { \"$date\" : \"2013-12-18T09:37:00.010Z\" } , \"bizDt\" : \"2013-12-17\", \"reqId\" : null, \"rptId\" : \"13198434644732\", \"sesId\" : \"ITD\", \"variationMargin\" : 0, \"premiumMargin\" : 0, \"liquiMargin\" : 41.1, \"spreadMargin\" : 0, \"additionalMargin\" : 281.9, \"received\" : { \"$date\" : \"2016-09-02T08:37:26.828Z\" } }"));
        marginComponentJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"member\" : \"ABCFR\", \"account\" : \"A1\", \"clss\" : \"BMW\", \"ccy\" : \"EUR\", \"txnTm\" : { \"$date\" : \"2013-12-18T09:37:00.010Z\" } , \"bizDt\" : \"2013-12-17\", \"reqId\" : null, \"rptId\" : \"13198434644734\", \"sesId\" : \"ITD\", \"variationMargin\" : 0, \"premiumMargin\" : 0, \"liquiMargin\" : 610.8, \"spreadMargin\" : 0, \"additionalMargin\" : 3222.3, \"received\" : { \"$date\" : \"2016-09-02T08:37:26.829Z\" } }"));
    }

    public final static List<JsonObject> totalMarginRequirementJson;
    static {
        totalMarginRequirementJson = new ArrayList<>();
        totalMarginRequirementJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"pool\" : \"ABCFRDEFFRFULM\", \"member\" : \"DEFFR\", \"account\" : \"PP\", \"ccy\" : \"CHF\", \"txnTm\" : { \"$date\": \"2013-12-18T09:36:00.010Z\"}, \"bizDt\" : \"2013-12-17\", \"reqId\" : null, \"rptId\" : \"13198434644688\", \"sesId\" : \"ITD\", \"unadjustedMargin\" : 15711280, \"adjustedMargin\" : 15711280, \"received\" : { \"$date\": \"2016-09-02T08:36:26.820Z\"} }"));
        totalMarginRequirementJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"pool\" : \"ABCFRDEFM\", \"member\" : \"ABCFR\", \"account\" : \"A1\", \"ccy\" : \"EUR\", \"txnTm\" : { \"$date\": \"2013-12-18T09:36:00.010Z\"}, \"bizDt\" : \"2013-12-17\", \"reqId\" : null, \"rptId\" : \"13198434644690\", \"sesId\" : \"ITD\", \"unadjustedMargin\" : 66958230.2, \"adjustedMargin\" : 66958230.2, \"received\" : { \"$date\": \"2016-09-02T08:36:26.821Z\"} }"));
        totalMarginRequirementJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"pool\" : \"ABCFRDEFFRFULM\", \"member\" : \"DEFFR\", \"account\" : \"PP\", \"ccy\" : \"CHF\", \"txnTm\" : { \"$date\": \"2013-12-18T09:36:00.010Z\"}, \"bizDt\" : \"2013-12-17\", \"reqId\" : null, \"rptId\" : \"131984346446í8\", \"sesId\" : \"ITD\", \"unadjustedMargin\" : 16711280, \"adjustedMargin\" : 16711280, \"received\" : { \"$date\": \"2016-09-02T08:37:26.820Z\"} }"));
        totalMarginRequirementJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"pool\" : \"ABCFRDEFM\", \"member\" : \"ABCFR\", \"account\" : \"A1\", \"ccy\" : \"EUR\", \"txnTm\" : { \"$date\": \"2013-12-18T09:36:00.010Z\"}, \"bizDt\" : \"2013-12-17\", \"reqId\" : null, \"rptId\" : \"13198434644699\", \"sesId\" : \"ITD\", \"unadjustedMargin\" : 66858230.2, \"adjustedMargin\" : 66858230.2, \"received\" : { \"$date\": \"2016-09-02T08:37:26.821Z\"} }"));
    }

    public final static List<JsonObject> marginShortfallSurplusJson;
    static {
        marginShortfallSurplusJson = new ArrayList<>();
        marginShortfallSurplusJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"pool\" : \"ABCFRDEFFRFULM\", \"poolType\" : \"Segregated\", \"member\" : \"DEFFR\", \"clearingCcy\" : \"EUR\", \"ccy\" : \"EUR\", \"txnTm\" : { \"$date\" : \"2013-12-18T14:49:38.700Z\"}, \"bizDt\" : \"2013-12-17\", \"reqId\" : null, \"rptId\" : \"13198434648200\", \"sesId\" : \"ITD\", \"marginRequirement\" : 29655920, \"securityCollateral\" : 4401250, \"cashBalance\" : 0, \"shortfallSurplus\" : -25254670, \"marginCall\" : -20473926.31, \"received\" : { \"$date\" : \"2016-09-02T13:50:05.882Z\"} }"));
        marginShortfallSurplusJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"pool\" : \"ABCFRDEFM\", \"poolType\" : \"Default\", \"member\" : \"ABCFR\", \"clearingCcy\" : \"EUR\", \"ccy\" : \"EUR\", \"txnTm\" : { \"$date\" : \"2013-12-18T14:49:38.700Z\"}, \"bizDt\" : \"2013-12-17\", \"reqId\" : null, \"rptId\" : \"13198434648202\", \"sesId\" : \"ITD\", \"marginRequirement\" : 3575360495.2, \"securityCollateral\" : 12685, \"cashBalance\" : -21990, \"shortfallSurplus\" : -3570369800.2, \"marginCall\" : -3570369800.2, \"received\" : { \"$date\" : \"2016-09-02T13:50:05.882Z\"} }"));
        marginShortfallSurplusJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"pool\" : \"ABCFRDEFFRFULM\", \"poolType\" : \"Segregated\", \"member\" : \"DEFFR\", \"clearingCcy\" : \"EUR\", \"ccy\" : \"EUR\", \"txnTm\" : { \"$date\" : \"2013-12-18T14:49:38.700Z\"}, \"bizDt\" : \"2013-12-17\", \"reqId\" : null, \"rptId\" : \"13198434648210\", \"sesId\" : \"ITD\", \"marginRequirement\" : 29655920, \"securityCollateral\" : 4601250, \"cashBalance\" : 0, \"shortfallSurplus\" : -25854670, \"marginCall\" : -22473926.31, \"received\" : { \"$date\" : \"2016-09-02T13:51:05.882Z\"} }"));
        marginShortfallSurplusJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"pool\" : \"ABCFRDEFM\", \"poolType\" : \"Default\", \"member\" : \"ABCFR\", \"clearingCcy\" : \"EUR\", \"ccy\" : \"EUR\", \"txnTm\" : { \"$date\" : \"2013-12-18T14:49:38.700Z\"}, \"bizDt\" : \"2013-12-17\", \"reqId\" : null, \"rptId\" : \"13198434648212\", \"sesId\" : \"ITD\", \"marginRequirement\" : 3475360495.2, \"securityCollateral\" : 22685, \"cashBalance\" : -31990, \"shortfallSurplus\" : -3575369800.2, \"marginCall\" : -3575369800.2, \"received\" : { \"$date\" : \"2016-09-02T13:51:05.882Z\"} }"));
    }

    public final static List<JsonObject> riskLimitJson;
    static {
        riskLimitJson = new ArrayList<>();
        riskLimitJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"member\" : \"DEFFR\", \"maintainer\" : \"ABCFR\", \"txnTm\" : { \"$date\" : \"2013-12-18T09:36:00.010Z\"}, \"reqId\" : null, \"rptId\" : \"13198434644650\", \"reqRslt\" : \"0\", \"txt\" : null, \"limitType\" : \"CASH\", \"utilization\" : 0, \"warningLevel\" : \"999999999000\", \"throttleLevel\" : \"999999999000\", \"rejectLevel\" : \"999999999000\", \"received\" : { \"$date\" : \"2016-09-02T08:36:26.806Z\"} }"));
        riskLimitJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"member\" : \"ABCFR\", \"maintainer\" : \"ABCFR\", \"txnTm\" : { \"$date\" : \"2013-12-18T09:36:00.010Z\"}, \"reqId\" : null, \"rptId\" : \"13198434644650\", \"reqRslt\" : \"0\", \"txt\" : null, \"limitType\" : \"NDM\", \"utilization\" : 0, \"warningLevel\" : \"999999999000\", \"throttleLevel\" : \"999999999000\", \"rejectLevel\" : \"999999999000\", \"received\" : { \"$date\" : \"2016-09-02T08:36:26.806Z\"} }"));
        riskLimitJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"member\" : \"DEFFR\", \"maintainer\" : \"ABCFR\", \"txnTm\" : { \"$date\" : \"2013-12-18T09:36:00.010Z\"}, \"reqId\" : null, \"rptId\" : \"13198434644750\", \"reqRslt\" : \"0\", \"txt\" : null, \"limitType\" : \"CASH\", \"utilization\" : 1000000, \"warningLevel\" : \"999999999000\", \"throttleLevel\" : \"999999999000\", \"rejectLevel\" : \"999999999000\", \"received\" : { \"$date\" : \"2016-09-02T08:37:26.806Z\"} }"));
        riskLimitJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"member\" : \"ABCFR\", \"maintainer\" : \"ABCFR\", \"txnTm\" : { \"$date\" : \"2013-12-18T09:36:00.010Z\"}, \"reqId\" : null, \"rptId\" : \"13198434644850\", \"reqRslt\" : \"0\", \"txt\" : null, \"limitType\" : \"NDM\", \"utilization\" : 8000000, \"warningLevel\" : \"999999999000\", \"throttleLevel\" : \"999999999000\", \"rejectLevel\" : \"999999999000\", \"received\" : { \"$date\" : \"2016-09-02T08:37:26.806Z\"} }"));
    }
}
