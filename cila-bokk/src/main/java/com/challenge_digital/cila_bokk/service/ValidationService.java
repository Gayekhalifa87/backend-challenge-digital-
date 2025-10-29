package com.challenge_digital.cila_bokk.service;

import com.challenge_digital.cila_bokk.dto.ValidationDTO;
import com.challenge_digital.cila_bokk.model.*;
import com.challenge_digital.cila_bokk.repository.ProjetRepository;
import com.challenge_digital.cila_bokk.repository.ValidationRepository;
import com.challenge_digital.cila_bokk.service.external.AgentApiDto;
import com.challenge_digital.cila_bokk.service.external.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final ValidationRepository validationRepository;
    private final ProjetRepository projetRepository;
    private final AgentService agentService;
    private final EmailService emailService;

    public List<ValidationDTO> getValidationsByProjet(Long projetId) {
        return validationRepository.findByProjetId(projetId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ValidationDTO validerProjet(Long projetId, NiveauValidation niveau, Long idAgentValidateur, String commentaire) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        // ✅ VÉRIFICATION STRICTE DU STATUT AVANT VALIDATION
        validateProjectStatusForLevel(projet, niveau);

        Validation validation = validationRepository.findByProjetIdAndNiveau(projetId, niveau)
                .orElseThrow(() -> new RuntimeException("Validation non trouvée pour ce niveau"));

        if (validation.getStatut() != StatutValidation.EN_ATTENTE) {
            throw new IllegalStateException("Cette validation a déjà été traitée.");
        }

        validation.setStatut(StatutValidation.APPROUVE);
        validation.setIdAgentValidateur(idAgentValidateur);
        validation.setCommentaire(commentaire);

        Validation savedValidation = validationRepository.save(validation);

        // Mise à jour du statut du projet
        switch (niveau) {
            case NIVEAU_1:  // Manager validé → passe à DTO/DPD
                projet.setStatut(StatutProjet.EN_ATTENTE_DTO_DPD);
                Validation validationN2 = new Validation();
                validationN2.setNiveau(NiveauValidation.NIVEAU_2);
                validationN2.setStatut(StatutValidation.EN_ATTENTE);
                projet.addValidation(validationN2);
                break;
            case NIVEAU_2:  // DTO/DPD validé → passe au Comité
                projet.setStatut(StatutProjet.EN_ATTENTE_COMITE);
                Validation validationN3 = new Validation();
                validationN3.setNiveau(NiveauValidation.NIVEAU_3);
                validationN3.setStatut(StatutValidation.EN_ATTENTE);
                projet.addValidation(validationN3);
                break;
            case NIVEAU_3:  // Comité validé → VALIDE
                projet.setStatut(StatutProjet.VALIDE);
                break;
        }

        projetRepository.save(projet);

        // Email de notification
        sendValidationEmail(projet, niveau, idAgentValidateur, commentaire, false);

        return convertToDTO(savedValidation);
    }

    @Transactional
    public ValidationDTO rejeterProjet(Long projetId, NiveauValidation niveau, Long idAgentValidateur, String commentaire) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        // ✅ VÉRIFICATION STRICTE DU STATUT AVANT REJET
        validateProjectStatusForLevel(projet, niveau);

        Validation validation = validationRepository.findByProjetIdAndNiveau(projetId, niveau)
                .orElseThrow(() -> new RuntimeException("Validation non trouvée pour ce niveau"));

        if (validation.getStatut() != StatutValidation.EN_ATTENTE) {
            throw new IllegalStateException("Cette validation a déjà été traitée.");
        }

        validation.setStatut(StatutValidation.REJETE);
        validation.setIdAgentValidateur(idAgentValidateur);
        validation.setCommentaire(commentaire);

        Validation savedValidation = validationRepository.save(validation);

        projet.setStatut(StatutProjet.REJETE);
        projetRepository.save(projet);

        // Email de notification
        sendValidationEmail(projet, niveau, idAgentValidateur, commentaire, true);

        return convertToDTO(savedValidation);
    }

    /**
     * ✅ VALIDATION STRICTE : Vérifie que le projet a le bon statut pour le niveau de validation
     */
    private void validateProjectStatusForLevel(Projet projet, NiveauValidation niveau) {
        switch (niveau) {
            case NIVEAU_1:
                if (projet.getStatut() != StatutProjet.EN_ATTENTE_MANAGER) {
                    throw new IllegalStateException(
                            "Ce projet ne peut pas être validé au niveau 1. " +
                                    "Statut actuel: " + projet.getStatut() + ", statut attendu: EN_ATTENTE_MANAGER"
                    );
                }
                break;
            case NIVEAU_2:
                if (projet.getStatut() != StatutProjet.EN_ATTENTE_DTO_DPD) {
                    throw new IllegalStateException(
                            "Ce projet ne peut pas être validé au niveau 2. " +
                                    "Statut actuel: " + projet.getStatut() + ", statut attendu: EN_ATTENTE_DTO_DPD"
                    );
                }
                break;
            case NIVEAU_3:
                if (projet.getStatut() != StatutProjet.EN_ATTENTE_COMITE) {
                    throw new IllegalStateException(
                            "Ce projet ne peut pas être validé au niveau 3. " +
                                    "Statut actuel: " + projet.getStatut() + ", statut attendu: EN_ATTENTE_COMITE"
                    );
                }
                break;
        }
    }

    /**
     * Envoie un email de notification après validation/rejet
     */
    private void sendValidationEmail(Projet projet, NiveauValidation niveau, Long idAgentValidateur,
                                     String commentaire, boolean estRejet) {
        String agentEmail = agentService.getAgentEmail(projet.getIdAgentSoumission());
        String agentNom = agentService.getAgentFullName(projet.getIdAgentSoumission());
        String validateurNom = agentService.getAgentFullName(idAgentValidateur);

        if (agentEmail != null) {
            String niveauLabel = getNiveauLabel(niveau);
            String subject = estRejet
                    ? "Rejet de votre projet : " + projet.getTitre()
                    : "Validation de votre projet : " + projet.getTitre();

            String body = estRejet
                    ? String.format(
                    "Bonjour %s,\n\n" +
                            "Votre projet '%s' a été rejeté par %s (%s).\n\n" +
                            "Motif : %s\n\n" +
                            "Vous pouvez modifier et resoumettre votre projet.\n\n" +
                            "Cordialement",
                    agentNom, projet.getTitre(), validateurNom, niveauLabel,
                    commentaire != null ? commentaire : "Non spécifié"
            )
                    : String.format(
                    "Bonjour %s,\n\n" +
                            "Votre projet '%s' a été validé par %s (%s).\n\n" +
                            "Commentaire : %s\n\n" +
                            "Statut actuel : %s\n\n" +
                            "Cordialement",
                    agentNom, projet.getTitre(), validateurNom, niveauLabel,
                    commentaire != null ? commentaire : "Aucun", projet.getStatut()
            );

            emailService.sendSimpleEmail(agentEmail, subject, body);
        }
    }

    public List<ValidationDTO> getHistoriqueValidations(Long projetId) {
        return validationRepository.findByProjetId(projetId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ValidationDTO convertToDTO(Validation validation) {
        ValidationDTO dto = new ValidationDTO();

        dto.setId(validation.getId());
        dto.setNiveau(validation.getNiveau());
        dto.setStatut(validation.getStatut());
        dto.setDateValidation(validation.getDateValidation());
        dto.setIdAgentValidateur(validation.getIdAgentValidateur());
        dto.setCommentaire(validation.getCommentaire());
        dto.setProjetId(validation.getProjet().getId());

        if (validation.getIdAgentValidateur() != null) {
            AgentApiDto validateur = agentService.getAgentById(validation.getIdAgentValidateur());
            if (validateur != null) {
                dto.setValidateurNom(validateur.getFullName());
                dto.setValidateurEmail(validateur.getEmail());
                dto.setValidateurMatricule(validateur.getMatricule() != null ? validateur.getMatricule().toString() : null);
                if (validateur.getFonction() != null) {
                    dto.setValidateurFonction(validateur.getFonction().getName());
                }
            }
        }

        return dto;
    }

    private String getNiveauLabel(NiveauValidation niveau) {
        switch (niveau) {
            case NIVEAU_1: return "Manager";
            case NIVEAU_2: return "DTO/DPD";
            case NIVEAU_3: return "Comité";
            default: return niveau.toString();
        }
    }
}