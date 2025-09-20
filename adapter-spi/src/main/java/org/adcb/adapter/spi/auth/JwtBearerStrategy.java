package org.adcb.adapter.spi.auth;

import org.adcb.adapter.commons.ServiceMetadata;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component("JWT_BEARER")
public class JwtBearerStrategy implements AuthenticationStrategy {
    @Override
    public void apply(ServiceMetadata config, HttpHeaders headers) {
        headers.add("Authorization","Bearer "+config.getAuth().getTokenSource());
    }
}
