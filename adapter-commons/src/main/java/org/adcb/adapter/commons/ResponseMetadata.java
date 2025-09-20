package org.adcb.adapter.commons;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ResponseMetadata {
    private String version;
    private String environment;
    private Map<String,String> headers;
    private String requestId;
    private String sessionId;
    private LocalDateTime processedAt;
}
