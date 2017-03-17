package com.deutscheboerse.risk.dave.utils;

import com.deutscheboerse.risk.dave.MainVerticleIT;
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

class DataHelper {
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

    static int getJsonObjectCount(String folderName, int ttsaveNo) {
        return getJsonArrayFromTTSaveFile(folderName, ttsaveNo)
                .orElse(new JsonArray())
                .size();
    }
}
