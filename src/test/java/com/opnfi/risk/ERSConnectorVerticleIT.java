package com.opnfi.risk;

import com.opnfi.risk.utils.AutoCloseableConnection;
import com.opnfi.risk.utils.DummyData;
import com.opnfi.risk.utils.Utils;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.camel.component.jms.JmsMessage;
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

        JsonObject config = new JsonObject().put("brokerHost", "localhost").put("brokerPort", sslPort).put("member", "ABCFR").put("sslCertAlias", "abcfr").put("truststore", ERSConnectorVerticleIT.class.getResource("ers.truststore").getPath()).put("truststorePassword", "123456").put("keystore", ERSConnectorVerticleIT.class.getResource("ers.keystore").getPath()).put("keystorePassword", "123456");
        vertx.deployVerticle(ERSConnectorVerticle.class.getName(), new DeploymentOptions().setConfig(config), context.asyncAssertSuccess());
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
        Map<String, Message> messages = new HashMap<>();

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
                        if (getMessagePayloadAsString(msg).contains("TrdgSesStatReq")) {
                            messages.put("tss", msg);
                        }

                        msg.acknowledge();
                    }
                    else
                    {
                        break;
                    }
                }

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
        context.assertTrue(messages.get("tss").getJMSReplyTo().toString().contains("eurex.response"));
        context.assertTrue(messages.get("tss").getJMSReplyTo().toString().contains("ABCFR.TradingSessionStatus"));
        context.assertTrue(getMessagePayloadAsString(messages.get("tss")).contains("SubReqTyp=\"0\""));
    }

    @Test
    public void testTradingSessionStatus(TestContext context) throws InterruptedException {
        final Async asyncReceiver = context.async();
        vertx.eventBus().consumer("ers.TradingSessionStatus", msg -> {
            try
            {
                JsonObject tss = (JsonObject)msg.body();

                context.assertEquals(tss.getString("sesId"), "1");
                context.assertEquals(tss.getString("stat"), "2");
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

                context.assertEquals(pos.getString("clearer"), "ABCFR");
                context.assertEquals(pos.getString("member"), "DEFFR");
                context.assertNull(pos.getString("reqID"));
                context.assertEquals(pos.getString("account"), "A1");
                context.assertEquals(pos.getJsonObject("bizDt"), new JsonObject().put("$date", timestampFormatterTimezone.format(timestampFormatter.parse("2009-12-16T00:00:00.000"))));
                context.assertEquals(pos.getString("sesId"), "ITD");
                context.assertEquals(pos.getString("rptId"), "13365938226608");
                context.assertEquals(pos.getString("putCall"), "C");
                context.assertEquals(pos.getString("maturityMonthYear"), "201001");
                context.assertEquals(pos.getString("strikePrice"), "3500");
                context.assertEquals(pos.getString("symbol"), "BMW");
                context.assertEquals(pos.getDouble("crossMarginLongQty"), 0.0);
                context.assertEquals(pos.getDouble("crossMarginShortQty"), 100.0);
                context.assertEquals(pos.getDouble("optionExcerciseQty"), 0.0);
                context.assertEquals(pos.getDouble("optionAssignmentQty"), 0.0);
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

                context.assertEquals(mc.getString("clearer"), "ABCFR");
                context.assertEquals(mc.getString("member"), "DEFFR");
                context.assertNull(mc.getString("reqID"));
                context.assertEquals(mc.getString("account"), "A1");
                context.assertEquals(mc.getJsonObject("bizDt"), new JsonObject().put("$date", timestampFormatterTimezone.format(timestampFormatter.parse("2009-12-16T00:00:00.000"))));
                context.assertEquals(mc.getJsonObject("txnTm"), new JsonObject().put("$date", timestampFormatterTimezone.format(timestampFormatter.parse("2009-12-16T14:46:18.550+01:00"))));
                context.assertEquals(mc.getString("sesId"), "ITD");
                context.assertEquals(mc.getString("clss"), "BMW");
                context.assertEquals(mc.getString("ccy"), "EUR");
                context.assertEquals(mc.getString("rptId"), "13365938226624");
                context.assertEquals(mc.getDouble("variationMargin"), 1714286.0);
                context.assertEquals(mc.getDouble("premiumMargin"), 25539.0);
                context.assertEquals(mc.getDouble("liquiMargin"), 0.0);
                context.assertEquals(mc.getDouble("spreadMargin"), 0.0);
                context.assertEquals(mc.getDouble("additionalMargin"), 20304.0);
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

                context.assertEquals(tmr.getString("clearer"), "ABCFR");
                context.assertEquals(tmr.getString("member"), "DEFFR");
                context.assertNull(tmr.getString("reqID"));
                context.assertEquals(tmr.getString("account"), "A1");
                context.assertEquals(tmr.getString("pool"), "ABCFRDEFM");
                context.assertEquals(tmr.getJsonObject("bizDt"), new JsonObject().put("$date", timestampFormatterTimezone.format(timestampFormatter.parse("2009-12-16T00:00:00.000"))));
                context.assertEquals(tmr.getJsonObject("txnTm"), new JsonObject().put("$date", timestampFormatterTimezone.format(timestampFormatter.parse("2009-12-16T14:46:18.550+01:00"))));
                context.assertEquals(tmr.getString("sesId"), "ITD");
                context.assertEquals(tmr.getString("ccy"), "EUR");
                context.assertEquals(tmr.getString("rptId"), "13365938226622");
                context.assertEquals(tmr.getDouble("adjustedMargin"), 58054385.7);
                context.assertEquals(tmr.getDouble("unadjustedMargin"), 58054385.7);
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

                context.assertEquals(mss.getString("clearer"), "ABCFR");
                context.assertEquals(mss.getString("member"), "DEFFR");
                context.assertNull(mss.getString("reqID"));
                context.assertEquals(mss.getString("pool"), "ABCFRDEFM");
                context.assertEquals(mss.getString("poolType"), "Default");
                context.assertEquals(mss.getJsonObject("bizDt"), new JsonObject().put("$date", timestampFormatterTimezone.format(timestampFormatter.parse("2009-12-16T00:00:00.000"))));
                context.assertEquals(mss.getJsonObject("txnTm"), new JsonObject().put("$date", timestampFormatterTimezone.format(timestampFormatter.parse("2009-12-16T14:46:18.550+01:00"))));
                context.assertEquals(mss.getString("sesId"), "ITD");
                context.assertEquals(mss.getString("ccy"), "CHF");
                context.assertEquals(mss.getString("clearingCcy"), "EUR");
                context.assertEquals(mss.getString("rptId"), "13365938226618");
                context.assertEquals(mss.getDouble("marginRequirement"), 5656891139.9);
                context.assertEquals(mss.getDouble("securityCollateral"), 604369.0);
                context.assertEquals(mss.getDouble("cashBalance"), 48017035.95);
                context.assertEquals(mss.getDouble("shortfallSurplus"), -5603269734.95);
                context.assertEquals(mss.getDouble("marginCall"), -5603269734.95);
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
        final Async asyncReceiver = context.async();
        vertx.eventBus().consumer("ers.RiskLimit", msg -> {
            try
            {
                JsonArray limits = (JsonArray)msg.body();
                context.assertEquals(limits.size(), 2);

                for (Object member : limits.getList()) {
                    JsonObject rl = (JsonObject)member;

                    context.assertEquals(rl.getString("clearer"), "ABCFR");
                    context.assertEquals(rl.getString("member"), "DEFFR");
                    context.assertEquals(rl.getString("maintainer"), "ABCFR");
                    context.assertNull(rl.getString("reqID"));
                    context.assertEquals(rl.getString("reqRslt"), "0");
                    context.assertNull(rl.getString("txt"));
                    context.assertEquals(rl.getJsonObject("txnTm"), new JsonObject().put("$date", timestampFormatterTimezone.format(timestampFormatter.parse("2009-12-16T14:46:18.550+01:00"))));
                    context.assertEquals(rl.getString("rptId"), "13365938226620");

                    switch (rl.getString("limitType"))
                    {
                        case "TMR":
                            context.assertEquals(rl.getDouble("utilization"), 2838987418.92);
                            context.assertEquals(rl.getDouble("warningLevel"), 1000.0);
                            context.assertEquals(rl.getDouble("throttleLevel"), 10000.0);
                            context.assertEquals(rl.getDouble("rejectLevel"), 100000.0);
                            break;
                        case "NDM":
                            context.assertEquals(rl.getDouble("utilization"), 2480888829.87);
                            context.assertEquals(rl.getDouble("warningLevel"), 2000.0);
                            context.assertEquals(rl.getDouble("throttleLevel"), 20000.0);
                            context.assertEquals(rl.getDouble("rejectLevel"), 200000.0);
                            break;
                        default:
                            context.fail("Got unexpected limit type!");
                    }
                }

                asyncReceiver.complete();
            }
            catch (Exception e)
            {
                context.fail(e);
            }
        });

        sendErsBroadcast(context, "ABCFR.MessageType.RiskLimits", DummyData.riskLimitXML);
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        ERSConnectorVerticleIT.vertx.close(context.asyncAssertSuccess());
    }
}
