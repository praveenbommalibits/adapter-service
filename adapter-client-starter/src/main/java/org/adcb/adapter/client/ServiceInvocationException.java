package org.adcb.adapter.client;

/**
 * Exception thrown for any service invocation errors via adapter client.
 */
public class ServiceInvocationException extends RuntimeException {
    public ServiceInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
