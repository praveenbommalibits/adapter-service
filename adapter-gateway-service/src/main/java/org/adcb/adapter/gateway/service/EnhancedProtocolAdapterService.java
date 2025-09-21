package org.adcb.adapter.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.adcb.adapter.commons.*;
import org.adcb.adapter.gateway.config.ServiceConfig;
import org.adcb.adapter.gateway.resilience.CircuitBreakerManager;
import org.adcb.adapter.gateway.resilience.RetryHandler;
import org.adcb.adapter.spi.ProtocolHandler;
import org.adcb.adapter.transform.TemplateService;
import org.adcb.adapter.transform.exception.TemplateProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced Protocol Adapter Service - Main orchestration layer.
 *
 * <p>This service coordinates all adapter functionality:
 * <ul>
 *   <li>Service configuration loading and validation</li>
 *   <li>Protocol handler selection and execution</li>
 *   <li>Authentication strategy application</li>
 *   <li>Resilience pattern integration (circuit breaker, retry)</li>
 *   <li>Request/response template processing</li>
 *   <li>Error mapping and response standardization</li>
 *   <li>Performance metrics collection</li>
 * </ul>
 *
 * <p>Returns standardized responses regardless of downstream protocol,
 * ensuring consistent API contracts for all consuming microservices.
 *
 * @since 1.0
 */
@Service
@Slf4j
public class EnhancedProtocolAdapterService {

    @Autowired
    private final Map<String, ProtocolHandler> protocolHandlers;
    private final ServiceConfig serviceConfigs;
    //private final Map<String, ServiceMetadata> serviceConfigs;
    private final TemplateService templateService;
    private final CircuitBreakerManager circuitBreakerManager;
    private final RetryHandler retryHandler;
    private final ErrorMapper errorMapper;

    @Autowired
    public EnhancedProtocolAdapterService(
            Map<String, ProtocolHandler> protocolHandlers,
            ServiceConfig serviceConfigs,
            TemplateService templateService,
            CircuitBreakerManager circuitBreakerManager,
            RetryHandler retryHandler,
            ErrorMapper errorMapper) {

        this.protocolHandlers = protocolHandlers;
        this.serviceConfigs = serviceConfigs;
        this.templateService = templateService;
        this.circuitBreakerManager = circuitBreakerManager;
        this.retryHandler = retryHandler;
        this.errorMapper = errorMapper;

        log.info("EnhancedProtocolAdapterService initialized with {} protocol handlers and {} service configs",
                protocolHandlers.size(), serviceConfigs);
    }

    /**
     * Main entry point for service invocation.
     *
     * @param serviceName unique service identifier
     * @param requestData request payload (typically Map<String,Object>)
     * @return StandardResponse with success payload or error details
     */
    public StandardResponse<?> invoke(String serviceName, Object requestData) {
        String correlationId = generateCorrelationId();
        long startTime = System.currentTimeMillis();

        log.info("Invoking service '{}' with correlation ID: {}", serviceName, correlationId);

        try {
            // 1. Load and validate service configuration
            ServiceMetadata config = getServiceConfig(serviceName);

            // 2. Get protocol handler
            ProtocolHandler handler = getProtocolHandler(config.getProtocol());

            // 3. Execute with resilience patterns
            Object rawResponse = executeWithResilience(serviceName, config, requestData, handler, correlationId);

            // 4. Process and transform response
            StandardResponse<?> response = processResponse(rawResponse, config, correlationId, startTime);

            log.info("Service '{}' completed successfully in {}ms", serviceName,
                    System.currentTimeMillis() - startTime);

            return response;

        } catch (Exception e) {
            log.error("Service '{}' failed with correlation ID '{}': {}",
                    serviceName, correlationId, e.getMessage(), e);

            return handleError(e, serviceName, correlationId, startTime);
        }
    }

    /**
     * Executes service call with circuit breaker and retry protection.
     */
    private Object executeWithResilience(String serviceName, ServiceMetadata config,
                                         Object requestData, ProtocolHandler handler,
                                         String correlationId) {

        return circuitBreakerManager.execute(serviceName, config, () ->
                retryHandler.execute(config, () -> {

                    // Enrich request data with system context
                    Object enrichedRequest = enrichRequestData(requestData, config, correlationId);

                    // Execute the protocol handler
                    return handler.execute(config, enrichedRequest);
                })
        );
    }

    /**
     * Enriches request data with system variables and correlation info.
     */
    private Object enrichRequestData(Object originalData, ServiceMetadata config, String correlationId) {
        if (originalData instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = new ConcurrentHashMap<>((Map<String, Object>) originalData);

            // Add system variables
            dataMap.put("correlationId", correlationId);
            dataMap.put("timestamp", LocalDateTime.now().toString());
            dataMap.put("serviceName", config.getServiceName());
            dataMap.put("systemVersion", "1.0");

            return dataMap;
        }

        return originalData;
    }

    /**
     * Processes raw response from protocol handler into StandardResponse.
     */
    /*private StandardResponse<?> processResponse(Object rawResponse, ServiceMetadata config,
                                                String correlationId, long startTime) {

        long processingTime = System.currentTimeMillis() - startTime;

        try {
            // If response is already a StandardResponse (from error mapping), return as-is
            if (rawResponse instanceof StandardResponse) {
                StandardResponse<?> standardResp = (StandardResponse<?>) rawResponse;
                return enrichStandardResponse(standardResp, config, correlationId, processingTime);
            }

            // Process successful response
            Object processedPayload = rawResponse;

            // Apply response template if configured
            if (config.getResponseTemplate() != null && rawResponse != null) {
                processedPayload = applyResponseTemplate(rawResponse, config);
            }

            // Build success response
            return StandardResponse.builder()
                    .success(true)
                    .status(ResponseStatus.SUCCESS)
                    .payload(processedPayload)
                    .correlationId(correlationId)
                    .timestamp(LocalDateTime.now())
                    .serviceName(config.getServiceName())
                    .protocol(config.getProtocol())
                    .performance(PerformanceMetrics.builder()
                            .executionTimeMs(processingTime)
                            .circuitBreakerState("CLOSED") // Will be enhanced with actual state
                            .retryAttempts(0) // Will be enhanced with actual retry count
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("Response processing failed for service '{}': {}",
                    config.getServiceName(), e.getMessage());

            ErrorDetails error = ErrorDetails.builder()
                    .errorCode("RESPONSE_PROCESSING_ERROR")
                    .errorMessage("Failed to process response")
                    .errorDescription("Error occurred while processing the response: " + e.getMessage())
                    .category(ErrorCategory.TECHNICAL)
                    .severity(ErrorSeverity.HIGH)
                    .source("GATEWAY_SERVICE")
                    .technicalMessage(e.getMessage())
                    .exceptionClass(e.getClass().getSimpleName())
                    .retryable(false)
                    .build();

            return StandardResponse.builder()
                    .success(false)
                    .status(ResponseStatus.TECHNICAL_ERROR)
                    .error(error)
                    .correlationId(correlationId)
                    .timestamp(LocalDateTime.now())
                    .serviceName(config.getServiceName())
                    .protocol(config.getProtocol())
                    .build();
        }
    }*/
    /**
     * Processes raw response from protocol handler into StandardResponse.
     * Note: Response templates are already applied by the ProtocolHandler.
     */
    private StandardResponse<?> processResponse(Object rawResponse, ServiceMetadata config,
                                                String correlationId, long startTime) {

        long processingTime = System.currentTimeMillis() - startTime;

        try {
            // If response is already a StandardResponse (from error mapping), return as-is
            if (rawResponse instanceof StandardResponse) {
                StandardResponse<?> standardResp = (StandardResponse<?>) rawResponse;
                return enrichStandardResponse(standardResp, config, correlationId, processingTime);
            }

            // rawResponse is already processed/templated by the ProtocolHandler
            // Just wrap it in StandardResponse
            return StandardResponse.builder()
                    .success(true)
                    .status(ResponseStatus.SUCCESS)
                    .payload(rawResponse)  // <-- Use rawResponse directly, no re-templating
                    .correlationId(correlationId)
                    .timestamp(LocalDateTime.now())
                    .serviceName(config.getServiceName())
                    .protocol(config.getProtocol())
                    .performance(PerformanceMetrics.builder()
                            .executionTimeMs(processingTime)
                            .circuitBreakerState("CLOSED")
                            .retryAttempts(0)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("Response processing failed for service '{}': {}",
                    config.getServiceName(), e.getMessage());

            ErrorDetails error = ErrorDetails.builder()
                    .errorCode("RESPONSE_PROCESSING_ERROR")
                    .errorMessage("Failed to process response")
                    .errorDescription("Error occurred while processing the response: " + e.getMessage())
                    .category(ErrorCategory.TECHNICAL)
                    .severity(ErrorSeverity.HIGH)
                    .source("GATEWAY_SERVICE")
                    .technicalMessage(e.getMessage())
                    .exceptionClass(e.getClass().getSimpleName())
                    .retryable(false)
                    .build();

            return StandardResponse.builder()
                    .success(false)
                    .status(ResponseStatus.TECHNICAL_ERROR)
                    .error(error)
                    .correlationId(correlationId)
                    .timestamp(LocalDateTime.now())
                    .serviceName(config.getServiceName())
                    .protocol(config.getProtocol())
                    .build();
        }
    }


    /**
     * Applies response template transformation if configured.
     */
    /*private Object applyResponseTemplate(Object rawResponse, ServiceMetadata config)
            throws TemplateProcessingException {

        if (rawResponse instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) rawResponse;

            String templateResult = templateService.process(config.getResponseTemplate(), responseMap);

            // Try to parse back to object structure
            try {
                return new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(templateResult, Object.class);
            } catch (Exception e) {
                log.warn("Could not parse template result as JSON, returning as string");
                return templateResult;
            }
        }

        return rawResponse;
    }*/

    /**
     * Enriches existing StandardResponse with correlation and performance data.
     */
    private StandardResponse<?> enrichStandardResponse(StandardResponse<?> response,
                                                       ServiceMetadata config,
                                                       String correlationId,
                                                       long processingTime) {

        return response.toBuilder()
                .correlationId(correlationId)
                .serviceName(config.getServiceName())
                .protocol(config.getProtocol())
                .performance(PerformanceMetrics.builder()
                        .executionTimeMs(processingTime)
                        .build())
                .build();
    }

    /**
     * Handles errors and maps them to StandardResponse.
     */
    private StandardResponse<?> handleError(Exception e, String serviceName,
                                            String correlationId, long startTime) {

        long processingTime = System.currentTimeMillis() - startTime;

        ErrorDetails error = errorMapper.mapError(e, serviceName, "UNKNOWN");

        return StandardResponse.builder()
                .success(false)
                .status(determineResponseStatus(error))
                .error(error)
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .serviceName(serviceName)
                .performance(PerformanceMetrics.builder()
                        .executionTimeMs(processingTime)
                        .build())
                .build();
    }

    /**
     * Gets service configuration by name.
     */
    private ServiceMetadata getServiceConfig(String serviceName) {
        ServiceMetadata config = serviceConfigs.getServices().get(serviceName);
        if (config == null) {
            throw new IllegalArgumentException("Service configuration not found: " + serviceName);
        }
        return config;
    }

    /**
     * Gets protocol handler by protocol name.
     */
    private ProtocolHandler getProtocolHandler(String protocol) {
        ProtocolHandler handler = protocolHandlers.get(protocol);
        if (handler == null) {
            throw new UnsupportedOperationException("Protocol handler not found: " + protocol);
        }
        return handler;
    }

    /**
     * Determines response status based on error category.
     */
    private ResponseStatus determineResponseStatus(ErrorDetails error) {
        return switch (error.getCategory()) {
            case BUSINESS, VALIDATION -> ResponseStatus.BUSINESS_ERROR;
            case CIRCUIT_BREAKER -> ResponseStatus.CIRCUIT_OPEN;
            default -> ResponseStatus.TECHNICAL_ERROR;
        };
    }

    /**
     * Generates unique correlation ID for request tracking.
     */
    private String generateCorrelationId() {
        return "adcb-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
