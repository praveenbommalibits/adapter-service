package org.adcb.adapter.transform.model;

import lombok.Data;

import java.time.Instant;

@Data
public class StandardResponse<T> {
    private boolean success;
    private String errorCode;
    private String errorDescription;
    private T payload;
    private String correlationId;
    private Instant timestamp;
}
