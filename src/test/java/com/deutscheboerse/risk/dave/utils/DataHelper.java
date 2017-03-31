package com.deutscheboerse.risk.dave.utils;

import com.deutscheboerse.risk.dave.MainVerticleIT;
import com.deutscheboerse.risk.dave.model.AbstractModel;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class DataHelper {
    private static final Logger LOG = LoggerFactory.getLogger(DataHelper.class);

    private static Optional<JsonArray> getJsonArrayFromTTSaveFile(String folderName, int ttsaveNo) {
        String jsonPath = String.format("%s/snapshot-%03d.json", MainVerticleIT.class.getResource(folderName).getPath(), ttsaveNo);
        try {
            byte[] jsonArrayBytes = Files.readAllBytes(Paths.get(jsonPath));
            JsonArray jsonArray = new JsonArray(new String(jsonArrayBytes, Charset.defaultCharset()));
            return Optional.of(jsonArray);
        } catch (IOException e) {
            LOG.error("Unable to read data from {}", jsonPath, e);
            return Optional.empty();
        }
    }

    static Collection<JsonObject> readTTSaveFile(String folderName, int ttsaveNo) {
        return getJsonArrayFromTTSaveFile(folderName, ttsaveNo)
                .orElse(new JsonArray())
                .stream()
                .map(json -> (JsonObject) json)
                .collect(Collectors.toList());
    }

    public static Optional<JsonObject> getLastJsonFromFile(String folderName, int ttsaveNo) {
        return getJsonArrayFromTTSaveFile(folderName, ttsaveNo)
                .orElse(new JsonArray())
                .stream()
                .map(json -> (JsonObject) json)
                .reduce((a, b) -> b);
    }

    public static <T extends AbstractModel> T getLastModelFromFile(Class<T> clazz, int ttsaveNo) {
        String folderName = clazz.getSimpleName().substring(0, 1).toLowerCase() +
                clazz.getSimpleName().substring(1).replace("Model", "");
        JsonObject json = getLastJsonFromFile(folderName, ttsaveNo).orElse(new JsonObject());
        try {
            T model = clazz.newInstance();
            model.mergeIn(json);
            return model;
        } catch (IllegalAccessException|InstantiationException e) {
            throw new AssertionError();
        }
    }

    static int getJsonObjectCount(String folderName, int ttsaveNo) {
        return getJsonArrayFromTTSaveFile(folderName, ttsaveNo)
                .orElse(new JsonArray())
                .size();
    }

    public static JsonObject getQueryParams(AbstractModel model) {
        JsonObject queryParams = new JsonObject();
        model.getKeys().forEach(key -> queryParams.put(key, model.getValue(key)));
        return queryParams;
    }

    public static JsonObject getMongoDocument(AbstractModel model) {
        return model.copy();
    }

    static JsonObject getStoreDocument(AbstractModel model) {
        JsonObject document = new JsonObject();
        JsonObject setDocument = new JsonObject();
        JsonObject pushDocument = new JsonObject();
        model.getKeys().forEach(key -> setDocument.put(key, model.getValue(key)));
        model.stream()
                .filter(entry -> !model.getKeys().contains(entry.getKey()))
                .forEach(entry -> pushDocument.put(entry.getKey(), entry.getValue()));
        document.put("$set", setDocument);
        document.put("$push", new JsonObject().put("snapshots", pushDocument));
        return document;
    }

}
