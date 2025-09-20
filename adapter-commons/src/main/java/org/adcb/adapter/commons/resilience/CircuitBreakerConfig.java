package org.adcb.adapter.commons.resilience;


import lombok.Data;


@Data
public class CircuitBreakerConfig {
    private boolean enabled;
    private float failureRateThreshold;
    private long waitDurationInOpenStateMs;
    private int slidingWindowSize;
    private int minimumNumberOfCalls;
}

