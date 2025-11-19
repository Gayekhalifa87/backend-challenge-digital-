package com.challenge_digital.cila_bokk.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * DTO pour la création d'un nouveau projet
 * Supporte la soumission solo ou en équipe (max 3 personnes incluant le porteur)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjetRequest {
    private String titre;
    private String teamName;
    private String description;
    private String objectif;
    private String gains;
    private String ressources;
    private String acteurs;
    private String indicateurs;
    private Long idAgentSoumission;

    /**
     * Type de soumission : "SOLO" ou "EQUIPE"
     */
    private TypeSoumission typeSoumission = TypeSoumission.SOLO;

    /**
     * Liste des membres de l'équipe (uniquement si typeSoumission = EQUIPE)
     * Maximum 2 membres additionnels (3 personnes au total avec le porteur)
     * Chaque membre doit avoir un matricule valide de l'API Agent
     */
    private List<MembreDTO> membresEquipe;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembreDTO {
        private String nom;          // Optionnel - sera enrichi depuis l'API
        private String prenom;       // Optionnel - sera enrichi depuis l'API
        private String matricule;    // OBLIGATOIRE - utilisé pour valider et enrichir
    }

    /**
     * Type de soumission du projet
     */
    public enum TypeSoumission {
        SOLO,    // Projet soumis par une seule personne
        EQUIPE   // Projet soumis par une équipe (2-3 personnes)
    }

    /**
     * Valide les règles métier de base
     */
    public boolean isValid() {
        if (titre == null || titre.trim().isEmpty()) return false;
        if (description == null || description.trim().isEmpty()) return false;
        if (idAgentSoumission == null) return false;

        // Si équipe, vérifier qu'il y a des membres
        if (typeSoumission == TypeSoumission.EQUIPE) {
            if (membresEquipe == null || membresEquipe.isEmpty()) return false;
            if (membresEquipe.size() > 2) return false; // Max 2 + porteur = 3

            // Vérifier que tous les membres ont un matricule
            return membresEquipe.stream()
                    .allMatch(m -> m.getMatricule() != null && !m.getMatricule().trim().isEmpty());
        }

        return true;
    }
}