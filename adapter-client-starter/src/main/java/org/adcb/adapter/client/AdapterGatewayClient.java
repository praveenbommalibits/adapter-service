package org.adcb.adapter.client;

import lombok.extern.slf4j.Slf4j;
import org.adcb.adapter.commons.StandardResponse;
import org.adcb.adapter.gateway.service.EnhancedProtocolAdapterService;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Simplified facade for client microservices to invoke adapter gateway services.
 *
 * <p>This client provides both synchronous and asynchronous methods to interact
 * with downstream services through the ADCB Adapter Gateway. It abstracts the
 * complexity of protocol handling, templating, and error management.
 *
 * <p>Key features:
 * <ul>
 *   <li>Synchronous and reactive API methods</li>
 *   <li>Automatic error handling and exception translation</li>
 *   <li>Built-in logging and correlation ID tracking</li>
 *   <li>Type-safe response handling with generics</li>
 *   <li>Simplified method signatures for common use cases</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private AdapterGatewayClient client;
 *
 * // Synchronous call
 * StandardResponse<?> result = client.invoke("user-api", Map.of("userId", "123"));
 *
 * // Reactive call
 * Mono<StandardResponse<?>> result = client.invokeAsync("user-api", Map.of("userId", "123"));
 *
 * // With custom timeout
 * StandardResponse<?> result = client.invokeWithTimeout("payment-api", params, Duration.ofSeconds(30));
 * }</pre>
 *
 * @since 1.0.0
 * @author ADCB Adapter Team
 */
@Slf4j
public class AdapterGatewayClient {

    private final EnhancedProtocolAdapterService protocolAdapterService;

    /**
     * Creates a new AdapterGatewayClient.
     *
     * @param protocolAdapterService the underlying adapter service
     * @throws IllegalArgumentException if protocolAdapterService is null
     */
    public AdapterGatewayClient(EnhancedProtocolAdapterService protocolAdapterService) {
        if (protocolAdapterService == null) {
            throw new IllegalArgumentException("EnhancedProtocolAdapterService cannot be null");
        }
        this.protocolAdapterService = protocolAdapterService;
        log.info("AdapterGatewayClient initialized successfully");
    }

    /**
     * Invokes a configured downstream service synchronously.
     *
     * @param serviceName   Service name as configured in application.yml
     * @param requestParams Request parameters to populate templates
     * @return StandardResponse containing success data or error details
     * @throws ServiceInvocationException if the service call fails
     * @throws IllegalArgumentException if serviceName is null or empty
     */
    public StandardResponse<?> invoke(String serviceName, Map<String, Object> requestParams) {
        validateServiceName(serviceName);
        log.debug("Invoking service '{}' with parameters: {}", serviceName, requestParams);

        try {
            StandardResponse<?> response = protocolAdapterService.invoke(serviceName, requestParams);
            log.debug("Service '{}' completed with status: {}", serviceName, response.getStatus());
            return response;
        } catch (Exception ex) {
            log.error("Failed to invoke service '{}': {}", serviceName, ex.getMessage(), ex);
            throw new ServiceInvocationException(
                    "Failed to invoke adapter service: " + serviceName, ex);
        }
    }

    /**
     * Invokes a configured downstream service reactively.
     *
     * @param serviceName   Service name as configured in application.yml
     * @param requestParams Request parameters to populate templates
     * @return Mono emitting StandardResponse with success data or error details
     * @throws IllegalArgumentException if serviceName is null or empty
     */
    public Mono<StandardResponse<?>> invokeAsync(String serviceName, Map<String, Object> requestParams) {
        validateServiceName(serviceName);
        log.debug("Invoking service '{}' asynchronously with parameters: {}", serviceName, requestParams);

        return Mono.<StandardResponse<?>>fromCallable(() ->
                        (StandardResponse<?>) protocolAdapterService.invoke(serviceName, requestParams)
                )
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(response ->
                        log.debug("Async service '{}' completed with status: {}", serviceName, response.getStatus())
                )
                .doOnError(error ->
                        log.error("Async service '{}' failed: {}", serviceName, error.getMessage())
                );
    }

    /**
     * Asynchronous with timeout - cancels operation if timeout exceeded
     * Use for: Long-running operations, unreliable external APIs, SLA enforcement
     */
   /* public CompletableFuture<StandardResponse<?>> invokeAsyncWithTimeout(
            String serviceName, Object requestData, Duration timeout) {

        log.debug("Asynchronous invocation with {}ms timeout requested for service: {}",
                timeout.toMillis(), serviceName);

        CompletableFuture<StandardResponse<?>> future = CompletableFuture.supplyAsync(() -> {
            try {
                return protocolAdapterService.invoke(serviceName, requestData);

            } catch (Exception e) {
                log.error("Timed async invocation failed for service '{}': {}", serviceName, e.getMessage(), e);
                return buildErrorResponse(serviceName, e, "ASYNC_TIMEOUT_INVOCATION_FAILED");
            }
        });

        // Apply timeout to the future
        return future.completeOnTimeout(
                buildTimeoutResponse(serviceName, timeout),
                timeout.toMillis(),
                TimeUnit.MILLISECONDS
        );
    }*/

    /**
     * Batch invocation - process multiple service calls in parallel
     * Use for: Data aggregation, parallel service calls, bulk operations
     */
    /*public CompletableFuture<Map<String, StandardResponse<?>>> invokeBatch(
            Map<String, Object> serviceRequests) {

        log.debug("Batch invocation requested for {} services", serviceRequests.size());

        Map<String, CompletableFuture<StandardResponse<?>>> futures = serviceRequests.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> invokeAsync(entry.getKey(), entry.getValue())
                ));

        return CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().join()
                        )));
    }
/*

    /**
     * Invokes a service and returns a CompletableFuture for easier integration with existing async code.
     *
     * @param serviceName   Service name as configured in application.yml
     * @param requestParams Request parameters to populate templates
     * @return CompletableFuture containing the StandardResponse
     */
    public CompletableFuture<StandardResponse<?>> invokeAsyncFuture(String serviceName, Map<String, Object> requestParams) {
        return invokeAsync(serviceName, requestParams).toFuture();
    }

    /**
     * Convenience method for simple GET-style service calls with a single parameter.
     *
     * @param serviceName Service name as configured in application.yml
     * @param paramName   Parameter name
     * @param paramValue  Parameter value
     * @return StandardResponse containing success data or error details
     */
    public StandardResponse<?> invokeWithParam(String serviceName, String paramName, Object paramValue) {
        return invoke(serviceName, Map.of(paramName, paramValue));
    }

    /**
     * Checks if a service is available and configured.
     *
     * @param serviceName Service name to check
     * @return true if service is configured, false otherwise
     */
    public boolean isServiceAvailable(String serviceName) {
        try {
            // This will throw an exception if service is not configured
            protocolAdapterService.invoke(serviceName, Map.of());
            return true;
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Service configuration not found")) {
                return false;
            }
            return true; // Service exists but call failed for other reasons
        } catch (Exception e) {
            return true; // Service exists but call failed
        }
    }

    private void validateServiceName(String serviceName) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Service name cannot be null or empty");
        }
    }
}
