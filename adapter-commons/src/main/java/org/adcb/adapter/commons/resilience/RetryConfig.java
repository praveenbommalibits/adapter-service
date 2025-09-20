package org.adcb.adapter.commons.resilience;

import lombok.Data;

import java.util.List;

@Data
public class RetryConfig {
    private boolean enabled;
    private int maxAttempts;
    private String strategy; // FIXED_INTERVAL, EXPONENTIAL_BACKOFF
    private long initialInterval;
    private double multiplier;
    private long maxInterval;
    private List<String> retryableExceptions;
}