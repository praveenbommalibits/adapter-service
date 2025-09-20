package org.adcb.adapter.transform.exception;

/**
 * Exception thrown when template processing fails.
 *
 * <p>This exception wraps underlying template engine errors and provides
 * contextual information about the failed operation.
 */
public class TemplateProcessingException extends Exception {

    public TemplateProcessingException(String message) {
        super(message);
    }

    public TemplateProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
