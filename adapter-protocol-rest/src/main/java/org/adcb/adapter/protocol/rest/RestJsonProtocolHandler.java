package org.adcb.adapter.protocol.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.adcb.adapter.commons.*;
import org.adcb.adapter.spi.ProtocolHandler;
import org.adcb.adapter.spi.auth.AuthenticationStrategy;
import org.adcb.adapter.transform.TemplateService;
import org.adcb.adapter.transform.exception.TemplateProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.*;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST JSON protocol handler using Spring WebFlux WebClient.
 *
 * <p>This handler processes all HTTP methods, expanding path parameters and
 * rendering request/response templates. It integrates authentication strategies,
 * resilience timeouts, and maps errors into StandardResponse.
 *
 * @since 1.0
 */
@Component("REST_JSON")
@Slf4j
public class RestJsonProtocolHandler implements ProtocolHandler {

    private final WebClient.Builder webClientBuilder;
    private final Map<String, AuthenticationStrategy> authStrategies;
    private final TemplateService templateService;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            log.debug("Executing REST call to {} via {}", config.getEndpointUrl(), config.getHttpMethod());

            // 1. Build WebClient with configured timeouts
            WebClient client = createWebClient(config);

            // 2. Render request body template if available
            String body = renderRequestBody(config, requestBody);

            // 3. Build headers including authentication
            HttpHeaders headers = buildHeaders(config);

            // 4. Execute the HTTP call, expanding path params and sending body
            String responseString = executeHttpCall(client, config, requestBody, body, headers);

            // 5. Apply response template if configured, then parse JSON
            return renderResponse(config, responseString);

        } catch (WebClientResponseException e) {
            return mapHttpError(e, config);
        } catch (WebClientException e) {
            return mapWebClientError(e, config);
        } catch (Exception e) {
            return mapGenericError(e, config);
        }
    }

    // Builds WebClient with optional connection/read timeouts
    private WebClient createWebClient(ServiceMetadata cfg) {
        WebClient.Builder builder = webClientBuilder.clone();
        if (cfg.getResilience() != null && cfg.getResilience().getTimeouts() != null) {
            var t = cfg.getResilience().getTimeouts();
            builder.clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                            .responseTimeout(Duration.ofMillis(t.getReadTimeout()))
                            .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                    (int) t.getConnectionTimeout())
            ));
        }
        return builder.build();
    }

    // Renders request body via TemplateService if requestTemplate provided
    private String renderRequestBody(ServiceMetadata cfg, Object requestBody) throws TemplateProcessingException {
        if (cfg.getRequestTemplate() != null && requestBody instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String,Object> ctx = (Map<String,Object>)requestBody;
            return templateService.process(cfg.getRequestTemplate(), ctx);
        }
        if (requestBody instanceof String) {
            return (String) requestBody;
        }
        if (requestBody != null) {
            try {
                return objectMapper.writeValueAsString(requestBody);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize request body", e);
            }
        }
        return null;
    }

    // Builds HTTP headers and applies authentication strategy
    private HttpHeaders buildHeaders(ServiceMetadata cfg) {
        HttpHeaders headers = new HttpHeaders();
        if (cfg.getHeaders() != null) {
            cfg.getHeaders().forEach(headers::add);
        }
        if (cfg.getAuth() != null && cfg.getAuth().getType() != null) {
            AuthenticationStrategy strat = authStrategies.get(cfg.getAuth().getType());
            if (strat != null) strat.apply(cfg, headers);
        }
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        }
        return headers;
    }

    /**
     * Executes HTTP call supporting path parameters and request body:
     * - GET/DELETE expand path variables into URL
     * - POST/PUT/PATCH expand path variables then include body
     */
    private String executeHttpCall(WebClient client,
                                   ServiceMetadata cfg,
                                   Object requestBody,
                                   String body,
                                   HttpHeaders headers) {
        String urlTemplate = cfg.getEndpointUrl();
        @SuppressWarnings("unchecked")
        Map<String,Object> params = requestBody instanceof Map ? (Map<String,Object>)requestBody : Map.of();
        // Expand {var} placeholders by simple replace
        for (Map.Entry<String,Object> e : params.entrySet()) {
            urlTemplate = urlTemplate.replace("{" + e.getKey() + "}", e.getValue().toString());
        }

        WebClient.RequestBodyUriSpec spec = switch(cfg.getHttpMethod().toUpperCase()) {
            case "POST"   -> client.post();
            case "PUT"    -> client.put();
            case "PATCH"  -> client.patch();
            //case "DELETE" -> client.delete().bodyValue("");  // DELETE rarely has a body
            default       -> client.method(HttpMethod.valueOf(cfg.getHttpMethod().toUpperCase()));
        };

        // Set URI and headers
        WebClient.RequestHeadersSpec<?> req = spec.uri(urlTemplate)
                .headers(h -> h.addAll(headers));

        // Attach body only for methods with request bodies
        if (switch(cfg.getHttpMethod().toUpperCase()) {
            case "POST","PUT","PATCH" -> true;
            default                   -> false;
        }) {
            req = ((WebClient.RequestBodySpec)req).bodyValue(body);
        }

        /*Mono<ClientResponse> respMono = req.exchange();
        ClientResponse resp = respMono.block();
        return resp.bodyToMono(String.class).block();*/
        return req.retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // Applies response template then parses JSON to Object
    /*private Object renderResponse(ServiceMetadata cfg, String respStr) throws Exception {
        if (cfg.getResponseTemplate() != null && respStr != null) {
            @SuppressWarnings("unchecked")
            Map<String,Object> ctx = objectMapper.readValue(respStr, Map.class);
            respStr = templateService.process(cfg.getResponseTemplate(), ctx);
        }
        return respStr != null && !respStr.isBlank()
                ? objectMapper.readValue(respStr, Object.class)
                : null;
    }*/

    // Applies response template then parses JSON to Object
    private Object renderResponse(ServiceMetadata cfg, String respStr) throws Exception {
        if (respStr == null || respStr.isBlank()) {
            return null;
        }

        // 1. Parse downstream JSON into a flat Map context
        @SuppressWarnings("unchecked")
        Map<String,Object> ctx = objectMapper.readValue(respStr, Map.class);

        // 2. Enrich context with system variables for template usage
        ctx.put("currentTimeISO", java.time.LocalDateTime.now().toString());
        ctx.put("systemName", "ADCB_ADAPTER");
        ctx.put("version", "1.0");

        // 3. Apply Freemarker template if configured
        if (cfg.getResponseTemplate() != null) {
            String templated = templateService.process(cfg.getResponseTemplate(), ctx);
            // 4. Parse the templated JSON into a Java object
            return objectMapper.readValue(templated, Object.class);
        }

        // 5. No template: return the full parsed Map
        return ctx;
    }


    // Maps WebClientResponseException to StandardResponse
    private StandardResponse<Object> mapHttpError(WebClientResponseException e, ServiceMetadata cfg) {
        ErrorDetails err = ErrorDetails.builder()
                .errorCode("HTTP_" + e.getStatusCode().value())
                .errorMessage(e.getStatusText())
                .errorDescription(e.getResponseBodyAsString())
                .category(ErrorCategory.TECHNICAL)
                .severity(ErrorSeverity.HIGH)
                .retryable(e.getStatusCode().is5xxServerError())
                .downstreamService(cfg.getServiceName())
                .httpStatusCode(e.getRawStatusCode())
                .build();
        return StandardResponse.<Object>builder()
                .success(false)
                .status(ResponseStatus.ERROR)
                .error(err)
                .serviceName(cfg.getServiceName())
                .protocol("REST_JSON")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Maps network errors to StandardResponse
    private StandardResponse<Object> mapWebClientError(WebClientException e, ServiceMetadata cfg) {
        ErrorDetails err = ErrorDetails.builder()
                .errorCode("NETWORK_ERROR")
                .errorMessage("Connectivity issue")
                .errorDescription(e.getMessage())
                .category(ErrorCategory.NETWORK)
                .severity(ErrorSeverity.HIGH)
                .retryable(true)
                .downstreamService(cfg.getServiceName())
                .build();
        return StandardResponse.<Object>builder()
                .success(false)
                .status(ResponseStatus.TECHNICAL_ERROR)
                .error(err)
                .serviceName(cfg.getServiceName())
                .protocol("REST_JSON")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Catches all other exceptions
    private StandardResponse<Object> mapGenericError(Exception e, ServiceMetadata cfg) {
        ErrorDetails err = ErrorDetails.builder()
                .errorCode("PROCESSING_ERROR")
                .errorMessage("Handler failure")
                .errorDescription(e.getMessage())
                .category(ErrorCategory.TECHNICAL)
                .severity(ErrorSeverity.CRITICAL)
                .downstreamService(cfg.getServiceName())
                .build();
        return StandardResponse.<Object>builder()
                .success(false)
                .status(ResponseStatus.TECHNICAL_ERROR)
                .error(err)
                .serviceName(cfg.getServiceName())
                .protocol("REST_JSON")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
