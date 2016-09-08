package com.opnfi.risk.utils;

/**
 * Created by schojak on 8.9.16.
 */
public class DummyData {
    public static String tradingSessionStatusXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><FIXML><TrdgSesStat SesID=\"1\" Stat=\"2\"/></FIXML>";
    public static String positionReportXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><FIXML> <PosRpt BizDt=\"2009-12-16\" ReqTyp=\"7\" RptID=\"13365938226608\" SetSesID=\"ITD\"> <Pty ID=\"ABCFR\" R=\"4\" Src=\"D\"/> <Pty ID=\"DEFFR\" R=\"1\" Src=\"D\"> <Sub ID=\"A1\" Typ=\"26\"/> </Pty> <Instrmt MMY=\"201001\" OptAt=\"1\" PutCall=\"1\" StrkPx=\"003500\" Sym=\"BMW \"/> <Qty Long=\"0\" Short=\"100\" Typ=\"XM\"/> <Qty Long=\"0\" Typ=\"EX\"/> <Qty Short=\"0\" Typ=\"AS\"/> </PosRpt></FIXML>";
}
