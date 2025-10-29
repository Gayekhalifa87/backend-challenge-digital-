package com.challenge_digital.cila_bokk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration pour Keycloak
 */
@Configuration
public class KeycloakConfig {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    /**
     * URL de base de Keycloak
     */
    public String getKeycloakServerUrl() {
        return authServerUrl + "/realms/" + realm;
    }

    /**
     * URL pour obtenir un token
     */
    public String getTokenUrl() {
        return getKeycloakServerUrl() + "/protocol/openid-connect/token";
    }

    /**
     * URL pour logout
     */
    public String getLogoutUrl() {
        return getKeycloakServerUrl() + "/protocol/openid-connect/logout";
    }

    /**
     * URL pour obtenir les informations utilisateur
     */
    public String getUserInfoUrl() {
        return getKeycloakServerUrl() + "/protocol/openid-connect/userinfo";
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}