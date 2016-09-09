package com.opnfi.risk.utils;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schojak on 8.9.16.
 */
public class DummyData {
    public final static String tradingSessionStatusXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><FIXML><TrdgSesStat SesID=\"1\" Stat=\"2\"/></FIXML>";
    public final static String positionReportXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><FIXML> <PosRpt BizDt=\"2009-12-16\" ReqTyp=\"7\" RptID=\"13365938226608\" SetSesID=\"ITD\"> <Pty ID=\"ABCFR\" R=\"4\" Src=\"D\"/> <Pty ID=\"DEFFR\" R=\"1\" Src=\"D\"> <Sub ID=\"A1\" Typ=\"26\"/> </Pty> <Instrmt MMY=\"201001\" OptAt=\"1\" PutCall=\"1\" StrkPx=\"003500\" Sym=\"BMW \"/> <Qty Long=\"0\" Short=\"100\" Typ=\"XM\"/> <Qty Long=\"0\" Typ=\"EX\"/> <Qty Short=\"0\" Typ=\"AS\"/> </PosRpt></FIXML>";
    public final static String marginComponentXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><FIXML> <MgnReqmtRpt BizDt=\"2009-12-16\" Clss=\"BMW\" RptID=\"13365938226624\" RptTyp=\"1\" SetSesID=\"ITD\" TxnTm=\"2009-12-16T14:46:18.55\"> <Pty ID=\"ABCFR\" R=\"4\" Src=\"D\"/> <Pty ID=\"DEFFR\" R=\"1\" Src=\"D\"> <Sub ID=\"A1\" Typ=\"26\"/> </Pty> <MgnAmt Amt=\"1714286.00\" Ccy=\"EUR\" Typ=\"23\"/> <MgnAmt Amt=\"25539.00\" Ccy=\"EUR\" Typ=\"17\"/> <MgnAmt Amt=\"0.00\" Ccy=\"EUR\" Typ=\"12\"/> <MgnAmt Amt=\"0.00\" Ccy=\"EUR\" Typ=\"10\"/> <MgnAmt Amt=\"20304.00\" Ccy=\"EUR\" Typ=\"1\"/> </MgnReqmtRpt></FIXML>";
    public final static String totalMarginRequirementXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><FIXML> <MgnReqmtRpt BizDt=\"2009-12-16\" RptID=\"13365938226622\" RptTyp=\"0\" SetSesID=\"ITD\" TxnTm=\"2009-12-16T14:46:18.55\"> <Pty ID=\"ABCFR\" R=\"4\" Src=\"D\"> <Sub ID=\"ABCFRDEFM\" Typ=\"4000\"/> </Pty> <Pty ID=\"DEFFR\" R=\"1\" Src=\"D\"> <Sub ID=\"A1\" Typ=\"26\"/> </Pty> <MgnAmt Amt=\"58054385.70\" Ccy=\"EUR\" Typ=\"3\"/> <MgnAmt Amt=\"58054385.70\" Ccy=\"EUR\" Typ=\"2\"/> </MgnReqmtRpt></FIXML>";
    public final static String marginShortfallSurplusXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><FIXML> <MgnReqmtRpt BizDt=\"2009-12-16\" Ccy=\"EUR\" RptID=\"13365938226618\" RptTyp=\"2\" SetSesID=\"ITD\" TxnTm=\"2009-12-16T14:46:18.55\"> <Pty ID=\"ABCFR\" R=\"4\" Src=\"D\"> <Sub ID=\"ABCFRDEFM\" Typ=\"4000\"/> <Sub ID=\"Default\" Typ=\"4001\"/> </Pty> <Pty ID=\"DEFFR\" R=\"1\" Src=\"D\"/> <MgnAmt Amt=\"5656891139.90\" Ccy=\"EUR\" Typ=\"22\"/> <MgnAmt Amt=\"604369.00\" Ccy=\"EUR\" Typ=\"19\"/> <MgnAmt Amt=\"48017035.95\" Ccy=\"EUR\" Typ=\"5\"/> <MgnAmt Amt=\"5603269734.95\" Ccy=\"EUR\" Typ=\"14\"/> <MgnAmt Amt=\"-5603269734.95\" Ccy=\"EUR\" Typ=\"13\"/> </MgnReqmtRpt></FIXML>";
    public final static String riskLimitXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><FIXML> <PtyRiskLmtRpt ReqRslt=\"0\" RptID=\"13365938226620\" TxnTm=\"2009-12-16T14:46:18.55\"> <PtyRiskLmt> <PtyDetl ID=\"ABCFR\" R=\"4\" Src=\"D\"/> <PtyDetl ID=\"DEFFR\" R=\"1\" Src=\"D\"/> <PtyDetl ID=\"ABCFR\" R=\"7\" Src=\"D\"/> <RiskLmt> <RiskLmtTyp Typ=\"4001\" UtilztnAmt=\"2838987418.92\"><WarnLvl Actn=\"4\" Amt=\"1000\"/> <WarnLvl Actn=\"0\" Amt=\"10000\"/> <WarnLvl Actn=\"2\" Amt=\"100000\"/> </RiskLmtTyp> <RiskLmtTyp Typ=\"4004\" UtilztnAmt=\"2480888829.87\"><WarnLvl Actn=\"4\" Amt=\"2000\"/> <WarnLvl Actn=\"0\" Amt=\"20000\"/> <WarnLvl Actn=\"2\" Amt=\"200000\"/> </RiskLmtTyp> </RiskLmt> </PtyRiskLmt> </PtyRiskLmtRpt></FIXML>";

    public final static List<JsonObject> tradingSessionStatusJson;
    static {
        tradingSessionStatusJson = new ArrayList<>();
        tradingSessionStatusJson.add(new JsonObject("{ \"reqId\" : null, \"sesId\" : \"1\", \"stat\" : \"1\", \"statRejRsn\" : null, \"txt\" : null, \"received\" : { \"$date\": \"2016-09-01T11:39:28.088Z\" } }"));
        tradingSessionStatusJson.add(new JsonObject("{ \"reqId\" : null, \"sesId\" : \"1\", \"stat\" : \"2\", \"statRejRsn\" : null, \"txt\" : null, \"received\" : { \"$date\": \"2016-09-01T11:40:28.088Z\" } }"));
    }

    public final static List<JsonObject> marginComponentsJson;
    static {
        marginComponentsJson = new ArrayList<>();
        marginComponentsJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"member\" : \"DEFFR\", \"account\" : \"PP\", \"clss\" : \"BMW\", \"ccy\" : \"EUR\", \"txnTm\" : { \"$date\" : \"2013-12-18T09:36:00.010Z\" } , \"bizDt\" : { \"$date\" : \"2013-12-17T23:00:00Z\"}, \"reqId\" : null, \"rptId\" : \"13198434644732\", \"sesId\" : \"ITD\", \"variationMargin\" : 0, \"premiumMargin\" : 0, \"liquiMargin\" : 40.1, \"spreadMargin\" : 0, \"additionalMargin\" : 291.9, \"received\" : { \"$date\" : \"2016-09-02T08:36:26.828Z\" } }"));
        marginComponentsJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"member\" : \"ABCFR\", \"account\" : \"A1\", \"clss\" : \"BMW\", \"ccy\" : \"EUR\", \"txnTm\" : { \"$date\" : \"2013-12-18T09:36:00.010Z\" } , \"bizDt\" : { \"$date\" : \"2013-12-17T23:00:00Z\"}, \"reqId\" : null, \"rptId\" : \"13198434644734\", \"sesId\" : \"ITD\", \"variationMargin\" : 0, \"premiumMargin\" : 0, \"liquiMargin\" : 600.8, \"spreadMargin\" : 0, \"additionalMargin\" : 3212.3, \"received\" : { \"$date\" : \"2016-09-02T08:36:26.829Z\" } }"));
        marginComponentsJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"member\" : \"DEFFR\", \"account\" : \"PP\", \"clss\" : \"BMW\", \"ccy\" : \"EUR\", \"txnTm\" : { \"$date\" : \"2013-12-18T09:37:00.010Z\" } , \"bizDt\" : { \"$date\" : \"2013-12-17T23:00:00Z\"}, \"reqId\" : null, \"rptId\" : \"13198434644732\", \"sesId\" : \"ITD\", \"variationMargin\" : 0, \"premiumMargin\" : 0, \"liquiMargin\" : 41.1, \"spreadMargin\" : 0, \"additionalMargin\" : 281.9, \"received\" : { \"$date\" : \"2016-09-02T08:37:26.828Z\" } }"));
        marginComponentsJson.add(new JsonObject("{ \"clearer\" : \"ABCFR\", \"member\" : \"ABCFR\", \"account\" : \"A1\", \"clss\" : \"BMW\", \"ccy\" : \"EUR\", \"txnTm\" : { \"$date\" : \"2013-12-18T09:37:00.010Z\" } , \"bizDt\" : { \"$date\" : \"2013-12-17T23:00:00Z\"}, \"reqId\" : null, \"rptId\" : \"13198434644734\", \"sesId\" : \"ITD\", \"variationMargin\" : 0, \"premiumMargin\" : 0, \"liquiMargin\" : 610.8, \"spreadMargin\" : 0, \"additionalMargin\" : 3222.3, \"received\" : { \"$date\" : \"2016-09-02T08:37:26.829Z\" } }"));
    }
}

