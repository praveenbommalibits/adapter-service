/*
package org.adcb.adapter.gateway.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import org.adcb.adapter.gateway.TestApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.adcb.adapter.client.AdapterGatewayClient;
import org.adcb.adapter.transform.model.StandardResponse;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

*/
/**
 * End-to-end integration test for the customer REST service adapter.
 * Starts a WireMockServer on port 8089, configures a stub for the downstream
 * REST API, invokes the adapter client, and verifies the StandardResponse.
 *//*

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class CustomerRestE2ETest {

    private static final int WIREMOCK_PORT = 8089;
    private static WireMockServer wireMockServer;

    @Autowired
    private AdapterGatewayClient client;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(WIREMOCK_PORT));
        wireMockServer.start();

        // Configure WireMock stub for /api/customers POST
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/customers"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"customerId\":\"12345\",\"name\":\"Alice\"}")));
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void invokeCustomerService_shouldReturnStandardResponse() {
        // Prepare input map matching customer_request.json template
        Map<String, Object> request = Map.of("customerId", "12345");

        // Invoke the adapter end-to-end
        StandardResponse<?> response = (StandardResponse<?>) client.invoke("customer_rest_service", request);

        // Validate the standardized response
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("200", response.getErrorCode());
        assertEquals("OK", response.getErrorDescription());

        assertNotNull(response.getPayload());
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) response.getPayload();
        assertEquals("12345", payload.get("customerId"));
        assertEquals("Alice", payload.get("name"));

        assertNotNull(response.getCorrelationId());
        assertNotNull(response.getTimestamp());
    }
}
*/
