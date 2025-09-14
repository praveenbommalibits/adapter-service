package org.adcb.adapter.gateway;

import org.adcb.adapter.gateway.config.ServiceConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {
        "org.adcb.adapter.gateway",
        "org.adcb.adapter.client",
        "org.adcb.adapter.transform",
        "org.adcb.adapter.spi",
        "org.adcb.adapter.commons",
        "org.adcb.adapter.protocol.rest",
        //"org.adcb.adapter.protocol.soap",
        //"org.adcb.adapter.protocol.proxy"
})
@EnableConfigurationProperties(ServiceConfig.class)
public class TestApplication {}
