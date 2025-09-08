package org.adcb.adapter.spi;


import org.adcb.adapter.commons.ServiceMetadata;

public interface ProtocolHandler {
    Object execute(ServiceMetadata config, Object requestBody);
}
