package org.adcb.adapter.protocol.proxy;

import org.adcb.adapter.commons.ServiceMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProxyPassProtocolHandlerTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private ProxyPassProtocolHandler handler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        handler = new ProxyPassProtocolHandler(webClient);
    }

    @Test
    void testExecute_returnsRawResponse() {
        ServiceMetadata metadata = new ServiceMetadata();
        metadata.setEndpointUrl("http://localhost:9999/audits");
        metadata.setHttpMethod("POST");

        when(webClient.method(HttpMethod.POST)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(metadata.getEndpointUrl())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("proxied response"));

        Object resp = handler.execute(metadata, "{\"data\":\"value\"}");

        assertEquals("proxied response", resp);
    }
}
