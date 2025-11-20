package org.adcb.adapter.spi.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages OAuth2 token lifecycle: fetch, cache, refresh with 5-minute expiry buffer.
 */
@Component
@Slf4j
public class OAuth2TokenManager {

    private static final int EXPIRY_BUFFER_SECONDS = 300; // 5 minutes
    private final WebClient webClient = WebClient.create();
    private final Map<String, TokenCache> tokenStore = new ConcurrentHashMap<>();

    /**
     * Gets valid access token, fetching/refreshing as needed.
     */
    public String getToken(String tokenEndpoint, String clientId, String clientSecret, String scope) {
        String cacheKey = tokenEndpoint + ":" + clientId;
        TokenCache cached = tokenStore.get(cacheKey);

        if (cached != null && !cached.isExpired()) {
            log.debug("Using cached token for clientId: {}", clientId);
            return cached.accessToken;
        }

        log.info("Fetching new token from {} for clientId: {}", tokenEndpoint, clientId);
        return fetchToken(tokenEndpoint, clientId, clientSecret, scope, cacheKey);
    }

    private synchronized String fetchToken(String tokenEndpoint, String clientId, String clientSecret, 
                                          String scope, String cacheKey) {
        // Double-check after acquiring lock
        TokenCache cached = tokenStore.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.accessToken;
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        if (scope != null && !scope.isEmpty()) {
            body.add("scope", scope);
        }

        TokenResponse response = webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();

        if (response == null || response.accessToken == null) {
            throw new RuntimeException("Failed to fetch OAuth2 token from " + tokenEndpoint);
        }

        long expiresAt = Instant.now().getEpochSecond() + response.expiresIn - EXPIRY_BUFFER_SECONDS;
        tokenStore.put(cacheKey, new TokenCache(response.accessToken, expiresAt));

        log.info("Token fetched successfully for clientId: {}, expires in {}s (buffer: {}s)", 
                clientId, response.expiresIn, EXPIRY_BUFFER_SECONDS);
        return response.accessToken;
    }

    @Data
    static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        @JsonProperty("expires_in")
        private long expiresIn;
        @JsonProperty("token_type")
        private String tokenType;
    }

    static class TokenCache {
        final String accessToken;
        final long expiresAt;

        TokenCache(String accessToken, long expiresAt) {
            this.accessToken = accessToken;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return Instant.now().getEpochSecond() >= expiresAt;
        }
    }
}
