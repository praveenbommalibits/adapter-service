package org.adcb.adapter.gateway.config;

import lombok.Data;
import org.adcb.adapter.commons.ServiceMetadata;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "adapter")
@Data
public class ServiceConfig {

    /**
     * Map of service name to ServiceMetadata loaded from YAML
     */
    private Map<String, ServiceMetadata> services;

    // No Spring annotations in POJO ServiceMetadata (imported from commons)
}
