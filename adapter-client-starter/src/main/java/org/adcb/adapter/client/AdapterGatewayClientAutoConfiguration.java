package org.adcb.adapter.client;

import lombok.extern.slf4j.Slf4j;
import org.adcb.adapter.gateway.service.EnhancedProtocolAdapterService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for ADCB Adapter Gateway Client.
 *
 * <p>This configuration class automatically creates and configures the
 * {@link AdapterGatewayClient} bean when the adapter gateway service is
 * available on the classpath and not disabled via configuration.
 *
 * <p>Configuration can be disabled by setting:
 * <pre>
 * adcb.adapter.client.enabled=false
 * </pre>
 *
 * <p>The auto-configuration will only activate when:
 * <ul>
 *   <li>{@link EnhancedProtocolAdapterService} is on the classpath</li>
 *   <li>No existing {@link AdapterGatewayClient} bean is defined</li>
 *   <li>The feature is not explicitly disabled</li>
 * </ul>
 *
 * @since 1.0.0
 * @author ADCB Adapter Team
 */
@AutoConfiguration
@ConditionalOnClass(EnhancedProtocolAdapterService.class)
@ConditionalOnProperty(prefix = "adcb.adapter.client", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AdapterClientProperties.class)
@Slf4j
public class AdapterGatewayClientAutoConfiguration {

    /**
     * Creates the AdapterGatewayClient bean if none exists.
     *
     * @param protocolAdapterService the adapter service to wrap
     * @param properties configuration properties
     * @return configured AdapterGatewayClient instance
     */
    @Bean
    @ConditionalOnMissingBean
    public AdapterGatewayClient adapterGatewayClient(
            EnhancedProtocolAdapterService protocolAdapterService,
            AdapterClientProperties properties) {

        log.info("Auto-configuring AdapterGatewayClient with properties: {}", properties);
        return new AdapterGatewayClient(protocolAdapterService);
    }
}
