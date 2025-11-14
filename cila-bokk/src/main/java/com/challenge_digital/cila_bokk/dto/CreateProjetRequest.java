package com.challenge_digital.cila_bokk.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * DTO pour la création d'un nouveau projet
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjetRequest {
    private String titre;
    private String teamName;
    private String description;
    private String problematique;
    private String objectif;
    private String gains;
    private String ressources;
    private String acteurs;
    private String indicateurs;
    private Long idAgentSoumission;

    // ✅ Nouvelle structure : liste des membres avec nom, prénom et matricule
    private List<MembreDTO> membresEquipe;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembreDTO {
        private String nom;
        private String prenom;
        private String matricule;
    }
}
