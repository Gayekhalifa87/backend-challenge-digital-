package com.challenge_digital.cila_bokk.service;

import org.springframework.stereotype.Service;

/**
 * ⚠️ VERSION MOCK - Sans envoi d'email réel
 * Utilisez cette version si vous n'avez pas configuré SMTP
 */
@Service
public class EmailService {

    public void sendSimpleEmail(String to, String subject, String body) {
        // ✅ Log dans la console au lieu d'envoyer un vrai email
        System.out.println("📧 EMAIL SIMULÉ");
        System.out.println("   To: " + to);
        System.out.println("   Subject: " + subject);
        System.out.println("   Body: " + body);
        System.out.println("   ✅ Email simulé envoyé avec succès\n");
    }
}