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
            LOG.info("Downloading products from URL {}", productListUrl);

            /*HttpClientOptions options = new HttpClientOptions()
                    .setProxyOptions(new ProxyOptions().setType(ProxyType.HTTP)
                            //.setHost("webproxy.deutsche-boerse.de").setPort(8080));
                            .setHost("10.139.7.11").setPort(8080));*/

            vertx.createHttpClient().getNow(productListUrl, res -> {
                if (res.statusCode() == 200) {
                    res.bodyHandler(body -> {
                        parseProducts(body);
                    });
                }
                else
                {
                    LOG.error("The product list URL doesn't seem to work! Status code {} returned with message {}.", res.statusCode(), res.statusMessage());
                }
            });

            return Future.succeededFuture();
        }
        else
        {
            LOG.warn("No product list URL defined. Products will not be loaded");
            return Future.succeededFuture();
        }
    }

    private void parseProducts(Buffer body) {
        LOG.info("Parsing product list");
        LOG.trace("Parsing product list {}", body.toString());

        final RecordParser parser = RecordParser.newDelimited("\n", line -> {
            String[] productFields = line.toString().split(";");
            String productId = productFields[0];
            if (productId != null)
            {
                LOG.info("Adding product {} to the product database", productId.trim());
                products.add(productId.trim());
            }
        });

        parser.handle(body);
    }

    private Future<Void> startListeners()
    {
        eb.consumer("masterdata.getMembershipInfo", message -> getMembershipInfo(message));

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

    @Override
    public void stop() throws Exception {
        LOG.info("Shutting down masterdata");
    }
}
