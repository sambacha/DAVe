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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.NamingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

@RunWith(VertxUnitRunner.class)
public class MainVerticleIT {
    protected static final DateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    protected static final DateFormat timestampFormatterTimezone = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private static Vertx vertx;
    private static int tcpPort;
    private static int sslPort;
    private static int httpPort;
    private static int mongoPort;

    @BeforeClass
    public static void setUp(TestContext context) {
        vertx = Vertx.vertx();

        httpPort = Integer.getInteger("http.port", 8080);
        tcpPort = Integer.getInteger("ers.tcpport", 5672);
        sslPort = Integer.getInteger("ers.sslport", 5671);
        mongoPort = Integer.getInteger("mongodb.port", 27017);

        JsonObject config = new JsonObject();
        config.put("ers", new JsonArray().add(new JsonObject().put("brokerHost", "localhost").put("brokerPort", sslPort).put("member", "ABCFR").put("sslCertAlias", "abcfr").put("truststore", MainVerticleIT.class.getResource("ers.truststore").getPath()).put("truststorePassword", "123456").put("keystore", MainVerticleIT.class.getResource("ers.keystore").getPath()).put("keystorePassword", "123456")));
        config.put("http", new JsonObject().put("httpPort", httpPort));
        config.put("mongodb", new JsonObject().put("db_name", "OpnFi-Risk-MainVerticleTest").put("connection_string", "mongodb://localhost:" + mongoPort));
        config.put("ersDebugger", new JsonObject().put("enable", true));

        //config.put("masterdata", new JsonObject().put("clearers", new JsonArray()));
        JsonObject clearerABCFR  =new JsonObject().put("clearer", "ABCFR").put("members", new JsonArray().add(new JsonObject().put("member", "ABCFR").put("accounts", new JsonArray().add("A1").add("A2").add("PP"))).add(new JsonObject().put("member", "GHIFR").put("accounts", new JsonArray().add("PP").add("MY"))));
        JsonObject clearerDEFFR = new JsonObject().put("clearer", "DEFFR").put("members", new JsonArray().add(new JsonObject().put("member", "DEFFR").put("accounts", new JsonArray().add("A1").add("A2").add("PP"))));
        config.put("masterdata", new JsonObject().put("clearers", new JsonArray().add(clearerABCFR).add(clearerDEFFR)));

        vertx.deployVerticle(MainVerticle.class.getName(), new DeploymentOptions().setConfig(config), context.asyncAssertSuccess());
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
    public void testPositionReport(TestContext context) throws InterruptedException {
        sendErsBroadcast(context, "ABCFR.MessageType.Position", DummyData.positionReportXML);

        final Async asyncRest = context.async();
        vertx.createHttpClient().getNow(httpPort, "localhost", "/api/v1.0/pr/latest/ABCFR/DEFFR/A1/BMW/C/3500/1/201001", res -> {
            context.assertEquals(res.statusCode(), 200);
            res.bodyHandler(body -> {
                try {
                    JsonArray positions = body.toJsonArray();

                    context.assertEquals(positions.size(), 1);

                    JsonObject pos = positions.getJsonObject(0);

                    context.assertEquals(pos.getString("clearer"), "ABCFR");
                    context.assertEquals(pos.getString("member"), "DEFFR");
                    context.assertNull(pos.getString("reqID"));
                    context.assertEquals(pos.getString("account"), "A1");
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
                    asyncRest.complete();
                }
                catch (Exception e)
                {
                    context.fail(e);
                }
            });
        });
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        MainVerticleIT.vertx.close(context.asyncAssertSuccess());
    }
}
