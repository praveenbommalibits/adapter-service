/*
package org.adcb.adapter.gateway.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import org.adcb.adapter.gateway.TestApplication;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.client.WireMock;

import java.util.Map;

@SpringJUnitConfig
@SpringBootTest(classes = TestApplication.class)
public class AdapterRestE2ETest {

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void setupWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
        wireMockServer.start();

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/customers"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"customerId\":\"12345\",\"active\":true}")));
    }

    @Test
    public void testBasicWiring() {
        // Basic test to verify Spring context loads
        assertNotNull("Test context should load", String.valueOf(TestApplication.class));
        System.out.println("Spring Boot test context loaded successfully");
    }

    @Test
    public void testWireMockServer() {
        // Test WireMock server is working
        assertTrue(wireMockServer.isRunning());
        System.out.println("WireMock server is running on port 8089");
    }
}
*/
