package org.adcb.adapter.spi.auth;

import lombok.extern.slf4j.Slf4j;
import org.adcb.adapter.commons.ServiceMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * OAuth2 Client Credentials authentication strategy.
 * Delegates token management to OAuth2TokenManager.
 */
@Component("OAUTH2")
@Slf4j
public class OAuth2ClientCredsStrategy implements AuthenticationStrategy {

    private final OAuth2TokenManager tokenManager;

    @Autowired
    public OAuth2ClientCredsStrategy(OAuth2TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public void apply(ServiceMetadata config, HttpHeaders headers) {
        if (config.getAuth() == null || config.getAuth().getTokenEndpoint() == null) {
            throw new IllegalArgumentException("OAuth2 requires tokenEndpoint configuration");
        }

        String token = tokenManager.getToken(
                config.getAuth().getTokenEndpoint(),
                config.getAuth().getClientId(),
                config.getAuth().getClientSecret(),
                config.getAuth().getScope()
        );

        headers.add("Authorization", "Bearer " + token);
        log.debug("Applied OAuth2 Bearer token for service: {}", config.getServiceName());
    }

    @Override
    public void refreshToken(ServiceMetadata config) {
        // Token refresh is handled automatically by OAuth2TokenManager
        log.debug("Token refresh handled by OAuth2TokenManager");
    }
}
