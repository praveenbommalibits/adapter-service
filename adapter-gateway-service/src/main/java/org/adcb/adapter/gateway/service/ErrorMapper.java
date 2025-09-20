package org.adcb.adapter.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.adcb.adapter.commons.ErrorDetails;
import org.adcb.adapter.commons.ErrorCategory;
import org.adcb.adapter.commons.ErrorSeverity;
import org.springframework.stereotype.Component;

/**
 * Maps various exceptions to standardized ErrorDetails.
 */
@Component
@Slf4j
public class ErrorMapper {

    public ErrorDetails mapError(Throwable throwable, String serviceName, String protocol) {

        return switch (throwable.getClass().getSimpleName()) {
            case "IllegalArgumentException" -> ErrorDetails.builder()
                    .errorCode("INVALID_CONFIGURATION")
                    .errorMessage("Service configuration error")
                    .errorDescription(throwable.getMessage())
                    .category(ErrorCategory.VALIDATION)
                    .severity(ErrorSeverity.HIGH)
                    .source("GATEWAY_SERVICE")
                    .technicalMessage(throwable.getMessage())
                    .exceptionClass(throwable.getClass().getSimpleName())
                    .retryable(false)
                    .downstreamService(serviceName)
                    .build();

            case "UnsupportedOperationException" -> ErrorDetails.builder()
                    .errorCode("UNSUPPORTED_OPERATION")
                    .errorMessage("Operation not supported")
                    .errorDescription(throwable.getMessage())
                    .category(ErrorCategory.TECHNICAL)
                    .severity(ErrorSeverity.MEDIUM)
                    .source("GATEWAY_SERVICE")
                    .technicalMessage(throwable.getMessage())
                    .exceptionClass(throwable.getClass().getSimpleName())
                    .retryable(false)
                    .downstreamService(serviceName)
                    .build();

            default -> ErrorDetails.builder()
                    .errorCode("UNEXPECTED_ERROR")
                    .errorMessage("An unexpected error occurred")
                    .errorDescription(throwable.getMessage())
                    .category(ErrorCategory.TECHNICAL)
                    .severity(ErrorSeverity.CRITICAL)
                    .source("GATEWAY_SERVICE")
                    .technicalMessage(throwable.getMessage())
                    .exceptionClass(throwable.getClass().getSimpleName())
                    .retryable(true)
                    .retryAfterSeconds(30)
                    .downstreamService(serviceName)
                    .build();
        };
    }
}
