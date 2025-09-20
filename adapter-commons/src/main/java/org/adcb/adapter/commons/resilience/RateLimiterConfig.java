package org.adcb.adapter.commons.resilience;

import lombok.Data;

@Data
public class RateLimiterConfig {
    private boolean enabled;
    private int permitsPerSecond;
    private long timeoutDuration;
}