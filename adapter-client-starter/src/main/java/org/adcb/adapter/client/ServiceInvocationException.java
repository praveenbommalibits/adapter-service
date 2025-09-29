package org.adcb.adapter.client;

/**
 * Exception thrown when adapter service invocation fails.
 *
 * <p>This exception wraps underlying failures from the adapter gateway,
 * providing a consistent error handling interface for client applications.
 *
 * @since 1.0.0
 */
public class ServiceInvocationException extends RuntimeException {

    private final String serviceName;

    public ServiceInvocationException(String message, Throwable cause) {
        super(message, cause);
        this.serviceName = extractServiceName(message);
    }

    public ServiceInvocationException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
    }

    /**
     * Gets the service name that failed (if available).
     *
     * @return service name or null if not available
     */
    public String getServiceName() {
        return serviceName;
    }

    private String extractServiceName(String message) {
        if (message != null && message.contains("adapter service: ")) {
            return message.substring(message.indexOf("adapter service: ") + 17);
        }
        return null;
    }
}
