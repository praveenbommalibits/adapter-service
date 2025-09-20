package org.adcb.adapter.commons.auth;

import lombok.Data;

/**
 * Authentication configuration for a service.
 */
@Data
public class AuthConfig {
    /** Type identifier for strategy: API_KEY, OAUTH2, JWT_BEARER, etc. */
    private String type;

    /** For API key: header vs query param strategy */
    private String strategy;
    private String keyName;
    private String tokenSource;

    // OAuth2-specific
    private String tokenEndpoint;
    private String clientId;
    private String clientSecret;
    private String scope;

    // JWT-specific and others can reuse tokenSource

    // Certificate-based (optional)
    private String keystorePath;
    private String keystorePassword;
    private String truststorePath;
    private String truststorePassword;
}
