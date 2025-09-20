package org.adcb.adapter.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse<T> {
    private boolean success;
    private ResponseStatus status;
    private T payload;
    private ResponseMetadata metadata;
    private ErrorDetails error;
    private String correlationId;
    private LocalDateTime timestamp;
    private String serviceName;
    private String protocol;
    private PaginationInfo pagination;
    private PerformanceMetrics performance;
    private Map<String,String> warnings;
    private Map<String,String> links;

    public static <T> StandardResponse<T> success(T payload) {
        return StandardResponse.<T>builder()
                .success(true).status(ResponseStatus.SUCCESS)
                .payload(payload).timestamp(LocalDateTime.now())
                .build();
    }
    public static <T> StandardResponse<T> error(ErrorDetails err) {
        return StandardResponse.<T>builder()
                .success(false).status(ResponseStatus.ERROR)
                .error(err).timestamp(LocalDateTime.now())
                .build();
    }
}
