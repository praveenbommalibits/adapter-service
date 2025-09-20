package org.adcb.adapter.gateway.resilience;

import io.github.resilience4j.circuitbreaker.*;
import org.adcb.adapter.commons.ServiceMetadata;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Manages circuit breakers per service and provides execution wrapper.
 */
@Component
public class CircuitBreakerManager {

    private final CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
    private final ConcurrentHashMap<String, CircuitBreaker> breakers = new ConcurrentHashMap<>();

    /**
     * Executes supplier protected by circuit breaker for the given service.
     *
     * @param serviceName unique identifier for the circuit breaker
     * @param config service metadata with circuit breaker configuration
     * @param supplier the operation to execute
     * @return result of supplier execution
     * @throws Exception if circuit is open or operation fails
     */
    public <T> T execute(String serviceName, ServiceMetadata config, Supplier<T> supplier) {
        CircuitBreaker circuitBreaker = breakers.computeIfAbsent(serviceName, key ->
                createCircuitBreaker(key, config));

        return CircuitBreaker.decorateSupplier(circuitBreaker, supplier).get();
    }

    /**
     * Creates a circuit breaker with service-specific configuration.
     */
    private CircuitBreaker createCircuitBreaker(String serviceName, ServiceMetadata config) {
        var cbConfig = config.getResilience().getCircuitBreaker();

        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(cbConfig.getFailureRateThreshold())
                .waitDurationInOpenState(Duration.ofMillis(cbConfig.getWaitDurationInOpenStateMs()))
                .slidingWindowSize(cbConfig.getSlidingWindowSize())
                .minimumNumberOfCalls(cbConfig.getMinimumNumberOfCalls())
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        return registry.circuitBreaker(serviceName, circuitBreakerConfig);
    }

    /**
     * Gets current state of circuit breaker for monitoring.
     */
    public CircuitBreaker.State getState(String serviceName) {
        CircuitBreaker cb = breakers.get(serviceName);
        return cb != null ? cb.getState() : null;
    }

    /**
     * Gets circuit breaker metrics for monitoring.
     */
    public CircuitBreaker.Metrics getMetrics(String serviceName) {
        CircuitBreaker cb = breakers.get(serviceName);
        return cb != null ? cb.getMetrics() : null;
    }
}
