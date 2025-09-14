package org.adcb.adapter.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.adcb.adapter.commons.ServiceMetadata;
import org.adcb.adapter.gateway.config.ServiceConfig;
import org.adcb.adapter.gateway.exception.AdapterException;
import org.adcb.adapter.spi.ProtocolHandler;
import org.adcb.adapter.transform.service.AdapterTransformService;
import org.adcb.adapter.transform.model.StandardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Core orchestration service that routes requests to correct ProtocolHandler,
 * performs request payload rendering using templates, and normalizes responses
 * into a standard format using adapter-transform-core.
 * /**
 *  * Core orchestration service that routes requests to a selected ProtocolHandler.
 *  * TODO HSM SOCKET HANDLER
 *  * TODO SEMANTIC SERVER SCANNING
 *  * TODO TRANSFORMATION SERVICE AND AGREEMENT ON RESPONSE STRUCTURE
 *  * TODO EXCEPTION HANDLING
 *  * TODO RETRIES AND FALLBACKS - BACK PRESSURE APPROACH AND CIRCUIT BREAKER PATTERN
 *  * TODO VERSIONING NEED TO CHECK
 *  *
 *  */

@Service
@RequiredArgsConstructor
@Slf4j
public class ProtocolAdapterService {

    private final ServiceConfig serviceConfig;
    private final AdapterTransformService transformService;
    @Autowired
    public Map<String, ProtocolHandler> protocolHandlers;

    @Value("${adapter.templates.path:src/main/resources/templates/}")
    private String templatesPath;

    /**
     * Main gateway call method.
     * Steps:
     *  1. Load and render request template with request data (if template exists).
     *  2. Invoke downstream protocol handler with rendered request (or raw request data for proxy).
     *  3. Transform raw response into standard, normalized response.
     *
     * @param serviceName logical service identifier from microservice call
     * @param requestData generic request data map (supports nested structures)
     * @return StandardResponse with normalized response attributes and payload
     */
    public StandardResponse<?> call(String serviceName, Map<String, Object> requestData) {
        ServiceMetadata metadata = serviceConfig.getServices().get(serviceName);

        if (metadata == null) {
            throw new AdapterException("Service config not found for: " + serviceName);
        }

        try {
            // 1. Prepare request payload
            String preparedRequest = null;
            String requestTemplate = metadata.getRequestTemplate();

            if (requestTemplate != null && !requestTemplate.isEmpty()) {
                // Render request template with requestData using transform-core engine
                String rawTemplate = loadTemplateContent(requestTemplate);
                preparedRequest = transformService.buildRequestPayload(rawTemplate, requestData);
            } else {
                // No template defined - use raw request data as-is (e.g. for ProxyPass)
                // For protocols expecting String payloads, convert if needed
                preparedRequest = requestData != null ? requestData.toString() : null;
            }

            // 2. Call downstream protocol handler
            ProtocolHandler handler = protocolHandlers.get(metadata.getProtocol());
            if (handler == null) {
                throw new AdapterException("No ProtocolHandler registered for protocol: " + metadata.getProtocol());
            }

            Object rawResponse = handler.execute(metadata, preparedRequest);

            // 3. Normalize and transform response
            StandardResponse<?> standardizedResponse;

            // Choose transformation based on protocol type
            switch (metadata.getProtocol().toUpperCase()) {
                case "SOAP":
                    // Parse SOAP responses using configured XPath expressions
                    standardizedResponse = transformService.parseSoapResponse(
                            rawResponse != null ? rawResponse.toString() : null,
                            metadata.getErrorCodeXPath(),
                            metadata.getErrorDescriptionXPath(),
                            getCorrelationId(),
                            serviceName);
                    break;
                case "REST_JSON":
                case "PROXY_PASS":
                default:
                    // Parse REST/Proxy JSON or raw string response
                    // For Proxy, response can be raw JSON/string as well
                    int httpStatus = metadata.getLastHttpStatus() != null ? metadata.getLastHttpStatus() : 200;
                    String errorDesc = metadata.getLastErrorDescription() != null ? metadata.getLastErrorDescription() : "OK";
                    standardizedResponse = transformService.parseRestResponse(
                            rawResponse != null ? rawResponse.toString() : null,
                            httpStatus,
                            errorDesc,
                            getCorrelationId(),
                            serviceName);
                    break;
            }

            return standardizedResponse;

        } catch (IOException e) {
            log.error("Failed to load template for service: {}", serviceName, e);
            throw new AdapterException("Failed to read templates for service: " + serviceName, e);
        } catch (Exception e) {
            log.error("Error in processing request for service: {}", serviceName, e);
            throw new AdapterException("Error processing request for service: " + serviceName, e);
        }
    }

    /**
     * Loads the content of a template file from the templates directory.
     *
     * @param templateFileName relative path or file name of the template
     * @return content of the template file as String
     * @throws IOException if reading the file fails
     */
    public String loadTemplateContent(String templateFileName) throws IOException {
        if (templateFileName == null || templateFileName.isBlank()) {
            return null;
        }
        Path templatePath = Path.of(templatesPath, templateFileName);
        return Files.readString(templatePath);
    }

    /**
     * Retrieve or generate a correlation id for tracing and logging.
     * Replace this with your actual implementation.
     */
    private String getCorrelationId() {
        // Stub: generate or extract correlation id from MDC or context
        return java.util.UUID.randomUUID().toString();
    }
}
