package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.masterdata.ProductDownloader;
import com.deutscheboerse.risk.dave.utils.DummyData;
import com.deutscheboerse.risk.dave.utils.DummyWebServer;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by schojak on 30.9.16.
 */
@RunWith(VertxUnitRunner.class)
public class ProductDownloaderTest {
    private static Vertx vertx;
    private static int port;
    private static HttpServer server;

    @BeforeClass
    public static void setUp(TestContext context)
    {
        vertx = Vertx.vertx();
        port = Integer.getInteger("http.port", 8080);
    }

    @Test
    public void testProductDownload(TestContext context) throws MalformedURLException {
        Async download = context.async();
        server = DummyWebServer.startWebserver(context, vertx, port, "/productlist.csv", DummyData.productList, "/302/productlist.csv");

        ProductDownloader pd = new ProductDownloader(vertx, "http://localhost:" + port + "/productlist.csv", null);
        pd.loadProducts(res -> {
            if (res.succeeded())
            {
                List<String> expected = new LinkedList<String>();
                expected.add("JUN3");
                expected.add("1COF");
                expected.add("1COV");

                context.assertEquals(expected, (List<String>)res.result());
                download.complete();
            }
            else
            {
                context.fail("Failed to download products: " + res.cause());
                download.complete();
            }
        });
    }

    @Test
    public void testProductDownloadInvalidURL(TestContext context) throws MalformedURLException {
        Async download = context.async();
        server = DummyWebServer.startWebserver(context, vertx, port, "/productlist.csv", DummyData.productList, "/302/productlist.csv");

        ProductDownloader pd = new ProductDownloader(vertx, "http://localhost:" + port + "/does/not/exist/productlist.csv", null);
        pd.loadProducts(res -> {
            if (res.succeeded())
            {
                context.fail("Invalid URL should not work");
            }
            else
            {
                download.complete();
            }
        });
    }

    @Test
    public void testProductDownloadRedirect(TestContext context) throws MalformedURLException {
        Async download = context.async();
        server = DummyWebServer.startWebserver(context, vertx, port, "/productlist.csv", DummyData.productList, "/302/productlist.csv");

        ProductDownloader pd = new ProductDownloader(vertx, "http://localhost:" + port + "/302/productlist.csv", null);
        pd.loadProducts(res -> {
            if (res.succeeded())
            {
                List<String> expected = new LinkedList<String>();
                expected.add("JUN3");
                expected.add("1COF");
                expected.add("1COV");

                context.assertEquals(expected, (List<String>)res.result());
                download.complete();
            }
            else
            {
                context.fail("Failed to download products: " + res.cause());
                download.complete();
            }
        });
    }

    @After
    public void stopWebserver(TestContext context)
    {
        server.close(context.asyncAssertSuccess());
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        DummyWebServer.stopWebserver(context, server);
        vertx.close(context.asyncAssertSuccess());
    }
}
