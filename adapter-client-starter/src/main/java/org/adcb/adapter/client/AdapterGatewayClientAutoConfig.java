package org.adcb.adapter.client;


import org.adcb.adapter.gateway.service.ProtocolAdapterService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * Auto-configuration for AdapterGatewayClient.
 * Automatically adds AdapterGatewayClient bean if missing.
 */
@Configuration
public class AdapterGatewayClientAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public AdapterGatewayClient adapterGatewayClient(ProtocolAdapterService protocolAdapterService) {
        return new AdapterGatewayClient(protocolAdapterService);
    }
}
