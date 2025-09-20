package org.adcb.adapter.commons;


import lombok.*;
import java.util.Map;

/**
 * Detailed error information for failures.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetails {
    private String errorCode;
    private String errorMessage;
    private String errorDescription;
    private ErrorCategory category;
    private ErrorSeverity severity;
    private String source;
    private String technicalMessage;
    private String exceptionClass;
    private String businessRuleViolated;
    private Map<String,Object> validationErrors;
    private boolean retryable;
    private Integer retryAfterSeconds;
    private String downstreamService;
    private Integer httpStatusCode;
    private String originalErrorCode;
    private Map<String,Object> additionalContext;
    private String[] resolutionSteps;
    private String supportReferenceId;
}

