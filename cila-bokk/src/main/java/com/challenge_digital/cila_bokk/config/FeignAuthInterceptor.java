package com.challenge_digital.cila_bokk.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
@Slf4j
public class FeignAuthInterceptor {

    @Bean
    public RequestInterceptor oauth2FeignRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                if (auth instanceof JwtAuthenticationToken jwtAuth) {
                    String tokenValue = jwtAuth.getToken().getTokenValue();
                    template.header("Authorization", "Bearer " + tokenValue);
                    log.debug("Feign interceptor - Token ajouté au header Authorization");
                } else {
                    log.warn("Feign interceptor - Pas d'utilisateur authentifié ou pas de JWT");
                }
            }
        };
    }
}
