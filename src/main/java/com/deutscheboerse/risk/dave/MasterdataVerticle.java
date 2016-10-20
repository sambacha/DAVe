package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.masterdata.ProductDownloader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.net.MalformedURLException;
import java.util.*;

/**
 * Created by schojak on 19.8.16.
 */
public class MasterdataVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(MasterdataVerticle.class);

    private EventBus eb;
    private final List<MessageConsumer<?>> eventBusConsumers = new ArrayList<>();
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
        if (config().getString("productListUrl") != null) {
            return loadProductsFromUrl(config().getString("productListUrl"));
        } else if (config().getJsonArray("productList") != null) {
            return loadProductsFromConfig(config().getJsonArray("productList"));
        } else {
            LOG.warn("No product list URL defined. Products will not be loaded");
            return Future.succeededFuture();
        }
    }

    private Future<Void> loadProductsFromConfig(JsonArray productList) {
        products = productList.getList();
        return Future.succeededFuture();
    }

    private Future<Void> loadProductsFromUrl(String productListUrl)
    {
        Future<Void> productLoad = Future.future();
        LOG.info("Downloading products from URL {}", productListUrl);

        try {
            ProductDownloader pd = new ProductDownloader(vertx, productListUrl, config().getJsonObject("httpProxy"));
            pd.loadProducts(res -> {
                if (res.succeeded())
                {
                    products = (List<String>)res.result();
                    productLoad.complete();
                }
                else
                {
                    LOG.error("Failed to download product list {}.", res.toString());
                    productLoad.fail("Failed to download product list.");
                }
            });
        }
        catch (MalformedURLException e)
        {
            LOG.error("Failed to parse the product list URL", e);
        }

        return productLoad;
    }

    private Future<Void> startListeners()
    {
        this.registerConsumer("masterdata.getMembershipInfo", message -> getMembershipInfo(message));
        this.registerConsumer("masterdata.getProducts", message -> getProducts(message));

        return Future.succeededFuture();
    }

    private <T> void registerConsumer(String address, Handler<Message<T>> handler) {
        EventBus eb = vertx.eventBus();
        this.eventBusConsumers.add(eb.consumer(address, handler));
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
        this.eventBusConsumers.forEach(consumer -> consumer.unregister());
    }
}
