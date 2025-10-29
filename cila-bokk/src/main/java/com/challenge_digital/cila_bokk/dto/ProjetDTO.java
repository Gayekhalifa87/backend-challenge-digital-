package com.challenge_digital.cila_bokk.dto;

import com.challenge_digital.cila_bokk.model.StatutProjet;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO combinant projet + infos agent externe
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjetDTO {

    // Infos du projet
    private Long id;
    private String titre;
    private String description;
    private String objectif;
    private String acteurs;
    private String ressources;
    private String indicateurs;
    private String gains;
    private StatutProjet statut;
    private LocalDateTime dateSoumission;
    private LocalDateTime dateModification;
    private Long idAgentSoumission;
    private Boolean soumis;

    // Infos de l'équipe
    private List<MembreEquipeDTO> membresEquipe; // pour projets en équipe
    private List<String> nomsMembresEquipe; // optionnel : noms récupérés depuis l’API externe

    // Infos de l'agent principal (API externe)
    private String agentNom;
    private String agentEmail;
    private String agentMatricule;
    private String agentDirection;
    private String agentService;
    private String agentFonction;

    // Validations
    private List<ValidationDTO> validations;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembreEquipeDTO {
        private String nom;
        private String prenom;
        private String matricule;
    }



}
