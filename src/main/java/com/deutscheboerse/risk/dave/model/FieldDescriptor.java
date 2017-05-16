package com.deutscheboerse.risk.dave.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public interface FieldDescriptor<T extends Model> {

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

        public <T extends Model> FieldDescriptor<T> build() {
            return () -> Collections.unmodifiableMap(fields);
        }
    }
}
