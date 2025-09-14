/*
package org.adcb.adapter.gateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.adcb.adapter.client.AdapterGatewayClient;
import org.adcb.adapter.transform.model.StandardResponse;

import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.test.server.MockWebServiceServer;
import org.springframework.xml.transform.StringSource;
import static org.springframework.ws.test.server.RequestCreators.payload;
import static org.springframework.ws.test.server.ResponseCreators.withPayload;

import java.util.Map;

@SpringBootTest
public class AdapterSoapE2ETest {

    @Autowired
    AdapterGatewayClient adapterGatewayClient;

    @Autowired
    private WebServiceTemplate webServiceTemplate;

    private MockWebServiceServer mockServer;

    @BeforeEach
    public void init() {
        mockServer = MockWebServiceServer.createServer(webServiceTemplate);

        String soapResponse = """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                <soapenv:Body>
                    <ns:getCustomerResponse xmlns:ns="http://example.com/customer">
                        <customerId>12345</customerId>
                        <status>Active</status>
                    </ns:getCustomerResponse>
                </soapenv:Body>
            </soapenv:Envelope>
            """;

        mockServer.expect(payload(new StringSource(
                        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                                "<soapenv:Body><ns:getCustomer xmlns:ns=\"http://example.com/customer\">" +
                                "<customerId>12345</customerId></ns:getCustomer></soapenv:Body></soapenv:Envelope>")))
                .andRespond(withPayload(new StringSource(soapResponse)));
    }

    @Test
    public void testSoapServiceCall() {
        Map<String, Object> request = Map.of("customerId", "12345");

        // Cast to StandardResponse (fix for type conversion)
        StandardResponse<?> response = (StandardResponse<?>) adapterGatewayClient.invoke("test_soap_service", request);

        assertTrue(response.isSuccess());
        assertEquals("0", response.getErrorCode()); // Assuming 0 means no error
        assertNotNull(response.getPayload());

        mockServer.verify();
    }
}
*/
