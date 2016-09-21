package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.utils.AutoCloseableConnection;
import com.deutscheboerse.risk.dave.utils.DummyData;
import com.deutscheboerse.risk.dave.utils.Utils;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.*;
import javax.naming.NamingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(VertxUnitRunner.class)
public class ERSConnectorVerticleIT {
    protected static final DateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    protected static final DateFormat timestampFormatterTimezone = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private static Vertx vertx;
    private static int tcpPort;
    private static int sslPort;

    @BeforeClass
    public static void setUp(TestContext context) {
        vertx = Vertx.vertx();

        tcpPort = Integer.getInteger("ers.tcpport", 5672);
        sslPort = Integer.getInteger("ers.sslport", 5671);

        emptyRequestQueue(context);

        JsonObject config = new JsonObject().put("brokerHost", "localhost").put("brokerPort", sslPort).put("member", "ABCFR").put("sslCertAlias", "abcfr").put("truststore", ERSConnectorVerticleIT.class.getResource("ers.truststore").getPath()).put("truststorePassword", "123456").put("keystore", ERSConnectorVerticleIT.class.getResource("ers.keystore").getPath()).put("keystorePassword", "123456");
        vertx.deployVerticle(ERSConnectorVerticle.class.getName(), new DeploymentOptions().setConfig(config), context.asyncAssertSuccess());

        // Deploy also the MasterDataVerticle which is a dependency for ERSVerticle
        JsonObject clearerABCFR = new JsonObject().put("clearer", "ABCFR").put("members", new JsonArray().add(new JsonObject().put("member", "ABCFR").put("accounts", new JsonArray().add("A1").add("A2").add("PP"))).add(new JsonObject().put("member", "GHIFR").put("accounts", new JsonArray().add("PP").add("MY"))));
        JsonObject clearerDEFFR = new JsonObject().put("clearer", "DEFFR").put("members", new JsonArray().add(new JsonObject().put("member", "DEFFR").put("accounts", new JsonArray().add("A1").add("A2").add("PP"))));
        JsonObject mdConfig = new JsonObject().put("clearers", new JsonArray().add(clearerABCFR).add(clearerDEFFR));

        DeploymentOptions options = new DeploymentOptions().setConfig(mdConfig);
        vertx.deployVerticle(MasterdataVerticle.class.getName(), options, context.asyncAssertSuccess());
    }

    private static void emptyRequestQueue(TestContext context)
    {
        Async asyncRequest = context.async();

        vertx.executeBlocking(future -> {
            try {
                Utils utils = new Utils();
                AutoCloseableConnection conn = utils.getAdminConnection("localhost", tcpPort);
                Session ses = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                conn.start();
                MessageConsumer consumer = ses.createConsumer(utils.getQueue("eurex.request.ABCFR"));

                while (true) {
                    Message msg = consumer.receive(1000);

                    if (msg != null) {
                        msg.acknowledge();
                    } else {
                        break;
                    }
                }

                consumer.close();
                ses.close();
                conn.close();

                future.complete();
            }
            catch (NamingException | JMSException e)
            {
                System.out.println("I got an error: " + e.toString());
                future.fail(e);
            }
        }, res -> {
            if (res.succeeded())
            {
                asyncRequest.complete();
            }
            else
            {
                context.fail("Failed to receive request messages");
            }
        });

        asyncRequest.awaitSuccess();
    }

    private void sendErsBroadcast(TestContext context, String routingKey, String messageBody) {
        final Async asyncBroadcast = context.async();

        vertx.executeBlocking(future -> {
            try {
                Utils utils = new Utils();
                AutoCloseableConnection conn = utils.getAdminConnection("localhost", tcpPort);
                Session ses = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                MessageProducer prod = ses.createProducer(utils.getTopic("eurex.broadcast/" + routingKey));
                prod.send(ses.createTextMessage(messageBody));

                future.complete();
            }
            catch (NamingException | JMSException e)
            {
                future.fail(e);
            }
        }, res -> {
            if (res.succeeded())
            {
                asyncBroadcast.complete();
            }
            else
            {
                context.fail("Failed to send broadcast message");
            }
        });

        asyncBroadcast.awaitSuccess();
    }

    private String getMessagePayloadAsString(Message msg) throws JMSException {
        if (msg instanceof TextMessage) {
            return ((TextMessage)msg).getText();
        }
        else if (msg instanceof BytesMessage)
        {
            BytesMessage byteMsg = (BytesMessage)msg;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < byteMsg.getBodyLength(); i++)
            {
                builder.append((char)byteMsg.readByte());
            }

            byteMsg.reset();
            return builder.toString();
        }

        return null;
    }

    @Test
    public void testInitialRequests(TestContext context) throws JMSException {
        Map<String, List<Message>> messages = new HashMap<>();

        // Collect request messages
        final Async asyncRequests = context.async();

        vertx.executeBlocking(future -> {
            try {
                Utils utils = new Utils();
                AutoCloseableConnection conn = utils.getAdminConnection("localhost", tcpPort);
                Session ses = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                conn.start();
                MessageConsumer consumer = ses.createConsumer(utils.getQueue("eurex.request.ABCFR"));

                while (true)
                {
                    Message msg = consumer.receive(1000);

                    if (msg != null) {
                        //System.out.println("Got message ... " + getMessagePayloadAsString(msg));

                        if (getMessagePayloadAsString(msg).contains("TrdgSesStatReq")) {
                            if (messages.get("tss") != null)
                            {
                                messages.get("tss").add(msg);
                            }
                            else
                            {
                                List<Message> list = new ArrayList<Message>();
                                list.add(msg);
                                messages.put("tss", list);
                            }
                        }
                        else if (getMessagePayloadAsString(msg).contains("MgnReqmtInq") && getMessagePayloadAsString(msg).contains("Qual=\"0\"")) {
                            if (messages.get("tmr") != null)
                            {
                                messages.get("tmr").add(msg);
                            }
                            else
                            {
                                List<Message> list = new ArrayList<Message>();
                                list.add(msg);
                                messages.put("tmr", list);
                            }
                        }
                        else if (getMessagePayloadAsString(msg).contains("MgnReqmtInq") && getMessagePayloadAsString(msg).contains("Qual=\"2\"")) {
                            if (messages.get("mss") != null)
                            {
                                messages.get("mss").add(msg);
                            }
                            else
                            {
                                List<Message> list = new ArrayList<Message>();
                                list.add(msg);
                                messages.put("mss", list);
                            }
                        }
                        else if (getMessagePayloadAsString(msg).contains("PtyRiskLmtReq")) {
                            if (messages.get("rl") != null)
                            {
                                messages.get("rl").add(msg);
                            }
                            else
                            {
                                List<Message> list = new ArrayList<Message>();
                                list.add(msg);
                                messages.put("rl", list);
                            }
                        }

                        msg.acknowledge();
                    }
                    else
                    {
                        break;
                    }
                }

                consumer.close();
                ses.close();
                conn.close();

                future.complete();
            }
            catch (NamingException | JMSException e)
            {
                System.out.println("I got an error: " + e.toString());
                future.fail(e);
            }
        }, res -> {
            if (res.succeeded())
            {
                asyncRequests.complete();
            }
            else
            {
                context.fail("Failed to receive request messages");
            }
        });

        asyncRequests.awaitSuccess();

        // TSS
        context.assertNotNull(messages.get("tss"));
        context.assertEquals(1, messages.get("tss").size());
        context.assertTrue(messages.get("tss").get(0).getJMSReplyTo().toString().contains("eurex.response"));
        context.assertTrue(messages.get("tss").get(0).getJMSReplyTo().toString().contains("ABCFR.TradingSessionStatus"));
        context.assertTrue(getMessagePayloadAsString(messages.get("tss").get(0)).contains("SubReqTyp=\"0\""));

        // TMR
        context.assertNotNull(messages.get("tmr"));
        context.assertEquals(5, messages.get("tmr").size());
        context.assertTrue(messages.get("tmr").get(0).getJMSReplyTo().toString().contains("eurex.response"));
        context.assertTrue(messages.get("tmr").get(0).getJMSReplyTo().toString().contains("ABCFR.TotalMarginRequirement"));
        context.assertTrue(getMessagePayloadAsString(messages.get("tmr").get(0)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"4\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("tmr").get(0)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"1\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("tmr").get(0)).contains("Sub ID=\"A1\" Typ=\"26\""));
        context.assertTrue(messages.get("tmr").get(1).getJMSReplyTo().toString().contains("eurex.response"));
        context.assertTrue(messages.get("tmr").get(1).getJMSReplyTo().toString().contains("ABCFR.TotalMarginRequirement"));
        context.assertTrue(getMessagePayloadAsString(messages.get("tmr").get(1)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"4\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("tmr").get(1)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"1\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("tmr").get(1)).contains("Sub ID=\"A2\" Typ=\"26\""));
        context.assertTrue(messages.get("tmr").get(2).getJMSReplyTo().toString().contains("eurex.response"));
        context.assertTrue(messages.get("tmr").get(2).getJMSReplyTo().toString().contains("ABCFR.TotalMarginRequirement"));
        context.assertTrue(getMessagePayloadAsString(messages.get("tmr").get(2)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"4\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("tmr").get(2)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"1\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("tmr").get(2)).contains("Sub ID=\"PP\" Typ=\"26\""));
        context.assertTrue(messages.get("tmr").get(3).getJMSReplyTo().toString().contains("eurex.response"));
        context.assertTrue(messages.get("tmr").get(3).getJMSReplyTo().toString().contains("ABCFR.TotalMarginRequirement"));
        context.assertTrue(getMessagePayloadAsString(messages.get("tmr").get(3)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"4\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("tmr").get(3)).contains("Pty ID=\"GHIFR\" Src=\"D\" R=\"1\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("tmr").get(3)).contains("Sub ID=\"PP\" Typ=\"26\""));
        context.assertTrue(messages.get("tmr").get(4).getJMSReplyTo().toString().contains("eurex.response"));
        context.assertTrue(messages.get("tmr").get(4).getJMSReplyTo().toString().contains("ABCFR.TotalMarginRequirement"));
        context.assertTrue(getMessagePayloadAsString(messages.get("tmr").get(4)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"4\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("tmr").get(4)).contains("Pty ID=\"GHIFR\" Src=\"D\" R=\"1\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("tmr").get(4)).contains("Sub ID=\"MY\" Typ=\"26\""));
        
        // MSS
        context.assertNotNull(messages.get("mss"));
        context.assertEquals(2, messages.get("mss").size());
        context.assertTrue(messages.get("mss").get(0).getJMSReplyTo().toString().contains("eurex.response"));
        context.assertTrue(messages.get("mss").get(0).getJMSReplyTo().toString().contains("ABCFR.MarginShortfallSurplus"));
        context.assertTrue(getMessagePayloadAsString(messages.get("mss").get(0)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"4\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("mss").get(0)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"1\""));
        context.assertTrue(messages.get("mss").get(1).getJMSReplyTo().toString().contains("eurex.response"));
        context.assertTrue(messages.get("mss").get(1).getJMSReplyTo().toString().contains("ABCFR.MarginShortfallSurplus"));
        context.assertTrue(getMessagePayloadAsString(messages.get("mss").get(1)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"4\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("mss").get(1)).contains("Pty ID=\"GHIFR\" Src=\"D\" R=\"1\""));

        // RL
        context.assertNotNull(messages.get("rl"));
        context.assertEquals(3, messages.get("rl").size());
        context.assertTrue(messages.get("rl").get(0).getJMSReplyTo().toString().contains("eurex.response"));
        context.assertTrue(messages.get("rl").get(0).getJMSReplyTo().toString().contains("ABCFR.RiskLimits"));
        context.assertTrue(getMessagePayloadAsString(messages.get("rl").get(0)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"4\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("rl").get(0)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"1\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("rl").get(0)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"7\""));
        context.assertTrue(messages.get("rl").get(1).getJMSReplyTo().toString().contains("eurex.response"));
        context.assertTrue(messages.get("rl").get(1).getJMSReplyTo().toString().contains("ABCFR.RiskLimits"));
        context.assertTrue(getMessagePayloadAsString(messages.get("rl").get(1)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"4\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("rl").get(1)).contains("Pty ID=\"GHIFR\" Src=\"D\" R=\"1\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("rl").get(1)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"7\""));
        context.assertTrue(messages.get("rl").get(2).getJMSReplyTo().toString().contains("eurex.response"));
        context.assertTrue(messages.get("rl").get(2).getJMSReplyTo().toString().contains("ABCFR.RiskLimits"));
        context.assertTrue(getMessagePayloadAsString(messages.get("rl").get(2)).contains("Pty ID=\"ABCFR\" Src=\"D\" R=\"4\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("rl").get(2)).contains("Pty ID=\"GHIFR\" Src=\"D\" R=\"1\""));
        context.assertTrue(getMessagePayloadAsString(messages.get("rl").get(2)).contains("Pty ID=\"GHIFR\" Src=\"D\" R=\"7\""));
    }

    @Test
    public void testTradingSessionStatus(TestContext context) throws InterruptedException {
        final Async asyncReceiver = context.async();
        vertx.eventBus().consumer("ers.TradingSessionStatus", msg -> {
            try
            {
                JsonObject tss = (JsonObject)msg.body();

                context.assertEquals("1", tss.getString("sesId"));
                context.assertEquals("2", tss.getString("stat"));
                context.assertNull(tss.getString("reqID"));
                asyncReceiver.complete();
            }
                catch (Exception e)
            {
                context.fail(e);
            }
        });

        sendErsBroadcast(context, "public.MessageType.TradingSessionStatus", DummyData.tradingSessionStatusXML);
    }

    @Test
    public void testPositionReport(TestContext context) throws InterruptedException {
        final Async asyncReceiver = context.async();
        vertx.eventBus().consumer("ers.PositionReport", msg -> {
            try {
                JsonObject pos = (JsonObject) msg.body();

                context.assertEquals("ABCFR", pos.getString("clearer"));
                context.assertEquals("DEFFR", pos.getString("member"));
                context.assertNull(pos.getString("reqID"));
                context.assertEquals("A1", pos.getString("account"));
                context.assertEquals(new JsonObject().put("$date", timestampFormatterTimezone.format(timestampFormatter.parse("2009-12-16T00:00:00.000"))), pos.getJsonObject("bizDt"));
                context.assertEquals("ITD", pos.getString("sesId"));
                context.assertEquals("13365938226608", pos.getString("rptId"));
                context.assertEquals("C", pos.getString("putCall"));
                context.assertEquals("201001", pos.getString("maturityMonthYear"));
                context.assertEquals("3500", pos.getString("strikePrice"));
                context.assertEquals("BMW", pos.getString("symbol"));
                context.assertEquals(0.0, pos.getDouble("crossMarginLongQty"));
                context.assertEquals(100.0, pos.getDouble("crossMarginShortQty"));
                context.assertEquals(0.0, pos.getDouble("optionExcerciseQty"));
                context.assertEquals(0.0, pos.getDouble("optionAssignmentQty"));
                context.assertNull(pos.getDouble("allocationTradeQty"));
                context.assertNull(pos.getDouble("deliveryNoticeQty"));
                asyncReceiver.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        sendErsBroadcast(context, "ABCFR.MessageType.Position", DummyData.positionReportXML);
    }

    @Test
    public void testMarginComponent(TestContext context) throws InterruptedException {
        final Async asyncReceiver = context.async();
        vertx.eventBus().consumer("ers.MarginComponent", msg -> {
            try
            {
                JsonObject mc = (JsonObject)msg.body();

                context.assertEquals("ABCFR", mc.getString("clearer"));
                context.assertEquals("DEFFR", mc.getString("member"));
                context.assertNull(mc.getString("reqID"));
                context.assertEquals("A1", mc.getString("account"));
                context.assertEquals(new JsonObject().put("$date", timestampFormatterTimezone.format(timestampFormatter.parse("2009-12-16T00:00:00.000"))), mc.getJsonObject("bizDt"));
                context.assertEquals(new JsonObject().put("$date", timestampFormatterTimezone.format(timestampFormatter.parse("2009-12-16T14:46:18.550+01:00"))), mc.getJsonObject("txnTm"));
                context.assertEquals("ITD", mc.getString("sesId"));
                context.assertEquals("BMW", mc.getString("clss"));
                context.assertEquals("EUR", mc.getString("ccy"));
                context.assertEquals("13365938226624", mc.getString("rptId"));
                context.assertEquals(1714286.0, mc.getDouble("variationMargin"));
                context.assertEquals(25539.0, mc.getDouble("premiumMargin"));
                context.assertEquals(0.0, mc.getDouble("liquiMargin"));
                context.assertEquals(0.0, mc.getDouble("spreadMargin"));
                context.assertEquals(20304.0, mc.getDouble("additionalMargin"));
                asyncReceiver.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        sendErsBroadcast(context, "ABCFR.MessageType.MarginComponents", DummyData.marginComponentXML);
    }

    @Test
    public void testTotalMarginRequirement(TestContext context) throws InterruptedException {
        final Async asyncReceiver = context.async();
        vertx.eventBus().consumer("ers.TotalMarginRequirement", msg -> {
            try {
                JsonObject tmr = (JsonObject) msg.body();

                context.assertEquals("ABCFR", tmr.getString("clearer"));
                context.assertEquals("DEFFR", tmr.getString("member"));
                context.assertNull(tmr.getString("reqID"));
                context.assertEquals("A1", tmr.getString("account"));
                context.assertEquals("ABCFRDEFM", tmr.getString("pool"));
                context.assertEquals(new JsonObject().put("$date", timestampFormatterTimezone.format(timestampFormatter.parse("2009-12-16T00:00:00.000"))), tmr.getJsonObject("bizDt"));
                context.assertEquals(new JsonObject().put("$date", timestampFormatterTimezone.format(timestampFormatter.parse("2009-12-16T14:46:18.550+01:00"))), tmr.getJsonObject("txnTm"));
                context.assertEquals("ITD", tmr.getString("sesId"));
                context.assertEquals("EUR", tmr.getString("ccy"));
                context.assertEquals("13365938226622", tmr.getString("rptId"));
                context.assertEquals(58054385.7, tmr.getDouble("adjustedMargin"));
                context.assertEquals(58054385.7, tmr.getDouble("unadjustedMargin"));
                asyncReceiver.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        sendErsBroadcast(context, "ABCFR.MessageType.TotalMarginRequirement", DummyData.totalMarginRequirementXML);
    }

    @Test
    public void testMarginShortfallSurplus(TestContext context) throws InterruptedException {
        final Async asyncReceiver = context.async();
        vertx.eventBus().consumer("ers.MarginShortfallSurplus", msg -> {
            try
            {
                JsonObject mss = (JsonObject)msg.body();

                context.assertEquals("ABCFR", mss.getString("clearer"));
                context.assertEquals("DEFFR", mss.getString("member"));
                context.assertNull(mss.getString("reqID"));
                context.assertEquals("ABCFRDEFM", mss.getString("pool"));
                context.assertEquals("Default", mss.getString("poolType"));
                context.assertEquals(new JsonObject().put("$date", timestampFormatterTimezone.format(timestampFormatter.parse("2009-12-16T00:00:00.000"))), mss.getJsonObject("bizDt"));
                context.assertEquals(new JsonObject().put("$date", timestampFormatterTimezone.format(timestampFormatter.parse("2009-12-16T14:46:18.550+01:00"))), mss.getJsonObject("txnTm"));
                context.assertEquals("ITD", mss.getString("sesId"));
                context.assertEquals("CHF", mss.getString("ccy"));
                context.assertEquals("EUR", mss.getString("clearingCcy"));
                context.assertEquals("13365938226618", mss.getString("rptId"));
                context.assertEquals(5656891139.9, mss.getDouble("marginRequirement"));
                context.assertEquals(604369.0, mss.getDouble("securityCollateral"));
                context.assertEquals(48017035.95, mss.getDouble("cashBalance"));
                context.assertEquals(-5603269734.95, mss.getDouble("shortfallSurplus"));
                context.assertEquals(-5603269734.95, mss.getDouble("marginCall"));
                asyncReceiver.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        sendErsBroadcast(context, "ABCFR.MessageType.MarginShortfallSurplus", DummyData.marginShortfallSurplusXML);
    }

    @Test
    public void testRiskLimit(TestContext context) throws InterruptedException {
        final Async asyncReceiver = context.async(2);
        vertx.eventBus().consumer("ers.RiskLimit", msg -> {
            try
            {
                JsonObject rl = (JsonObject)msg.body();
                context.assertEquals("ABCFR", rl.getString("clearer"));
                context.assertEquals("DEFFR", rl.getString("member"));
                context.assertEquals("ABCFR", rl.getString("maintainer"));
                context.assertNull(rl.getString("reqID"));
                context.assertEquals("0", rl.getString("reqRslt"));
                context.assertNull(rl.getString("txt"));
                context.assertEquals(new JsonObject().put("$date", timestampFormatterTimezone.format(timestampFormatter.parse("2009-12-16T14:46:18.550+01:00"))), rl.getJsonObject("txnTm"));
                context.assertEquals("13365938226620", rl.getString("rptId"));

                switch (rl.getString("limitType"))
                {
                    case "TMR":
                        context.assertEquals(2838987418.92, rl.getDouble("utilization"));
                        context.assertEquals(1000.0, rl.getDouble("warningLevel"));
                        context.assertEquals(10000.0, rl.getDouble("throttleLevel"));
                        context.assertEquals(100000.0, rl.getDouble("rejectLevel"));
                        break;
                    case "NDM":
                        context.assertEquals(2480888829.87, rl.getDouble("utilization"));
                        context.assertEquals(2000.0, rl.getDouble("warningLevel"));
                        context.assertEquals(20000.0, rl.getDouble("throttleLevel"));
                        context.assertEquals(200000.0, rl.getDouble("rejectLevel"));
                        break;
                    default:
                        context.fail("Got unexpected limit type!");
                }

                asyncReceiver.countDown();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        sendErsBroadcast(context, "ABCFR.MessageType.RiskLimits", DummyData.riskLimitXML);
        asyncReceiver.awaitSuccess();
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        ERSConnectorVerticleIT.vertx.close(context.asyncAssertSuccess());
    }
}
