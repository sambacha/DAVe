package com.opnfi.risk;

import com.opnfi.risk.auth.ApiAuthHandler;
import com.opnfi.risk.restapi.ers.*;
import com.opnfi.risk.restapi.user.UserApi;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.mongo.HashSaltStyle;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by schojak on 19.8.16.
 */
public class MasterdataVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(MasterdataVerticle.class);

    private EventBus eb;
    private Map<String, List<String>> clearerMemberRelationship = new HashMap<>();
    private Map<String, JsonObject> memberDetails = new HashMap<>();

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        LOG.info("Starting {} with configuration: {}", MasterdataVerticle.class.getSimpleName(), config().encodePrettily());
        eb = vertx.eventBus();

        List<Future> futures = new ArrayList<>();
        futures.add(processMemberMasterdata());
        // TODO: Load product data
        //futures.add(loadProducts);
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
        JsonArray clearers = config().getJsonArray("clearers");
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
