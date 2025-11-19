package com.challenge_digital.cila_bokk.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Représente un agent depuis l'API externe
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentDTO {

    private Long id;
    private String matricule;

    @JsonProperty("fullName")
    private String fullName;

    private String email;
    private Double taux;
    private String sexe;

    @JsonProperty("dateNaissance")
    private Date dateNaissance;

    @JsonProperty("situationMatrimoniale")
    private String situationMatrimoniale;

    private String telephone;
    private String cin;
    private Boolean active;
    private Boolean conge;

    // Relations
    private ChefDTO chef;
    private FonctionDTO fonction;
    private DirectionDTO direction;
    private EquipeDTO etablissement;
    private EquipeDTO equipe;
    private List<ContratDTO> contrats;

    @JsonProperty("personneContacts")
    private List<Object> personneContacts;

    @JsonProperty("zoneInterventionList")
    private List<Object> zoneInterventionList;

    /**
     * DTOs internes
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChefDTO {
        private Long id;
        private String matricule;
        @JsonProperty("fullName")
        private String fullName;
        private String email;
        private Boolean active;
        private FonctionDTO fonction;
        private DirectionDTO direction;
        private EquipeDTO equipe;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FonctionDTO {
        private Long id;
        private Boolean active;
        private String name;
        private String code;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DirectionDTO {
        private Long id;
        private Boolean active;
        private String name;
        private String code;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquipeDTO {
        private Long id;
        private Boolean active;
        private String code;
        private String name;
        private Object secteur;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContratDTO {
        private Long id;
        private Boolean active;
        @JsonProperty("dateFin")
        private Date dateFin;
        @JsonProperty("dateDebut")
        private Date dateDebut;
        @JsonProperty("typeContrat")
        private TypeContratDTO typeContrat;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeContratDTO {
        private Long id;
        private Boolean active;
        private String name;
        private String code;
    }

    // Méthodes utilitaires
    public String getNomComplet() {
        return fullName != null ? fullName : "Agent Inconnu";
    }

    public String getCodeEquipe() {
        return equipe != null ? equipe.getCode() : null;
    }

    public String getNomEquipe() {
        return equipe != null ? equipe.getName() : null;
    }
}