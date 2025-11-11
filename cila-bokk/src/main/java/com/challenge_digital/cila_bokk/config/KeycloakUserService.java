//
package com.challenge_digital.cila_bokk.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserService {

    /**
     * 🔹 Récupère le JWT courant à partir du SecurityContext
     */
    public Jwt getCurrentJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        if (auth.getPrincipal() instanceof Jwt jwt) return jwt;
        return null;
    }

    /**
     * 🔹 Retourne email ou username depuis le JWT
     */
    public String getEmailOrUsername() {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) return null;

        Map<String, Object> claims = jwt.getClaims();
        if (claims.containsKey("email")) {
            return claims.get("email").toString();
        } else if (claims.containsKey("preferred_username")) {
            return claims.get("preferred_username").toString();
        }
        return null;
    }

    /**
     * 🔹 Retourne toutes les informations du JWT (utiles pour /api/user)
     */
    public Map<String, Object> getUserInfo() {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) {
            return Map.of("message", "Aucun utilisateur connecté");
        }
        return jwt.getClaims();
    }


    public String getEmail() {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) return null;
        Object email = jwt.getClaims().get("email");
        return email != null ? email.toString() : null;
    }

}
