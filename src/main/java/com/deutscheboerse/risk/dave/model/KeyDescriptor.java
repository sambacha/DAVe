package com.deutscheboerse.risk.dave.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public interface KeyDescriptor {

    Map<String, Class<?>> getUniqueFields();

    static ModelKeyDescriptorBuilder newBuilder() {
        return new ModelKeyDescriptorBuilder();
    }

    final class ModelKeyDescriptorBuilder {
        private Map<String, Class<?>> fields = new LinkedHashMap<>();

        private ModelKeyDescriptorBuilder() {
        }

        ModelKeyDescriptorBuilder addField(String fieldName, Class<?> fieldType) {
            fields.put(fieldName, fieldType);
            return this;
        }

        public KeyDescriptor build() {
            return () -> Collections.unmodifiableMap(fields);
        }
    }
}
