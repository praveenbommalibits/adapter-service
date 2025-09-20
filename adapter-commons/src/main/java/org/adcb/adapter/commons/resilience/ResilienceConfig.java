package org.adcb.adapter.commons.resilience;

import lombok.Data;
import java.util.List;

/**
 * Resilience configuration: circuit breaker, retry, timeout, rate limiter.
 */
@Data
public class ResilienceConfig {
    private CircuitBreakerConfig circuitBreaker;
    private RetryConfig retry;
    private TimeoutConfig timeouts;
    private RateLimiterConfig rateLimiter;
}
