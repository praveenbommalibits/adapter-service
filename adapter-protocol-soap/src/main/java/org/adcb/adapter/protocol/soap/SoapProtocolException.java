package org.adcb.adapter.protocol.soap;

/**
 * Thrown for any SOAP-specific protocol errors.
 */
public class SoapProtocolException extends RuntimeException {
    public SoapProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
    public SoapProtocolException(String message) {
        super(message);
    }
}
