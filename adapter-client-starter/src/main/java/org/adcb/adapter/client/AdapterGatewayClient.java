package org.adcb.adapter.client;

import org.adcb.adapter.gateway.service.ProtocolAdapterService;
import org.adcb.adapter.commons.ServiceMetadata;
import java.util.Map;

/**
 * Facade for client microservices to invoke adapter gateway services.
 * Example usage:
 *   result = adapterGatewayClient.invoke("payment_service", requestParams);
 */
public class AdapterGatewayClient {

    private final ProtocolAdapterService protocolAdapterService;

    public AdapterGatewayClient(ProtocolAdapterService protocolAdapterService) {
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
            return protocolAdapterService.call(serviceName, requestParams);
        } catch (Exception ex) {
            throw new ServiceInvocationException(
                    "Failed to invoke adapter service: " + serviceName, ex);
        }
    }
}
