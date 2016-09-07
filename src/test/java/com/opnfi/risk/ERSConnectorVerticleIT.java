package com.opnfi.risk;

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

@RunWith(VertxUnitRunner.class)
public class ERSConnectorVerticleIT {
    private static Vertx vertx;
    private static int tcpPort;
    private static int sslPort;

    @BeforeClass
    public static void setUp(TestContext context) {
        ERSConnectorVerticleIT.vertx = Vertx.vertx();
        ERSConnectorVerticleIT.tcpPort = Integer.getInteger("ers.tcp_port", 5672);
        ERSConnectorVerticleIT.sslPort = Integer.getInteger("ers.ssl_port", 5671);

        JsonObject config = new JsonObject().put("brokerHost", "localhost").put("brokerPort", sslPort).put("member", "ABCFR").put("sslCertAlias", "abcfr").put("truststore", ERSConnectorVerticleIT.class.getResource("ers.truststore").getPath()).put("truststorePassword", "123456").put("keystore", ERSConnectorVerticleIT.class.getResource("ers.keystore").getPath()).put("keystorePassword", "123456");

        vertx.deployVerticle(ERSConnectorVerticle.class.getName(), new DeploymentOptions().setConfig(config), context.asyncAssertSuccess());
    }

    @Test
    public void testTradingSessionStatus(TestContext test)
    {

    }

    @AfterClass
    public static void tearDown(TestContext context) {
        ERSConnectorVerticleIT.vertx.close(context.asyncAssertSuccess());
    }
}
