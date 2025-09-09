package org.adcb.adapter.protocol.proxy;

import org.adcb.adapter.spi.ProtocolHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Auto-configures the PROXY_PASS ProtocolHandler bean for the adapter platform.
 */
@Configuration
public class ProxyPassProtocolAutoConfig {

    @Bean("PROXY_PASS")
    public ProtocolHandler proxyPassProtocolHandler(WebClient.Builder builder) {
        return new ProxyPassProtocolHandler(builder.build());
    }
}
