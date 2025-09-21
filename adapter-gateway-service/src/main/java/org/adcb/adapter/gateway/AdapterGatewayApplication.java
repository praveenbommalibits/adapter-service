package org.adcb.adapter.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot main class placeholder for the Adapter Gateway runtime.
 * This keeps the structure ready for future Spring Boot integration.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "org.adcb.adapter.commons",
        "org.adcb.adapter.spi",
        "org.adcb.adapter.transform",
        "org.adcb.adapter.protocol.rest",
        "org.adcb.adapter.protocol.soap",
        "org.adcb.adapter.gateway"
})
public class AdapterGatewayApplication {
    public static void main(String[] args) {
        System.out.println("Starting ADCB Adapter Gateway Service...");
        SpringApplication.run(AdapterGatewayApplication.class, args);
        System.out.println("ADCB Adapter Gateway Service started successfully!");
    }
}
