package com.deutscheboerse.risk.dave.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public interface FieldDescriptor {

    Map<String, Class<?>> getUniqueFields();

    static FieldDescriptorBuilder newBuilder() {
        return new FieldDescriptorBuilder();
    }

    final class FieldDescriptorBuilder {
        private Map<String, Class<?>> fields = new LinkedHashMap<>();

        private FieldDescriptorBuilder() {
        }

        FieldDescriptorBuilder addField(String fieldName, Class<?> fieldType) {
            fields.put(fieldName, fieldType);
            return this;
        }

        public FieldDescriptor build() {
            return () -> Collections.unmodifiableMap(fields);
        }
    }
}
