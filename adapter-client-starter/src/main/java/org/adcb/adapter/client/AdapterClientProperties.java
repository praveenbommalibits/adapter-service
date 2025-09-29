package org.adcb.adapter.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the ADCB Adapter Client.
 *
 * <p>These properties control the behavior of the adapter client
 * and can be configured via application.yml or application.properties.
 *
 * <p>Example configuration:
 * <pre>
 * adcb:
 *   adapter:
 *     client:
 *       enabled: true
 *       default-timeout: 30s
 *       log-requests: false
 *       log-responses: false
 * </pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "adcb.adapter.client")
@Data
public class AdapterClientProperties {

    /**
     * Whether the adapter client is enabled.
     */
    private boolean enabled = true;

    /**
     * Default timeout for service calls.
     */
    private String defaultTimeout = "30s";

    /**
     * Whether to log request payloads (be careful with sensitive data).
     */
    private boolean logRequests = false;

    /**
     * Whether to log response payloads (be careful with sensitive data).
     */
    private boolean logResponses = false;
}
