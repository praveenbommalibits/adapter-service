package org.adcb.adapter.protocol.proxy;

import lombok.extern.slf4j.Slf4j;
import org.adcb.adapter.spi.ProtocolHandler;
import org.adcb.adapter.commons.ServiceMetadata;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Protocol handler for direct HTTP proxy/pass-through (no request/response transformation).
 */
@Slf4j
public class ProxyPassProtocolHandler implements ProtocolHandler {

    private final WebClient webClient;

    public ProxyPassProtocolHandler(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Object execute(ServiceMetadata config, Object requestBody) {
        HttpMethod method = HttpMethod.valueOf(config.getHttpMethod());
        WebClient.RequestBodyUriSpec req = webClient.method(method);
        req.uri(config.getEndpointUrl());

        // Set request headers if any
        if (config.getHeaders() != null) {
            for (Map.Entry<String, String> entry : config.getHeaders().entrySet()) {
                req.header(entry.getKey(), entry.getValue());
            }
        }

        // Set request body if present
        WebClient.RequestHeadersSpec<?> request;
        if (requestBody != null) {
            request = req.bodyValue(requestBody);
        } else {
            request = req;
        }

        // Pass response as-is (String)
        String resp = request.retrieve()
                .bodyToMono(String.class)
                .block();

        log.debug("Proxy pass-through response: {}", resp);
        return resp;
    }
}
