package org.adcb.adapter.commons.resilience;

import lombok.Data;

@Data
public class TimeoutConfig {
    private long connectionTimeout;
    private long readTimeout;
    private long totalTimeout;
}

