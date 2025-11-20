package org.adcb.adapter.gateway;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "org.adcb.adapter.commons",
        "org.adcb.adapter.spi",
        "org.adcb.adapter.transform",
        "org.adcb.adapter.protocol.rest",
        "org.adcb.adapter.protocol.soap",
        "org.adcb.adapter.gateway"
})
public class AdapterGatewayConfiguration {
}
