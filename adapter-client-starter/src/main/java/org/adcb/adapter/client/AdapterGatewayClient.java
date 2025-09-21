package org.adcb.adapter.client;

import org.adcb.adapter.gateway.service.EnhancedProtocolAdapterService;
import java.util.Map;

/**
 * Facade for client microservices to invoke adapter gateway services.
 * Example usage:
 *   result = adapterGatewayClient.invoke("payment_service", requestParams);
 */
public class AdapterGatewayClient {

    private final EnhancedProtocolAdapterService protocolAdapterService;

    public AdapterGatewayClient(EnhancedProtocolAdapterService protocolAdapterService) {
        this.protocolAdapterService = protocolAdapterService;
    }

    /**
     * Invokes configured downstream service using Gateway.
     *
     * @param serviceName   Name configured in YAML
     * @param requestParams Request params to populate template
     * @return Response object (typed map or POJO)
     */
    public Object invoke(String serviceName, Map<String, Object> requestParams) {
        try {
            return protocolAdapterService.invoke(serviceName, requestParams);
        } catch (Exception ex) {
            throw new ServiceInvocationException(
                    "Failed to invoke adapter service: " + serviceName, ex);
        }
    }
}
