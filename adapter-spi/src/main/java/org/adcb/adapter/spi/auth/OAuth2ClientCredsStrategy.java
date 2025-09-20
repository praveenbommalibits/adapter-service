package org.adcb.adapter.spi.auth;

import lombok.Data;
import org.adcb.adapter.commons.ServiceMetadata;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component("OAUTH2")
public class OAuth2ClientCredsStrategy implements AuthenticationStrategy {
    private volatile String accessToken;
    @Override
    public void apply(ServiceMetadata config, HttpHeaders headers) {
        if (accessToken == null || tokenExpired()) refreshToken(config);
        headers.add("Authorization","Bearer "+accessToken);
    }
    @Override
    public void refreshToken(ServiceMetadata config) {
        WebClient client = WebClient.create();
        TokenResponse tr = client.post()
                .uri(config.getAuth().getTokenEndpoint())
                .bodyValue(Map.of("grant_type","client_credentials",
                        "client_id",config.getAuth().getClientId(),
                        "client_secret",config.getAuth().getClientSecret()))
                .retrieve().bodyToMono(TokenResponse.class).block();
        this.accessToken = tr.getAccessToken();
    }
    private boolean tokenExpired() { return false; } // implement expiry
    @Data
    static class TokenResponse { private String accessToken; private long expiresIn; }
}
