package com.challenge_digital.cila_bokk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // toutes les routes
                .allowedOriginPatterns("http://localhost:*") // Angular local + prod
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // méthodes autorisées
                .allowedHeaders("*") // tous les headers
                .allowCredentials(true) // autorise cookies et Authorization header
                .exposedHeaders("Authorization"); // pour récupérer le token si besoin
    }
}
