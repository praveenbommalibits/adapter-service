package org.adcb.adapter.spi.auth;

import org.adcb.adapter.commons.ServiceMetadata;
import org.springframework.http.HttpHeaders;

/**
 * Strategy for applying authentication to outbound requests.
 */
public interface AuthenticationStrategy {
    /** Adds authentication headers or parameters */
    void apply(ServiceMetadata config, HttpHeaders headers);
    /** Refresh token if expired (no-op by default) */
    default void refreshToken(ServiceMetadata config) {}
}
