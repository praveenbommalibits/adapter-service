package com.company.adapter.config;

import java.util.Map;
import java.util.Objects;

/**
 * Configuration entity for services managed by the config service.
 */
public class ServiceConfigEntity {
    private final String id;
    private final Map<String, String> properties;

    public ServiceConfigEntity(String id, Map<String, String> properties) {
        this.id = Objects.requireNonNull(id, "id");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public String getId() { return id; }
    public Map<String, String> getProperties() { return properties; }
}
