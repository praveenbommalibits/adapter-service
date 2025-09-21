package org.adcb.adapter.protocol.soap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.adcb.adapter.commons.*;
import org.adcb.adapter.spi.ProtocolHandler;
import org.adcb.adapter.spi.auth.AuthenticationStrategy;
import org.adcb.adapter.transform.TemplateService;
import org.adcb.adapter.transform.exception.TemplateProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * SOAP protocol handler using WebClient for HTTP transport.
 *
 * <p>This handler processes SOAP-based service calls with full support for:
 * <ul>
 *   <li>XML request template processing using Freemarker</li>
 *   <li>SOAP envelope construction and namespace handling</li>
 *   <li>XML response parsing to nested Map structure</li>
 *   <li>JSON response template transformation</li>
 *   <li>Authentication strategy integration</li>
 *   <li>Comprehensive error handling and mapping</li>
 *   <li>Timeout and connection management</li>
 * </ul>
 *
 * <p>The handler transforms SOAP XML responses into JSON-structured StandardResponse objects,
 * ensuring consistent error handling and data format across all SOAP integrations.
 *
 * <p>Flow:
 * 1. Process request template (XML) with request data
 * 2. Send SOAP request via WebClient
 * 3. Parse XML response into nested Map
 * 4. Apply JSON response template to extract/transform needed fields
 * 5. Return structured Java object for StandardResponse wrapping
 *
 * @since 1.0
 */
@Component("SOAP")
@Slf4j
public class SoapProtocolHandler implements ProtocolHandler {

    private final WebClient.Builder webClientBuilder;
    private final Map<String, AuthenticationStrategy> authStrategies;
    private final TemplateService templateService;
    private final XmlMapper xmlMapper = new XmlMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public SoapProtocolHandler(WebClient.Builder webClientBuilder,
                               Map<String, AuthenticationStrategy> authStrategies,
                               TemplateService templateService) {
        this.webClientBuilder = webClientBuilder;
        this.authStrategies = authStrategies;
        this.templateService = templateService;

        // Configure XML mapper for better namespace and structure handling
        this.xmlMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Executes SOAP service call with complete request/response processing.
     *
     * @param config      Service metadata containing endpoint, templates, auth config
     * @param requestBody Request data map containing template variables
     * @return Processed response object ready for StandardResponse wrapping
     */
    @Override
    public Object execute(ServiceMetadata config, Object requestBody) {
        try {
            log.debug("Executing SOAP call to: {}", config.getEndpointUrl());

            // 1. Build WebClient with timeouts
            WebClient webClient = createWebClient(config);

            // 2. Process XML request template
            String soapXmlRequest = renderSoapRequest(config, requestBody);
            log.debug("Generated SOAP request: {}", soapXmlRequest);

            // 3. Build headers with authentication
            HttpHeaders headers = buildHeaders(config);

            // 4. Execute SOAP call
            String soapXmlResponse = executeSoapCall(webClient, config, soapXmlRequest, headers);
            log.debug("Received SOAP response: {}", soapXmlResponse);

            // 5. Process and transform response
            return processSoapResponse(config, soapXmlResponse);

        } catch (WebClientResponseException e) {
            log.error("SOAP HTTP error for service '{}': {} - {}",
                    config.getServiceName(), e.getStatusCode(), e.getResponseBodyAsString());
            return mapHttpError(e, config);
        } catch (WebClientException e) {
            log.error("SOAP WebClient error for service '{}': {}", config.getServiceName(), e.getMessage());
            return mapWebClientError(e, config);
        } catch (Exception e) {
            log.error("Unexpected SOAP error for service '{}': {}", config.getServiceName(), e.getMessage(), e);
            return mapGenericError(e, config);
        }
    }

    /**
     * Creates WebClient with configured timeouts for SOAP operations.
     */
    private WebClient createWebClient(ServiceMetadata config) {
        WebClient.Builder builder = webClientBuilder.clone();

        // Configure timeouts if specified
        if (config.getResilience() != null && config.getResilience().getTimeouts() != null) {
            var timeouts = config.getResilience().getTimeouts();
            builder.clientConnector(
                    new ReactorClientHttpConnector(
                            HttpClient.create()
                                    .responseTimeout(Duration.ofMillis(timeouts.getReadTimeout()))
                                    .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                            (int) timeouts.getConnectionTimeout())
                    )
            );
        }

        return builder.build();
    }

    /**
     * Renders SOAP XML request using Freemarker template and request data.
     *
     * @param config      Service configuration containing request template path
     * @param requestBody Request data map for template variable substitution
     * @return Complete SOAP XML envelope as string
     */
    private String renderSoapRequest(ServiceMetadata config, Object requestBody)
            throws TemplateProcessingException {

        if (config.getRequestTemplate() == null) {
            throw new IllegalArgumentException("SOAP request template is required");
        }

        if (requestBody instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> requestMap = (Map<String, Object>) requestBody;
            return templateService.process(config.getRequestTemplate(), requestMap);
        }

        throw new IllegalArgumentException("SOAP request body must be a Map for template processing");
    }

    /**
     * Builds HTTP headers including SOAP-specific headers and authentication.
     */
    private HttpHeaders buildHeaders(ServiceMetadata config) {
        HttpHeaders headers = new HttpHeaders();

        // Add configured headers (Content-Type, SOAPAction, etc.)
        if (config.getHeaders() != null) {
            config.getHeaders().forEach(headers::add);
        }

        // Apply authentication
        if (config.getAuth() != null && config.getAuth().getType() != null && !"NONE".equals(config.getAuth().getType())) {
            AuthenticationStrategy authStrategy = authStrategies.get(config.getAuth().getType());
            if (authStrategy != null) {
                authStrategy.apply(config, headers);
            } else {
                log.warn("No authentication strategy found for type: {}", config.getAuth().getType());
            }
        }

        // Ensure SOAP Content-Type is set
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.add(HttpHeaders.CONTENT_TYPE, "text/xml; charset=utf-8");
        }

        return headers;
    }

    /**
     * Executes SOAP HTTP call using WebClient.
     *
     * @param client      Configured WebClient instance
     * @param config      Service configuration
     * @param xmlRequest  SOAP XML request body
     * @param headers     HTTP headers including SOAP headers
     * @return Raw SOAP XML response as string
     */
    private String executeSoapCall(WebClient client, ServiceMetadata config,
                                   String xmlRequest, HttpHeaders headers) {

        return client.post()
                .uri(config.getEndpointUrl())
                .headers(h -> h.addAll(headers))
                .bodyValue(xmlRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * Processes SOAP XML response into structured Java object.
     *
     * <p>Processing steps:
     * 1. Parse XML response into nested Map structure
     * 2. Flatten/normalize the XML structure for template access
     * 3. Add system variables for template processing
     * 4. Apply JSON response template to extract needed fields
     * 5. Parse final JSON into Java object
     *
     * @param config           Service configuration with response template
     * @param soapXmlResponse  Raw SOAP XML response string
     * @return Processed object ready for StandardResponse payload
     */
    private Object processSoapResponse(ServiceMetadata config, String soapXmlResponse) throws Exception {
        if (soapXmlResponse == null || soapXmlResponse.trim().isEmpty()) {
            return null;
        }

        // 1. Parse SOAP XML into nested Map structure
        @SuppressWarnings("unchecked")
        Map<String, Object> xmlMap = xmlMapper.readValue(soapXmlResponse, Map.class);
        log.debug("Full XML structure: {}", xmlMap);

        // 2. Extract SOAP Body content for easier template access
        Map<String, Object> templateContext = extractSoapBody(xmlMap);
        log.debug("Extracted template context: {}", templateContext);

        // 3. Add system variables for template processing
        templateContext.put("currentTimeISO", LocalDateTime.now().toString());
        templateContext.put("systemName", "ADCB_ADAPTER");
        templateContext.put("version", "1.0");

        log.debug("SOAP template context keys: {}", templateContext.keySet());

        // 4. Apply JSON response template if configured
        if (config.getResponseTemplate() != null) {
            String jsonResponse = templateService.process(config.getResponseTemplate(), templateContext);
            log.debug("Templated JSON response: {}", jsonResponse);

            // 5. Parse templated JSON into Java object
            return objectMapper.readValue(jsonResponse, Object.class);
        }

        // 6. No template: return the extracted SOAP body structure
        return templateContext;
    }

    /**
     * Extracts and flattens SOAP Body content for easier template variable access.
     *
     * <p>Converts nested XML structure like:
     * <pre>
     * {
     *   "Envelope": {
     *     "Body": {
     *       "getUserDetailsResponse": {
     *         "user": { ... },
     *         "success": true
     *       }
     *     }
     *   }
     * }
     * </pre>
     * Into flattened structure:
     * <pre>
     * {
     *   "getUserDetailsResponse": {
     *     "user": { ... },
     *     "success": true
     *   }
     * }
     * </pre>
     *
     * @param xmlMap Parsed XML Map structure
     * @return Flattened Map with SOAP Body content at root level
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractSoapBody(Map<String, Object> xmlMap) {
        log.debug("Starting XML structure extraction from: {}", xmlMap.keySet());
        // Navigate to Envelope -> Body
        Map<String, Object> envelope = (Map<String, Object>)
                xmlMap.getOrDefault("Envelope", xmlMap.get("SOAP-ENV:Envelope"));
        if (envelope == null) return xmlMap;

        Map<String, Object> body = (Map<String, Object>)
                envelope.getOrDefault("Body", envelope.get("SOAP-ENV:Body"));
        if (body == null) return xmlMap;

        // If Body has exactly one child (the response element), return that child's map
        if (body.size() == 1) {
            Object responseElement = body.values().iterator().next();
            if (responseElement instanceof Map) {
                return (Map<String, Object>) responseElement;
            }
        }
        // Otherwise return the Body map itself
        return body;
    }


    /**
     * Maps HTTP errors to StandardResponse format.
     */
    private StandardResponse<Object> mapHttpError(WebClientResponseException e, ServiceMetadata config) {
        ErrorDetails error = ErrorDetails.builder()
                .errorCode("SOAP_HTTP_ERROR")
                .errorMessage("SOAP HTTP error")
                .errorDescription("HTTP " + e.getStatusCode().value() + " error from SOAP endpoint: " +
                        e.getResponseBodyAsString())
                .category(ErrorCategory.TECHNICAL)
                .severity(ErrorSeverity.HIGH)
                .source("SOAP_PROTOCOL")
                .httpStatusCode(e.getStatusCode().value())
                .originalErrorCode(String.valueOf(e.getStatusCode().value()))
                .retryable(e.getStatusCode().is5xxServerError())
                .downstreamService(config.getServiceName())
                .build();

        return StandardResponse.<Object>builder()
                .success(false)
                .status(ResponseStatus.ERROR)
                .error(error)
                .serviceName(config.getServiceName())
                .protocol("SOAP")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Maps WebClient network errors to StandardResponse format.
     */
    private StandardResponse<Object> mapWebClientError(WebClientException e, ServiceMetadata config) {
        ErrorDetails error = ErrorDetails.builder()
                .errorCode("SOAP_NETWORK_ERROR")
                .errorMessage("SOAP network connectivity error")
                .errorDescription("Failed to connect to SOAP endpoint: " + e.getMessage())
                .category(ErrorCategory.NETWORK)
                .severity(ErrorSeverity.HIGH)
                .source("SOAP_PROTOCOL")
                .technicalMessage(e.getMessage())
                .exceptionClass(e.getClass().getSimpleName())
                .retryable(true)
                .retryAfterSeconds(30)
                .downstreamService(config.getServiceName())
                .build();

        return StandardResponse.<Object>builder()
                .success(false)
                .status(ResponseStatus.TECHNICAL_ERROR)
                .error(error)
                .serviceName(config.getServiceName())
                .protocol("SOAP")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Maps generic exceptions to StandardResponse format.
     */
    private StandardResponse<Object> mapGenericError(Exception e, ServiceMetadata config) {
        ErrorDetails error = ErrorDetails.builder()
                .errorCode("SOAP_PROCESSING_ERROR")
                .errorMessage("SOAP request processing failed")
                .errorDescription("An unexpected error occurred while processing SOAP request")
                .category(ErrorCategory.TECHNICAL)
                .severity(ErrorSeverity.HIGH)
                .source("SOAP_PROTOCOL")
                .technicalMessage(e.getMessage())
                .exceptionClass(e.getClass().getSimpleName())
                .retryable(false)
                .downstreamService(config.getServiceName())
                .build();

        return StandardResponse.<Object>builder()
                .success(false)
                .status(ResponseStatus.TECHNICAL_ERROR)
                .error(error)
                .serviceName(config.getServiceName())
                .protocol("SOAP")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
