package org.adcb.adapter.gateway.config;

import org.adcb.adapter.transform.service.AdapterTransformService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransformConfig {

    @Bean
    public AdapterTransformService adapterTransformService() {
        return new AdapterTransformService();
    }
}
