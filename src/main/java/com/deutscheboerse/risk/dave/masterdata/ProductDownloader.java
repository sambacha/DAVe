package com.deutscheboerse.risk.dave.masterdata;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.parsetools.RecordParser;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by schojak on 30.9.16.
 */
public class ProductDownloader extends AbstractDownloader{
    private static final Logger LOG = LoggerFactory.getLogger(ProductDownloader.class);

    public ProductDownloader(Vertx vertx, String productUrl, JsonObject proxyConfiguration) throws MalformedURLException {
        super(vertx, productUrl, proxyConfiguration);
    }

    public void loadProducts(Handler<Future> handler)
    {
        download(res -> {
            if (res.succeeded())
            {
                handler.handle(Future.succeededFuture(parseProducts((Buffer)res.result())));
            }
            else
            {
                handler.handle(Future.failedFuture(res.toString()));
            }
        });
    }

    private List<String> parseProducts(Buffer body) {
        LOG.info("Parsing product list");
        LOG.trace("Parsing product list {}", body.toString());

        List<String> products = new LinkedList<>();

        final RecordParser parser = RecordParser.newDelimited("\n", line -> {
            String[] productFields = line.toString().split(";");
            String productId = productFields[0];
            if (productId != null && !productId.equals("PRODUCT_ID") && !productId.equals(""))
            {
                LOG.trace("Adding product {} to the product database", productId.trim());
                products.add(productId.trim());
            }
        });

        parser.handle(body.appendString("\n"));

        return products;
    }
}
