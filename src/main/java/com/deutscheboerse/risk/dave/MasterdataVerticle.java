package com.deutscheboerse.risk.dave;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;
import io.vertx.core.parsetools.RecordParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by schojak on 19.8.16.
 */
public class MasterdataVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(MasterdataVerticle.class);

    private EventBus eb;
    private Map<String, List<String>> clearerMemberRelationship = new HashMap<>();
    private Map<String, JsonObject> memberDetails = new HashMap<>();
    private List<String> products = new LinkedList<>();

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        LOG.info("Starting {} with configuration: {}", MasterdataVerticle.class.getSimpleName(), config().encodePrettily());
        eb = vertx.eventBus();

        List<Future> futures = new ArrayList<>();
        futures.add(processMemberMasterdata());
        futures.add(loadProducts());
        // TODO: Load margin classes
        //futures.add(loadMarginClasses);
        futures.add(startListeners());

        CompositeFuture.all(futures).setHandler(ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(ar.cause());
            }
        });
    }

    private Future<Void> processMemberMasterdata()
    {
        JsonArray clearers = config().getJsonArray("clearers", new JsonArray());
        clearers.forEach(clr -> {
            JsonObject clearer = (JsonObject)clr;
            String clearerId = clearer.getString("clearer");

            List<String> members = new ArrayList<>();
            clearer.getJsonArray("members").forEach(mbr -> {
                JsonObject member = (JsonObject)mbr;
                String memberId = member.getString("member");

                members.add(memberId);

                member.put("clearer", clearerId);
                memberDetails.put(memberId, member);
            });
            clearerMemberRelationship.put(clearerId, members);
        });

        return Future.succeededFuture();
    }

    private Future<Void> loadProducts()
    {
        String productListUrl = config().getString("productListUrl");

        if (productListUrl != null) {
            return loadProductsFromUrl(productListUrl);
        }
        else
        {
            LOG.warn("No product list URL defined. Products will not be loaded");
            return Future.succeededFuture();
        }
    }

    private Future<Void> loadProductsFromUrl(String productListUrl)
    {
        Future<Void> productLoad = Future.future();
        LOG.info("Downloading products from URL {}", productListUrl);

        try {
            URL url = new URL(productListUrl);

            HttpClientOptions options = new HttpClientOptions();
            configureProxy(options);
            vertx.createHttpClient(options).getNow(getPortFromUrl(url), url.getHost(), url.getPath(), res -> {
                if (res.statusCode() == 200) {
                    res.bodyHandler(body -> {
                        parseProducts(body);
                        productLoad.complete();
                    });
                } else {
                    LOG.error("The product list URL doesn't seem to work! Status code {} returned with message {}.", res.statusCode(), res.statusMessage());
                    productLoad.fail("The product list URL doesn't seem to work! Status code " + res.statusCode() + " returned with message " + res.statusMessage());
                }
            });
        }
        catch (MalformedURLException e)
        {
            LOG.error("Failed to parse the product list URL", e);
        }

        return productLoad;
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

    private void configureProxy(HttpClientOptions options) {
        if (config().getJsonObject("httpProxy") != null) {
            options.setProxyOptions(new ProxyOptions().setType(ProxyType.HTTP)
                    .setHost(config().getJsonObject("httpProxy").getString("host")).setPort(config().getJsonObject("httpProxy").getInteger("port")));
        }
    }

    private void parseProducts(Buffer body) {
        LOG.info("Parsing product list");
        LOG.trace("Parsing product list {}", body.toString());

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
    }

    private Future<Void> startListeners()
    {
        eb.consumer("masterdata.getMembershipInfo", message -> getMembershipInfo(message));
        eb.consumer("masterdata.getProducts", message -> getProducts(message));

        return Future.succeededFuture();
    }

    private void getMembershipInfo(Message msg)
    {
        LOG.trace("Received getMembershipInfo request with parameters: " + msg.body().toString());
        JsonObject params = (JsonObject)msg.body();
        String memberId = params.getString("member");
        JsonArray response = new JsonArray();

        if (clearerMemberRelationship.containsKey(memberId))
        {
            clearerMemberRelationship.get(memberId).forEach(ncm -> {
                if (memberDetails.containsKey(ncm))
                {
                    response.add(memberDetails.get(ncm));
                }
            });
        }
        else if (memberDetails.containsKey(memberId))
        {
            response.add(memberDetails.get(memberId));
        }

        msg.reply(response);
    }

    private void getProducts(Message msg)
    {
        LOG.trace("Received getProducts request");
        JsonArray response = new JsonArray(products);

        msg.reply(response);
    }

    @Override
    public void stop() throws Exception {
        LOG.info("Shutting down masterdata");
    }
}
