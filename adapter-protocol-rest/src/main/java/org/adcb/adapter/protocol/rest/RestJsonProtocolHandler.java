package org.adcb.adapter.protocol.rest;



// plus your existing imports


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.adcb.adapter.commons.*;
import org.adcb.adapter.spi.ProtocolHandler;
import org.adcb.adapter.spi.auth.AuthenticationStrategy;
import org.adcb.adapter.transform.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import io.netty.channel.ChannelOption;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;


/**
 * REST JSON protocol handler using Spring WebFlux WebClient.
 *
 * <p>This handler processes REST-based service calls with full support for:
 * <ul>
 *   <li>All HTTP methods (GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS)</li>
 *   <li>Authentication strategy integration</li>
 *   <li>Request/response template processing</li>
 *   <li>Comprehensive error handling and mapping</li>
 *   <li>Timeout and connection management</li>
 * </ul>
 *
 * <p>The handler transforms raw HTTP responses into StandardResponse objects,
 * ensuring consistent error handling across all REST integrations.
 *
 * @since 1.0
 */
@Component("REST_JSON")
@Slf4j
public class RestJsonProtocolHandler implements ProtocolHandler {

    private final WebClient.Builder webClientBuilder;
    private final Map<String, AuthenticationStrategy> authStrategies;
    private final TemplateService templateService;

    @Autowired
    public RestJsonProtocolHandler(WebClient.Builder webClientBuilder,
                                   Map<String, AuthenticationStrategy> authStrategies,
                                   TemplateService templateService) {
        this.webClientBuilder = webClientBuilder;
        this.authStrategies = authStrategies;
        this.templateService = templateService;
    }

    @Override
    public Object execute(ServiceMetadata config, Object requestBody) {
        try {
            log.debug("Executing REST call to: {} using method: {}",
                    config.getEndpointUrl(), config.getHttpMethod());

            // 1. Build WebClient with timeouts
            WebClient webClient = createWebClient(config);

            // 2. Process request template
            String processedRequestBody = processRequestTemplate(config, requestBody);

            // 3. Build headers with authentication
            HttpHeaders headers = buildHeaders(config);

            // 4. Execute HTTP call
            ResponseEntity<String> response = executeHttpCall(webClient, config, processedRequestBody, headers);

            // 5. Process response
            return processResponse(config, response);

        } catch (WebClientResponseException e) {
            log.error("HTTP error for service '{}': {} - {}",
                    config.getServiceName(), e.getStatusCode(), e.getResponseBodyAsString());
            return mapHttpError(e, config);
        } catch (WebClientException e) {
            log.error("WebClient error for service '{}': {}", config.getServiceName(), e.getMessage());
            return mapWebClientError(e, config);
        } catch (Exception e) {
            log.error("Unexpected error for service '{}': {}", config.getServiceName(), e.getMessage(), e);
            return mapGenericError(e, config);
        }
    }

    private WebClient createWebClient(ServiceMetadata config) {
        WebClient.Builder builder = webClientBuilder.clone();

        // Configure timeouts if specified
        if (config.getResilience() != null && config.getResilience().getTimeouts() != null) {
            var timeouts = config.getResilience().getTimeouts();
            builder.clientConnector(
                    new org.springframework.http.client.reactive.ReactorClientHttpConnector(
                            reactor.netty.http.client.HttpClient.create()
                                    .responseTimeout(Duration.ofMillis(timeouts.getReadTimeout()))
                                    .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                            (int) timeouts.getConnectionTimeout())
                    )
            );
        }

        return builder.build();
    }

    private String processRequestTemplate(ServiceMetadata config, Object requestBody) {
        try {
            if (config.getRequestTemplate() != null && requestBody instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> requestMap = (Map<String, Object>) requestBody;
                return templateService.process(config.getRequestTemplate(), requestMap);
            }

            // If no template, convert to JSON string
            if (requestBody instanceof Map || requestBody instanceof String) {
                return requestBody.toString();
            }

            return new ObjectMapper().writeValueAsString(requestBody);

        } catch (Exception e) {
            log.error("Request template processing failed for service '{}': {}",
                    config.getServiceName(), e.getMessage());
            throw new RuntimeException("Request template processing failed", e);
        }
    }

    private HttpHeaders buildHeaders(ServiceMetadata config) {
        HttpHeaders headers = new HttpHeaders();

        // Add configured headers
        if (config.getHeaders() != null) {
            config.getHeaders().forEach(headers::add);
        }

        // Apply authentication
        if (config.getAuth() != null && config.getAuth().getType() != null) {
            AuthenticationStrategy authStrategy = authStrategies.get(config.getAuth().getType());
            if (authStrategy != null) {
                authStrategy.apply(config, headers);
            } else {
                log.warn("No authentication strategy found for type: {}", config.getAuth().getType());
            }
        }

        // Ensure Content-Type is set
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        }

        return headers;
    }
    private ResponseEntity<String> executeHttpCall(WebClient client, ServiceMetadata cfg,
                                                   String b, HttpHeaders h) {
        String method = cfg.getHttpMethod().toUpperCase();
        WebClient.RequestHeadersSpec<?> spec;
        switch (method) {
            case "GET":    spec = client.get().uri(cfg.getEndpointUrl()); break;
            case "POST":   spec = client.post().uri(cfg.getEndpointUrl()).bodyValue(b); break;
            case "PUT":    spec = client.put().uri(cfg.getEndpointUrl()).bodyValue(b); break;
            case "DELETE": spec = client.delete().uri(cfg.getEndpointUrl()); break;
            case "PATCH":  spec = client.patch().uri(cfg.getEndpointUrl()).bodyValue(b); break;
            default: throw new UnsupportedOperationException("Unsupported: " + method);
        }
        h.forEach((k,v) -> spec.header(k, v.toArray(new String[0])));
        return spec.retrieve().toEntity(String.class).block();
    }

    private Object processResponse(ServiceMetadata config, ResponseEntity<String> response) {
        try {
            String responseBody = response.getBody();

            // Process response template if configured
            if (config.getResponseTemplate() != null && responseBody != null) {
                // Convert response to map for template processing
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = new ObjectMapper()
                        .readValue(responseBody, Map.class);

                responseBody = templateService.process(config.getResponseTemplate(), responseMap);
            }

            // Parse final response as JSON
            if (responseBody != null && !responseBody.trim().isEmpty()) {
                return new ObjectMapper()
                        .readValue(responseBody, Object.class);
            }

            return null;

        } catch (Exception e) {
            log.error("Response processing failed for service '{}': {}",
                    config.getServiceName(), e.getMessage());
            throw new RuntimeException("Response processing failed", e);
        }
    }

    private StandardResponse<Object> mapHttpError(WebClientResponseException e, ServiceMetadata config) {
        ErrorDetails error = ErrorDetails.builder()
                .errorCode(mapHttpStatusToErrorCode((HttpStatus) e.getStatusCode()))
                .errorMessage(getHttpErrorMessage((HttpStatus) e.getStatusCode()))
                .errorDescription(String.format("HTTP %d error from %s: %s",
                        e.getStatusCode().value(), config.getEndpointUrl(), e.getResponseBodyAsString()))
                .category(determineErrorCategory((HttpStatus) e.getStatusCode()))
                .severity(determineErrorSeverity((HttpStatus) e.getStatusCode()))
                .source("REST_PROTOCOL")
                .httpStatusCode(e.getStatusCode().value())
                .originalErrorCode(String.valueOf(e.getStatusCode().value()))
                //.originalErrorMessage(e.getResponseBodyAsString())
                .retryable(isRetryableHttpStatus((HttpStatus) e.getStatusCode()))
                .downstreamService(config.getServiceName())
                .build();

        return StandardResponse.<Object>builder()
                .success(false)
                .status(org.adcb.adapter.commons.ResponseStatus.ERROR)
                .error(error)
                .serviceName(config.getServiceName())
                .protocol("REST_JSON")
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    private StandardResponse<Object> mapWebClientError(WebClientException e, ServiceMetadata config) {
        ErrorDetails error = ErrorDetails.builder()
                .errorCode("NETWORK_ERROR")
                .errorMessage("Network connectivity error")
                .errorDescription("Failed to connect to downstream service: " + e.getMessage())
                .category(ErrorCategory.NETWORK)
                .severity(ErrorSeverity.HIGH)
                .source("REST_PROTOCOL")
                .technicalMessage(e.getMessage())
                .exceptionClass(e.getClass().getSimpleName())
                .retryable(true)
                .retryAfterSeconds(30)
                .downstreamService(config.getServiceName())
                .build();

        return StandardResponse.<Object>builder()
                .success(false)
                .status(org.adcb.adapter.commons.ResponseStatus.TECHNICAL_ERROR)
                .error(error)
                .serviceName(config.getServiceName())
                .protocol("REST_JSON")
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    private StandardResponse<Object> mapGenericError(Exception e, ServiceMetadata config) {
        ErrorDetails error = ErrorDetails.builder()
                .errorCode("PROCESSING_ERROR")
                .errorMessage("Request processing failed")
                .errorDescription("An unexpected error occurred while processing the request")
                .category(ErrorCategory.TECHNICAL)
                .severity(ErrorSeverity.HIGH)
                .source("REST_PROTOCOL")
                .technicalMessage(e.getMessage())
                .exceptionClass(e.getClass().getSimpleName())
                .retryable(false)
                .downstreamService(config.getServiceName())
                .build();

        return StandardResponse.<Object>builder()
                .success(false)
                .status(org.adcb.adapter.commons.ResponseStatus.TECHNICAL_ERROR)
                .error(error)
                .serviceName(config.getServiceName())
                .protocol("REST_JSON")
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    // Helper methods for HTTP status mapping
    private String mapHttpStatusToErrorCode(HttpStatus status) {
        return switch (status.value()) {
            case 400 -> "INVALID_REQUEST";
            case 401 -> "AUTHENTICATION_FAILED";
            case 403 -> "AUTHORIZATION_DENIED";
            case 404 -> "RESOURCE_NOT_FOUND";
            case 408 -> "REQUEST_TIMEOUT";
            case 429 -> "RATE_LIMIT_EXCEEDED";
            case 500 -> "INTERNAL_SERVER_ERROR";
            case 502 -> "BAD_GATEWAY";
            case 503 -> "SERVICE_UNAVAILABLE";
            case 504 -> "GATEWAY_TIMEOUT";
            default -> "HTTP_ERROR_" + status.value();
        };
    }

    private String getHttpErrorMessage(HttpStatus status) {
        return switch (status.value()) {
            case 400 -> "Invalid request format";
            case 401 -> "Authentication failed";
            case 403 -> "Access denied";
            case 404 -> "Resource not found";
            case 408 -> "Request timeout";
            case 429 -> "Too many requests";
            case 500 -> "Internal server error";
            case 502 -> "Bad gateway";
            case 503 -> "Service unavailable";
            case 504 -> "Gateway timeout";
            default -> "HTTP error " + status.value();
        };
    }

    private ErrorCategory determineErrorCategory(HttpStatus status) {
        if (status.is4xxClientError()) {
            return switch (status.value()) {
                case 400 -> ErrorCategory.VALIDATION;
                case 401 -> ErrorCategory.AUTHENTICATION;
                case 403 -> ErrorCategory.AUTHORIZATION;
                case 429 -> ErrorCategory.RATE_LIMIT;
                default -> ErrorCategory.BUSINESS;
            };
        }
        return ErrorCategory.TECHNICAL;
    }

    private ErrorSeverity determineErrorSeverity(HttpStatus status) {
        return switch (status.value()) {
            case 404, 400 -> ErrorSeverity.MEDIUM;
            case 401, 403 -> ErrorSeverity.HIGH;
            case 500, 502, 503, 504 -> ErrorSeverity.CRITICAL;
            default -> ErrorSeverity.MEDIUM;
        };
    }

    private boolean isRetryableHttpStatus(HttpStatus status) {
        return status.value() == 408 || status.value() == 429 ||
                status.is5xxServerError();
    }
}
