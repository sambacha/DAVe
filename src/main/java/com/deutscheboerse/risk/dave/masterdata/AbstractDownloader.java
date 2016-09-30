package com.deutscheboerse.risk.dave.masterdata;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by schojak on 30.9.16.
 */
public class AbstractDownloader {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDownloader.class);

    private static int MAX_REDIRECTS = 10;

    private final Vertx vertx;
    private final URL productUrl;
    private final JsonObject proxyConfiguration;

    private int redirects = 0;

    public AbstractDownloader(Vertx vertx, String productUrl, JsonObject proxyConfiguration) throws MalformedURLException {
        this.vertx = vertx;
        this.productUrl = new URL(productUrl);
        this.proxyConfiguration = proxyConfiguration;
    }

    protected void download(Handler<Future> handler)
    {
        HttpClientOptions options = new HttpClientOptions();
        configureProxy(options);
        HttpClient client = vertx.createHttpClient(options);
        downloadNow(client, productUrl, handler);
    }

    private void downloadNow(HttpClient client, URL url, Handler<Future> handler)
    {
        client.getNow(getPortFromUrl(url), url.getHost(), url.getPath(), res -> {
            if (res.statusCode() == 200) {
                res.bodyHandler(body -> {
                    handler.handle(Future.succeededFuture(body));
                });
            }
            else if (res.statusCode() == 302) {
                if (redirects < MAX_REDIRECTS)
                {
                    redirects++;

                    try {
                        LOG.info("Redirecting to {}. Maybe you should check the product list URL.", res.getHeader("Location"));
                        URL newUrl = new URL(res.getHeader("Location"));
                        downloadNow(client, newUrl, handler);
                    }
                    catch (MalformedURLException e)
                    {
                        LOG.error("The redirect header doesn't seem to contain valid URL: '{}'!.", res.getHeader("Location"), e);
                        handler.handle(Future.failedFuture("The redirect header doesn't seem to contain valid URL: '" + res.getHeader("Location") + "'. Exception: " + e));
                    }
                }
            }
            else {
                LOG.error("The download URL doesn't seem to work! Status code {} returned with message {}.", res.statusCode(), res.statusMessage());
                handler.handle(Future.failedFuture("The product list URL doesn't seem to work! Status code " + res.statusCode() + " returned with message " + res.statusMessage()));
            }
        });
    }

    private void configureProxy(HttpClientOptions options) {
        if (proxyConfiguration != null) {
            options.setProxyOptions(new ProxyOptions().setType(ProxyType.HTTP)
                    .setHost(proxyConfiguration.getString("host")).setPort(proxyConfiguration.getInteger("port")));
        }
    }

    private int getPortFromUrl(URL url)
    {
        if (url.getPort() == -1) {
            switch (url.getProtocol())
            {
                case "https":
                    return 443;
                case "http":
                default:
                    return 80;
            }
        }
        else
        {
            return url.getPort();
        }
    }
}
