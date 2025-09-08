package org.adcb.adapter.gateway.exception;

/**
 * Custom runtime exception for gateway operations.
 */

public class AdapterException extends RuntimeException {
    public AdapterException(String message) {
        super(message);
    }
    public AdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}

