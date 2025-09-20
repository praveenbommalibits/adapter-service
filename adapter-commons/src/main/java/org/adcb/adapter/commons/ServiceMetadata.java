package org.adcb.adapter.commons;

import lombok.Data;
import org.adcb.adapter.commons.auth.AuthConfig;
import org.adcb.adapter.commons.resilience.ResilienceConfig;

import java.util.Map;

/**
 * Holds all configuration for a downstream service, including auth, resilience, templates, etc.
 */
@Data
public class ServiceMetadata {
    // Core settings
    private String serviceName;
    private String protocol;
    private String endpointUrl;
    private String requestTemplate;
    private String responseTemplate;
    private String httpMethod;
    private Map<String, String> headers;

    // Authentication settings
    private AuthConfig auth;

    // Resilience settings
    private ResilienceConfig resilience;

    // SOAP error extraction
    private String errorCodeXPath;
    private String errorDescriptionXPath;

    // Runtime fields
    private Integer lastHttpStatus;
    private String lastErrorDescription;
}
