//package com.challenge_digital.cila_bokk.service;
//
//import com.challenge_digital.cila_bokk.dto.ValidationDTO;
//import com.challenge_digital.cila_bokk.model.*;
//import com.challenge_digital.cila_bokk.repository.ProjetRepository;
//import com.challenge_digital.cila_bokk.repository.ValidationRepository;
//import com.challenge_digital.cila_bokk.service.external.AgentService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Map;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ValidationService {
//
//    private final ProjetRepository projetRepository;
//    private final ValidationRepository validationRepository;
//    private final AgentService agentService;
//
//    /**
//     * 🔹 Valider un projet (EN_ATTENTE_MANAGER → EN_ATTENTE_COMITE)
//     * Seul le directeur de la direction du projet peut valider
//     */
//    @Transactional
//    public ValidationDTO validerProjet(Long projetId, String commentaire) {
//        // 1. Récupérer le projet
//        Projet projet = projetRepository.findById(projetId)
//                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'ID: " + projetId));
//
//        // 2. Vérifier le statut actuel
//        if (projet.getStatut() != StatutProjet.EN_ATTENTE_MANAGER) {
//            throw new IllegalStateException(
//                    "Le projet doit être au statut EN_ATTENTE_MANAGER pour être validé. Statut actuel: "
//                            + projet.getStatut()
//            );
//        }
//
//        // 3. Récupérer l'agent connecté
//        Map<String, Object> agentConnecte = agentService.getConnectedAgentDetails();
//        if (agentConnecte == null || agentConnecte.containsKey("message")) {
//            throw new RuntimeException("Utilisateur non connecté ou introuvable");
//        }
//
//        Long validateurId = convertToLong(agentConnecte.get("id"));
//
//        // 4. Vérifier que le validateur est directeur
//        Map<String, Object> fonction = (Map<String, Object>) agentConnecte.get("fonction");
//        if (fonction == null) {
//            throw new IllegalStateException("L'agent connecté n'a pas de fonction définie");
//        }
//
//        String fonctionName = (String) fonction.get("name");
//        if (fonctionName == null || !fonctionName.toLowerCase().contains("directeur")) {
//            throw new IllegalStateException(
//                    "Seul un directeur peut valider un projet. Fonction actuelle: " + fonctionName
//            );
//        }
//
//        // 5. Vérifier que le projet appartient à la direction du validateur
//        if (!projetAppartientDirection(projet, agentConnecte)) {
//            throw new IllegalStateException(
//                    "Vous ne pouvez valider que les projets de votre direction"
//            );
//        }
//
//        // 6. Récupérer ou créer la validation niveau 1
//        Optional<Validation> validationOpt = validationRepository.findByProjetIdAndNiveau(
//                projetId,
//                NiveauValidation.NIVEAU_1
//        );
//
//        Validation validation;
//        if (validationOpt.isPresent()) {
//            validation = validationOpt.get();
//        } else {
//            validation = new Validation();
//            validation.setProjet(projet);
//            validation.setNiveau(NiveauValidation.NIVEAU_1);
//        }
//
//        // 7. Mettre à jour la validation
//        validation.setStatut(StatutValidation.APPROUVE);
//        validation.setIdAgentValidateur(validateurId);
//        validation.setCommentaire(commentaire);
//
//        // 8. Mettre à jour le statut du projet
//        projet.setStatut(StatutProjet.EN_ATTENTE_COMITE);
//
//        // 9. Créer la validation niveau 2 (pour le comité)
//        Validation validation2 = new Validation();
//        validation2.setProjet(projet);
//        validation2.setNiveau(NiveauValidation.NIVEAU_2);
//        validation2.setStatut(StatutValidation.EN_ATTENTE);
//
//        // 10. Sauvegarder
//        validationRepository.save(validation);
//        validationRepository.save(validation2);
//        projetRepository.save(projet);
//
//        log.info("✅ Projet {} validé par le directeur {} et passé à EN_ATTENTE_COMITE",
//                projetId, validateurId);
//
//        return convertToDTO(validation);
//    }
//
//    /**
//     * 🔹 Rejeter un projet (EN_ATTENTE_MANAGER → REJETE)
//     * Seul le directeur de la direction du projet peut rejeter
//     */
//    @Transactional
//    public ValidationDTO rejeterProjet(Long projetId, String commentaire) {
//        // 1. Récupérer le projet
//        Projet projet = projetRepository.findById(projetId)
//                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'ID: " + projetId));
//
//        // 2. Vérifier le statut actuel
//        if (projet.getStatut() != StatutProjet.EN_ATTENTE_MANAGER) {
//            throw new IllegalStateException(
//                    "Le projet doit être au statut EN_ATTENTE_MANAGER pour être rejeté. Statut actuel: "
//                            + projet.getStatut()
//            );
//        }
//
//        // 3. Récupérer l'agent connecté
//        Map<String, Object> agentConnecte = agentService.getConnectedAgentDetails();
//        if (agentConnecte == null || agentConnecte.containsKey("message")) {
//            throw new RuntimeException("Utilisateur non connecté ou introuvable");
//        }
//
//        Long validateurId = convertToLong(agentConnecte.get("id"));
//
//        // 4. Vérifier que le validateur est directeur
//        Map<String, Object> fonction = (Map<String, Object>) agentConnecte.get("fonction");
//        if (fonction == null) {
//            throw new IllegalStateException("L'agent connecté n'a pas de fonction définie");
//        }
//
//        String fonctionName = (String) fonction.get("name");
//        if (fonctionName == null || !fonctionName.toLowerCase().contains("directeur")) {
//            throw new IllegalStateException(
//                    "Seul un directeur peut rejeter un projet. Fonction actuelle: " + fonctionName
//            );
//        }
//
//        // 5. Vérifier que le projet appartient à la direction du validateur
//        if (!projetAppartientDirection(projet, agentConnecte)) {
//            throw new IllegalStateException(
//                    "Vous ne pouvez rejeter que les projets de votre direction"
//            );
//        }
//
//        // 6. Récupérer ou créer la validation niveau 1
//        Optional<Validation> validationOpt = validationRepository.findByProjetIdAndNiveau(
//                projetId,
//                NiveauValidation.NIVEAU_1
//        );
//
//        Validation validation;
//        if (validationOpt.isPresent()) {
//            validation = validationOpt.get();
//        } else {
//            validation = new Validation();
//            validation.setProjet(projet);
//            validation.setNiveau(NiveauValidation.NIVEAU_1);
//        }
//
//        // 7. Mettre à jour la validation
//        validation.setStatut(StatutValidation.REJETE);
//        validation.setIdAgentValidateur(validateurId);
//        validation.setCommentaire(commentaire);
//
//        // 8. Mettre à jour le statut du projet
//        projet.setStatut(StatutProjet.REJETE);
//
//        // 9. Sauvegarder
//        validationRepository.save(validation);
//        projetRepository.save(projet);
//
//        log.info("❌ Projet {} rejeté par le directeur {}", projetId, validateurId);
//
//        return convertToDTO(validation);
//    }
//
//    /**
//     * 🔹 Vérifie si un projet appartient à la direction du validateur
//     */
//    private boolean projetAppartientDirection(Projet projet, Map<String, Object> agentConnecte) {
//        try {
//            // Récupérer la direction du validateur
//            Object rattachementValidateurObj = agentConnecte.get("rattachement");
//            if (rattachementValidateurObj == null) {
//                log.warn("Le validateur n'a pas de rattachement défini");
//                return false;
//            }
//
//            Map<String, Object> rattachementValidateur = (Map<String, Object>) rattachementValidateurObj;
//            Long directionValidateurId = convertToLong(rattachementValidateur.get("id"));
//
//            // Récupérer l'agent soumissionnaire du projet
//            Map<String, Object> agentSoumissionnaire = agentService.getAgentById(projet.getIdAgentSoumission());
//            if (agentSoumissionnaire == null || agentSoumissionnaire.containsKey("message")) {
//                log.warn("Agent soumissionnaire non trouvé pour le projet {}", projet.getId());
//                return false;
//            }
//
//            // Récupérer la direction de l'agent soumissionnaire
//            Object rattachementProjetObj = agentSoumissionnaire.get("rattachement");
//            if (rattachementProjetObj == null) {
//                log.warn("L'agent soumissionnaire n'a pas de rattachement défini");
//                return false;
//            }
//
//            Map<String, Object> rattachementProjet = (Map<String, Object>) rattachementProjetObj;
//            Long directionProjetId = convertToLong(rattachementProjet.get("id"));
//
//            // Comparer les IDs
//            boolean appartient = directionValidateurId.equals(directionProjetId);
//
//            if (!appartient) {
//                log.info("❌ Le projet {} (direction: {}) n'appartient pas à la direction du validateur ({})",
//                        projet.getId(), directionProjetId, directionValidateurId);
//            } else {
//                log.info("✅ Le projet {} appartient à la direction du validateur", projet.getId());
//            }
//
//            return appartient;
//        } catch (Exception e) {
//            log.error("Erreur lors de la vérification d'appartenance à la direction", e);
//            return false;
//        }
//    }
//
//    /**
//     * 🔹 Conversion Validation → ValidationDTO
//     */
//    private ValidationDTO convertToDTO(Validation validation) {
//        ValidationDTO dto = new ValidationDTO();
//        dto.setId(validation.getId());
//        dto.setNiveau(validation.getNiveau());
//        dto.setStatut(validation.getStatut());
//        dto.setDateValidation(validation.getDateValidation());
//        dto.setIdAgentValidateur(validation.getIdAgentValidateur());
//        dto.setCommentaire(validation.getCommentaire());
//        dto.setProjetId(validation.getProjet().getId());
//
//        // Enrichir avec les informations du validateur si disponible
//        if (validation.getIdAgentValidateur() != null) {
//            Map<String, Object> validateur = agentService.getAgentById(validation.getIdAgentValidateur());
//            if (validateur != null && !validateur.containsKey("message")) {
//                dto.setValidateurNom((String) validateur.get("fullName"));
//                dto.setValidateurEmail((String) validateur.get("email"));
//                dto.setValidateurMatricule(String.valueOf(validateur.get("matricule")));
//
//                Map<String, Object> fonction = (Map<String, Object>) validateur.get("fonction");
//                if (fonction != null) {
//                    dto.setValidateurFonction((String) fonction.get("name"));
//                }
//            }
//        }
//
//        return dto;
//    }
//
//    /**
//     * 🔹 Utilitaire pour convertir Integer/Long/String en Long
//     */
//    private Long convertToLong(Object value) {
//        if (value == null) return null;
//        if (value instanceof Long) return (Long) value;
//        if (value instanceof Integer) return ((Integer) value).longValue();
//        return Long.valueOf(value.toString());
//    }
//}