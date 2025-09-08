package org.adcb.adapter.gateway.service;




import org.adcb.adapter.spi.ProtocolHandler;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Factory to retrieve protocol handler instances.
 */
public class ProtocolHandlerFactory {
    private final Supplier<ProtocolHandler> supplier;

    public ProtocolHandlerFactory(Supplier<ProtocolHandler> supplier) {
        this.supplier = Objects.requireNonNull(supplier, "supplier");
    }

    public ProtocolHandler get() {
        return supplier.get();
    }
}
