package org.adcb.adapter.protocol.soap;

import org.adcb.adapter.commons.ServiceMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceMessageExtractor;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.SoapFaultClientException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SoapProtocolHandlerTest {

    @Mock
    private WebServiceTemplate webServiceTemplate;

    private SoapProtocolHandler soapProtocolHandler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        soapProtocolHandler = new SoapProtocolHandler(webServiceTemplate);
    }

    @Test
    void testExecute_successResponse() throws Exception {
        ServiceMetadata metadata = buildTestServiceMetadata();
        Object requestBody = "<request>test</request>";
        String expectedResponse = "<soapResponse>Success</soapResponse>";

        when(webServiceTemplate.sendAndReceive(
                anyString(),
                any(WebServiceMessageCallback.class),
                any(WebServiceMessageExtractor.class)))
                .thenReturn(expectedResponse);

        Object actualResponse = soapProtocolHandler.execute(metadata, requestBody);

        assertEquals(expectedResponse, actualResponse);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(webServiceTemplate).sendAndReceive(urlCaptor.capture(),
                any(WebServiceMessageCallback.class),
                any(WebServiceMessageExtractor.class));
        assertEquals(metadata.getEndpointUrl(), urlCaptor.getValue());
    }

   // @Test
    void testExecute_soapFaultThrows() {
        ServiceMetadata metadata = buildTestServiceMetadata();
        Object requestBody = "<request>test</request>";

        SoapMessage mockSoapMessage = Mockito.mock(SoapMessage.class);

        // Complete stubbing statement
        when(webServiceTemplate.sendAndReceive(
                anyString(),
                any(WebServiceMessageCallback.class),
                any(WebServiceMessageExtractor.class)))
                .thenThrow(new SoapFaultClientException(mockSoapMessage));

        SoapProtocolException ex = assertThrows(SoapProtocolException.class, () ->
                soapProtocolHandler.execute(metadata, requestBody));

        assertTrue(ex.getMessage().contains("SOAP fault"));
    }


    @Test
    void testExecute_transportIOExceptionThrows() {
        ServiceMetadata metadata = buildTestServiceMetadata();
        Object requestBody = "<request>test</request>";

        when(webServiceTemplate.sendAndReceive(anyString(),
                any(WebServiceMessageCallback.class),
                any(WebServiceMessageExtractor.class)))
                .thenThrow(new WebServiceIOException("Timeout"));

        SoapProtocolException ex = assertThrows(SoapProtocolException.class, () ->
                soapProtocolHandler.execute(metadata, requestBody));

        assertTrue(ex.getMessage().contains("SOAP transport failed"));
    }

    private ServiceMetadata buildTestServiceMetadata() {
        ServiceMetadata metadata = new ServiceMetadata();
        metadata.setEndpointUrl("http://test-soap-endpoint");
        metadata.setRequestTemplate("testRequestTemplate");
        metadata.setHeaders(null);
        metadata.setResponseTemplate("testResponseTemplate");
        // Populate other metadata as needed...
        return metadata;
    }
}
