package org.adcb.adapter.gateway.resilience;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.adcb.adapter.commons.ServiceMetadata;
import org.adcb.adapter.commons.resilience.RetryConfig;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.List;
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

    private final io.github.resilience4j.retry.RetryRegistry retryRegistry =
            io.github.resilience4j.retry.RetryRegistry.ofDefaults();

    public <T> T execute(ServiceMetadata config, Supplier<T> supplier) {
        var rc = config.getResilience().getRetry();
        if (rc == null || !rc.isEnabled()) {
            return supplier.get();
        }

        io.github.resilience4j.retry.RetryConfig rConfig = io.github.resilience4j.retry.RetryConfig.custom()
                .maxAttempts(rc.getMaxAttempts())
                // Remove waitDuration; use intervalFunction exclusively
                .intervalFunction(intervalFunction(rc))
                .retryExceptions(resolveExceptionClasses(rc.getRetryableExceptions()))
                .build();

        io.github.resilience4j.retry.Retry retry =
                retryRegistry.retry(config.getServiceName(), rConfig);

        // Optional: subscribe to retry events
        retry.getEventPublisher().onRetry(e -> {
            // log retry attempt if desired
        });

        return io.github.resilience4j.retry.Retry.decorateSupplier(retry, supplier).get();
    }

    private io.github.resilience4j.core.IntervalFunction intervalFunction(
            org.adcb.adapter.commons.resilience.RetryConfig rc) {
        if ("EXPONENTIAL_BACKOFF".equalsIgnoreCase(rc.getStrategy())) {
            return io.github.resilience4j.core.IntervalFunction
                    .ofExponentialBackoff(rc.getInitialInterval(), rc.getMultiplier(), rc.getMaxInterval());
        }
        return io.github.resilience4j.core.IntervalFunction.of(rc.getInitialInterval());
    }

    private Class<? extends Throwable>[] resolveExceptionClasses(List<String> names) {
        return names.stream().map(name -> {
            try {
                return Class.forName(name).asSubclass(Throwable.class);
            } catch (Exception e) {
                throw new RuntimeException("Invalid retryable exception: " + name, e);
            }
        }).toArray(Class[]::new);
    }
}
