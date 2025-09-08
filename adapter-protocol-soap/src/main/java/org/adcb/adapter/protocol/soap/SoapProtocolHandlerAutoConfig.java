package org.adcb.adapter.protocol.soap;

import org.adcb.adapter.spi.ProtocolHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the SOAP protocol handler bean with Spring so the gateway/service can pick it up.
 */
@Configuration
public class SoapProtocolHandlerAutoConfig {
    @Bean("SOAP")
    public ProtocolHandler soapProtocolHandler() {
        return new SoapProtocolHandler();
    }
}
