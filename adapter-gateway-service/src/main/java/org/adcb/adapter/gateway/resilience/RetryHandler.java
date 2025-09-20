package org.adcb.adapter.gateway.resilience;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.adcb.adapter.commons.ServiceMetadata;
import org.adcb.adapter.commons.resilience.RetryConfig;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * RetryHandler wraps a Supplier call in a Resilience4j Retry with
 * service-specific configuration from ServiceMetadata.
 *
 * <p>Configuration (in ServiceMetadata.resilience.retry):
 *  - enabled
 *  - maxAttempts
 *  - waitDuration (ms)
 *  - multiplier (for exponential backoff)
 *  - maxInterval (ms)
 *  - retryableExceptions (class names)
 */
@Component
public class RetryHandler {

    private final RetryRegistry retryRegistry = RetryRegistry.ofDefaults();

    /**
     * Executes the supplier with configured retry policy.
     *
     * @param config   service metadata containing RetryConfig
     * @param supplier the operation to execute
     * @param <T>      return type
     * @return result of supplier
     */
    public <T> T execute(ServiceMetadata config, Supplier<T> supplier) {
        var retryConfig = config.getResilience().getRetry();
        if (retryConfig == null || !retryConfig.isEnabled()) {
            return supplier.get();
        }

        // Build Resilience4j RetryConfig
        io.github.resilience4j.retry.RetryConfig rConfig = io.github.resilience4j.retry.RetryConfig.custom()
                .maxAttempts(retryConfig.getMaxAttempts())
                .waitDuration(Duration.ofMillis(retryConfig.getInitialInterval()))
                .intervalFunction(intervalFunction(retryConfig))
                .retryExceptions(resolveExceptionClasses(retryConfig.getRetryableExceptions()))
                .build();

        // Get or create Retry instance
        Retry retry = retryRegistry.retry(config.getServiceName(), rConfig);

        // Optionally subscribe to events for metrics/logging
        Retry.EventPublisher events = retry.getEventPublisher();
        events.onRetry(event -> {
            // log retry attempt
        });

        // Decorate and execute
        return Retry.decorateSupplier(retry, supplier).get();
    }

    // Helper to create interval function (fixed or exponential)
    private IntervalFunction intervalFunction(org.adcb.adapter.commons.resilience.RetryConfig rc) {
        if ("EXPONENTIAL_BACKOFF".equalsIgnoreCase(rc.getStrategy())) {
            return IntervalFunction.ofExponentialBackoff(
                    rc.getInitialInterval(), rc.getMultiplier(), rc.getMaxInterval());
        }
        // Default fixed wait
        return IntervalFunction.of(rc.getInitialInterval());
    }

    // Resolve exception class names to actual Class objects
    private Class<? extends Throwable>[] resolveExceptionClasses(java.util.List<String> names) {
        return names.stream().map(name -> {
            try {
                return Class.forName(name).asSubclass(Throwable.class);
            } catch (Exception e) {
                throw new RuntimeException("Invalid retryable exception: " + name, e);
            }
        }).toArray(Class[]::new);
    }
}
