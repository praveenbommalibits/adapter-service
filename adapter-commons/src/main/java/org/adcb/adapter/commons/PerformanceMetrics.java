package org.adcb.adapter.commons;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PerformanceMetrics {
    private Long executionTimeMs;
    private Long downstreamCallTimeMs;
    private Long transformationTimeMs;
    private Long authenticationTimeMs;
    private Integer retryAttempts;
    private String circuitBreakerState;
    private Boolean cacheHit;
    private Long queueWaitTimeMs;
}