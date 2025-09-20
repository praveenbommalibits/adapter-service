/*
package org.adcb.adapter.protocol.rest;

import org.adcb.adapter.commons.ServiceMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RestJsonProtocolHandlerTest {

    @Mock
    private WebClient webClient;

    @Mock
    private RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RequestBodySpec requestBodySpec;

    @Mock
    private RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private ResponseSpec responseSpec;

    private RestJsonProtocolHandler handler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        HttpClientProvider httpClientProvider = mock(HttpClientProvider.class);
        when(httpClientProvider.getWebClient()).thenReturn(webClient);

        handler = new RestJsonProtocolHandler(httpClientProvider);
    }

    @Test
    void testExecute_successResponseWithHeaders() {
        ServiceMetadata metadata = new ServiceMetadata();
        metadata.setEndpointUrl("http://example.com/api/resource");
        metadata.setHttpMethod("POST");
        metadata.setHeaders(Map.of("Authorization", "Bearer test-token"));
        metadata.setProtocol("REST_JSON");

        when(webClient.method(HttpMethod.POST)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(metadata.getEndpointUrl())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn((RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{\"status\":\"ok\"}"));

        Object response = handler.execute(metadata, Map.of("id", 123));

        assertNotNull(response);
        assertEquals("{\"status\":\"ok\"}", response);

        verify(webClient).method(HttpMethod.POST);
        verify(requestBodySpec).header("Authorization", "Bearer test-token");
        verify(requestBodySpec).bodyValue(any());
        verify(responseSpec).bodyToMono(String.class);
    }

    @Test
    void testExecute_successResponseWithoutHeaders() {
        ServiceMetadata metadata = new ServiceMetadata();
        metadata.setEndpointUrl("http://example.com/api/resource");
        metadata.setHttpMethod("GET");
        metadata.setHeaders(null);

        when(webClient.method(HttpMethod.GET)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(metadata.getEndpointUrl())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn((RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{\"data\":\"value\"}"));

        Object response = handler.execute(metadata, Map.of());

        assertNotNull(response);
        assertEquals("{\"data\":\"value\"}", response);

        verify(webClient).method(HttpMethod.GET);
        verify(requestBodySpec, never()).header(anyString(), anyString());
        verify(requestBodySpec).bodyValue(any());
        verify(responseSpec).bodyToMono(String.class);
    }

    @Test
    void testExecute_errorResponseThrowsException() {
        ServiceMetadata metadata = new ServiceMetadata();
        metadata.setEndpointUrl("http://example.com/api/resource");
        metadata.setHttpMethod("POST");

        when(webClient.method(HttpMethod.POST)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(metadata.getEndpointUrl())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn((RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("Remote error")));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                handler.execute(metadata, Map.of("param", "value")));

        assertEquals("Remote error", ex.getMessage());

        verify(webClient).method(HttpMethod.POST);
        verify(requestBodySpec).bodyValue(any());
        verify(responseSpec).bodyToMono(String.class);
    }
}
*/
