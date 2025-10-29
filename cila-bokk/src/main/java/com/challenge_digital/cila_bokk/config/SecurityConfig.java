package com.challenge_digital.cila_bokk.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Configuration de sécurité avec Keycloak OAuth2
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // ✅ Endpoints publics
                        .requestMatchers("/", "/accueil", "/login", "/static/**", "/assets/**").permitAll()

                        // ✅ Endpoints d'authentification (publics)
                        .requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/auth/logout").permitAll()

                        // ✅ API publique : consultation des projets
                        .requestMatchers(HttpMethod.GET, "/api/projets").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/projets/count").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/projets/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/projets/agent/{agentId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/projets/statut/{statut}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/projets/agent/{agentId}/brouillons").permitAll()

                        // ✅ API publique : consultation des validations
                        .requestMatchers(HttpMethod.GET, "/api/validations/**").permitAll()

                        // ✅ Endpoints protégés (nécessitent authentification)
                        .requestMatchers(HttpMethod.POST, "/api/projets").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/projets/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/projets/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/validations/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/validations/**").authenticated()

                        .requestMatchers("/api/auth/me").authenticated()

                        // Le reste nécessite une authentification
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint((request, response, authException) -> {
                            System.out.println("🚨 Erreur auth sur: " + request.getRequestURI());

                            String uri = request.getRequestURI();
                            // Liste des endpoints publics
                            if (uri.equals("/") || uri.equals("/accueil") ||
                                    uri.startsWith("/static") || uri.startsWith("/assets") ||
                                    uri.startsWith("/api/auth/") ||
                                    (uri.startsWith("/api/projets") && request.getMethod().equals("GET")) ||
                                    (uri.startsWith("/api/validations") && request.getMethod().equals("GET"))) {
                                response.setStatus(HttpServletResponse.SC_OK);
                                return;
                            }

                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
                        })
                );

        return http.build();
    }

    /**
     * Décodeur JWT pour Keycloak
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        String jwkSetUri = "https://refonte.seneau.sn/realms/auth2-dev/protocol/openid-connect/certs";
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    /**
     * Convertisseur pour extraire les rôles du JWT
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        // Vous pouvez personnaliser ici l'extraction des rôles si nécessaire
        return converter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuration CORS
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "https://refonte.seneau.sn"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}