package org.adcb.adapter.spi.auth;

import org.adcb.adapter.commons.ServiceMetadata;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component("API_KEY")
public class ApiKeyAuthStrategy implements AuthenticationStrategy {
    @Override
    public void apply(ServiceMetadata config, HttpHeaders headers) {
        String token = config.getAuth().getTokenSource();
        if (config.getAuth().getStrategy().equals("HEADER")) {
            headers.add(config.getAuth().getKeyName(), token);
        } else {
            // query param handling in handler
        }
    }
}
