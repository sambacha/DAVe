package com.deutscheboerse.risk.dave;

import com.deutscheboerse.risk.dave.persistence.PersistenceService;
import com.deutscheboerse.risk.dave.persistence.RestPersistenceService;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import javax.inject.Singleton;

public class PersistenceBinder extends AbstractModule {

    @Override
    protected void configure() {
        bindConfig();
        bindPersistenceService();
    }

    private void bindConfig() {
        JsonObject config = Vertx.currentContext().config();

        bind(JsonObject.class).annotatedWith(Names.named("storeManager.conf")).toInstance(config);
    }

    private void bindPersistenceService() {
        bind(PersistenceService.class).to(RestPersistenceService.class).in(Singleton.class);
    }
}
