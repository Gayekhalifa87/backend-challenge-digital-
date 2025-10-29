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

        Validation validation = validationRepository.findByProjetIdAndNiveau(projetId, niveau)
                .orElseThrow(() -> new RuntimeException("Validation non trouvée pour ce niveau"));

        if (validation.getStatut() != StatutValidation.EN_ATTENTE) {
            throw new IllegalStateException("Cette validation a déjà été traitée.");
        }

        validation.setStatut(StatutValidation.APPROUVE);
        validation.setIdAgentValidateur(idAgentValidateur);
        validation.setCommentaire(commentaire);

        Validation savedValidation = validationRepository.save(validation);

        // ✅ Adapter les statuts selon votre nouveau workflow
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
        String agentEmail = agentService.getAgentEmail(projet.getIdAgentSoumission());
        String agentNom = agentService.getAgentFullName(projet.getIdAgentSoumission());
        String validateurNom = agentService.getAgentFullName(idAgentValidateur);

        if (agentEmail != null) {
            String niveauLabel = getNiveauLabel(niveau);
            String subject = "Validation de votre projet : " + projet.getTitre();
            String body = String.format(
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

        return convertToDTO(savedValidation);
    }

    @Transactional
    public ValidationDTO rejeterProjet(Long projetId, NiveauValidation niveau, Long idAgentValidateur, String commentaire) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

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
        String agentEmail = agentService.getAgentEmail(projet.getIdAgentSoumission());
        String agentNom = agentService.getAgentFullName(projet.getIdAgentSoumission());
        String validateurNom = agentService.getAgentFullName(idAgentValidateur);

        if (agentEmail != null) {
            String niveauLabel = getNiveauLabel(niveau);
            String subject = "Rejet de votre projet : " + projet.getTitre();
            String body = String.format(
                    "Bonjour %s,\n\n" +
                            "Votre projet '%s' a été rejeté par %s (%s).\n\n" +
                            "Motif : %s\n\n" +
                            "Vous pouvez modifier et resoumettre votre projet.\n\n" +
                            "Cordialement",
                    agentNom, projet.getTitre(), validateurNom, niveauLabel,
                    commentaire != null ? commentaire : "Non spécifié"
            );
            emailService.sendSimpleEmail(agentEmail, subject, body);
        }

        return convertToDTO(savedValidation);
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

    /**
     * ✅ Helper pour obtenir le label du niveau de validation
     */
    private String getNiveauLabel(NiveauValidation niveau) {
        switch (niveau) {
            case NIVEAU_1: return "Manager";
            case NIVEAU_2: return "DTO/DPD";
            case NIVEAU_3: return "Comité";
            default: return niveau.toString();
        }
    }
}