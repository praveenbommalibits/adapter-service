package org.adcb.adapter.gateway.service;


import lombok.RequiredArgsConstructor;
import org.adcb.adapter.commons.ServiceMetadata;
import org.adcb.adapter.gateway.config.ServiceConfig;
import org.adcb.adapter.gateway.exception.AdapterException;
import org.adcb.adapter.gateway.transform.RequestTemplateEngine;
import org.adcb.adapter.gateway.transform.ResponseTransformer;
import org.adcb.adapter.spi.ProtocolHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Core orchestration service that routes requests to a selected ProtocolHandler.
 */

@Service
@RequiredArgsConstructor
public class ProtocolAdapterService {

    private final ServiceConfig serviceConfig;
    private final RequestTemplateEngine requestTemplateEngine;
    private final ResponseTransformer responseTransformer;
    public Map<String, ProtocolHandler> protocolHandlers;

    @Value("${adapter.templates.path:src/main/resources/templates/}")
    private String templatesPath;

    /**
     * Main gateway call method.
     * Loads templates from file, applies request data,
     * calls appropriate protocol handler, transforms response.
     */
    public Object call(String serviceName, Map<String, Object> requestData) {
        ServiceMetadata metadata = serviceConfig.getConfigs().get(serviceName);
        if (metadata == null) {
            throw new AdapterException("Service config not found for: " + serviceName);
        }

        try {
            // Load request template file content
            String requestTemplateContent = loadTemplateContent(metadata.getRequestTemplate());

            // Apply request data to template
            String preparedRequest = requestTemplateEngine.processTemplate(requestTemplateContent, requestData);

            // Lookup ProtocolHandler implementation by protocol key
            ProtocolHandler handler = protocolHandlers.get(metadata.getProtocol());
            if (handler == null) {
                throw new AdapterException("No ProtocolHandler registered for protocol: " + metadata.getProtocol());
            }

            // Invoke downstream service using prepared request
            Object rawResponse = handler.execute(metadata, preparedRequest);

            // Load response template file content
            String responseTemplateContent = loadTemplateContent(metadata.getResponseTemplate());

            // Transform response as per template
            return responseTransformer.transform(rawResponse, responseTemplateContent);

        } catch (IOException e) {
            throw new AdapterException("Failed to read templates for service: " + serviceName, e);
        }
    }

    //TODO Need to enhance for filepath and nested data for both json and xml cases
    public String loadTemplateContent(String templateFileName) throws IOException {
        Path templatePath = Path.of(templatesPath, templateFileName);
        return Files.readString(templatePath);
    }
}

