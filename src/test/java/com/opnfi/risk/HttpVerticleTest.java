package com.opnfi.risk;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by jakub on 03.09.16.
 */
@RunWith(VertxUnitRunner.class)
public class HttpVerticleTest {
    private static Vertx vertx;
    private static int port;

    @BeforeClass
    public static void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();

        // Get some free port
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
    }

    @Test
    public void testPlainHttp(TestContext context) {
        final Async asyncStart = context.async();

        JsonObject config = new JsonObject().put("httpPort", port);
        vertx.deployVerticle(HttpVerticle.class.getName(), new DeploymentOptions().setConfig(config), res -> {
            if (res.succeeded()) {
                asyncStart.complete();
            }
            else
            {
                context.fail(res.cause());
            }
        });

        asyncStart.awaitSuccess();
        final Async asyncClient = context.async();

        vertx.createHttpClient().getNow(port, "localhost", "/api/v1.0/user/loginStatus", res -> {
            context.assertEquals(res.statusCode(), 200);
            res.headers().forEach(v -> {System.out.println("HEaders: " + " = " + v);});res.headers().forEach(v -> {System.out.println("HEaders: " + " = " + v);});
            asyncClient.complete();
        });
    }

    @Test
    public void testCORS(TestContext context) {
        final Async asyncStart = context.async();

        JsonObject config = new JsonObject().put("httpPort", port).put("CORS", new JsonObject().put("enable", true).put("origin", "https://localhost:8888"));

        vertx.deployVerticle(HttpVerticle.class.getName(), new DeploymentOptions().setConfig(config), res -> {
            if (res.succeeded()) {
                asyncStart.complete();
            }
            else
            {
                context.fail(res.cause());
            }
        });

        asyncStart.awaitSuccess();
        final Async asyncClient = context.async();

        String myOrigin = "https://localhost:8888";

        vertx.createHttpClient().get(port, "localhost", "/api/v1.0/user/loginStatus", res -> {
            context.assertEquals(res.statusCode(), 200);
            context.assertEquals(res.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), myOrigin);
            asyncClient.complete();
        }).putHeader(HttpHeaders.ORIGIN, myOrigin).end();
    }

    @After
    public void cleanup(TestContext context)
    {
        vertx.deploymentIDs().forEach(id -> {
            vertx.undeploy(id, context.asyncAssertSuccess());
        });
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

}
