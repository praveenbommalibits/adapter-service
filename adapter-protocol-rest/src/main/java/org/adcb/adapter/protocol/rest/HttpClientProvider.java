package org.adcb.adapter.protocol.rest;


import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class HttpClientProvider {

    private final WebClient webClient;

    public HttpClientProvider() {
        this.webClient = WebClient.builder()
                .build();
    }

    public WebClient getWebClient() {
        return webClient;
    }
}
