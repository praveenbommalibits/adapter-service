package org.adcb.adapter.protocol.rest;

import lombok.extern.slf4j.Slf4j;
import org.adcb.adapter.commons.ServiceMetadata;
import org.adcb.adapter.spi.ProtocolHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component("REST_JSON")
public class RestJsonProtocolHandler implements ProtocolHandler {

    private final WebClient webClient;

    @Autowired
    public RestJsonProtocolHandler(HttpClientProvider httpClientProvider) {
        this.webClient = httpClientProvider.getWebClient();
    }

    @Override
    public Object execute(ServiceMetadata config, Object requestBody) {
        log.info("Executing REST_JSON protocol for endpoint: {}", config.getEndpointUrl());

        WebClient.RequestBodySpec requestSpec = webClient
                .method(HttpMethod.valueOf(config.getHttpMethod()))
                .uri(config.getEndpointUrl())
                .contentType(MediaType.APPLICATION_JSON);

        if (config.getHeaders() != null) {
            config.getHeaders().forEach(requestSpec::header);
        }

        String response = requestSpec
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.debug("Received REST_JSON response: {}", response);
        return response;
    }
}
