package com.opnfi.risk;

import com.opnfi.risk.utils.AutoCloseableConnection;
import com.opnfi.risk.utils.DummyData;
import com.opnfi.risk.utils.Utils;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.NamingException;

@RunWith(VertxUnitRunner.class)
public class ERSConnectorVerticleIT {
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

    @Test
    public void testTradingSessionStatus(TestContext context) throws InterruptedException {
        final Async asyncReceiver = context.async();
        vertx.eventBus().consumer("ers.TradingSessionStatus", msg -> {
            JsonObject tss = (JsonObject)msg.body();

            context.assertEquals(tss.getString("sesId"), "1");
            context.assertEquals(tss.getString("stat"), "2");
            context.assertNull(tss.getString("reqID"));
            asyncReceiver.complete();
        });

        sendErsBroadcast(context, "public.MessageType.TradingSessionStatus", DummyData.tradingSessionStatusXML);
    }

    @Test
    public void testPositionReport(TestContext context) throws InterruptedException {
        final Async asyncReceiver = context.async();
        vertx.eventBus().consumer("ers.PositionReport", msg -> {
            JsonObject pos = (JsonObject)msg.body();

            context.assertEquals(pos.getString("clearer"), "ABCFR");
            context.assertEquals(pos.getString("member"), "DEFFR");
            context.assertNull(pos.getString("reqID"));
            context.assertEquals(pos.getString("account"), "A1");
            context.assertEquals(pos.getJsonObject("bizDt"), new JsonObject().put("$date", "2009-12-16T00:00:00.000+01:00"));
            context.assertEquals(pos.getString("settlSesId"), "ITD");
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
        });

        sendErsBroadcast(context, "ABCFR.MessageType.Position", DummyData.positionReportXML);
    }

    @Test
    public void testMarginComponent(TestContext context) throws InterruptedException {
        final Async asyncReceiver = context.async();
        vertx.eventBus().consumer("ers.MarginComponent", msg -> {
            JsonObject pos = (JsonObject)msg.body();

            context.assertEquals(pos.getString("clearer"), "ABCFR");
            context.assertEquals(pos.getString("member"), "DEFFR");
            context.assertNull(pos.getString("reqID"));
            context.assertEquals(pos.getString("account"), "A1");
            context.assertEquals(pos.getJsonObject("bizDt"), new JsonObject().put("$date", "2009-12-16T00:00:00.000+01:00"));
            context.assertEquals(pos.getJsonObject("txnTm"), new JsonObject().put("$date", "2009-12-16T14:46:18.550+01:00"));
            context.assertEquals(pos.getString("sesId"), "ITD");
            context.assertEquals(pos.getString("clss"), "BMW");
            context.assertEquals(pos.getString("ccy"), "EUR");
            context.assertEquals(pos.getString("rptId"), "13365938226624");
            context.assertEquals(pos.getDouble("variationMargin"), 1714286.0);
            context.assertEquals(pos.getDouble("premiumMargin"), 25539.0);
            context.assertEquals(pos.getDouble("liquiMargin"), 0.0);
            context.assertEquals(pos.getDouble("spreadMargin"), 0.0);
            context.assertEquals(pos.getDouble("additionalMargin"), 20304.0);
            asyncReceiver.complete();
        });

        sendErsBroadcast(context, "ABCFR.MessageType.MarginComponents", DummyData.marginComponentXML);
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        ERSConnectorVerticleIT.vertx.close(context.asyncAssertSuccess());
    }
}
