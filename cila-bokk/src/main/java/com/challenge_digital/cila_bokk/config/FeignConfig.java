package com.challenge_digital.cila_bokk.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Configuration Feign pour activer les clients d'API externes
 * Scanne le package 'service.external' pour trouver tous les @FeignClient
 */
@CrossOrigin
@Configuration
@EnableFeignClients(basePackages = "com.challenge_digital.cila_bokk.service.external")
public class FeignConfig {
    // Cette classe active automatiquement OpenFeign
    // Tous les @FeignClient dans le package 'service.external' seront détectés
}