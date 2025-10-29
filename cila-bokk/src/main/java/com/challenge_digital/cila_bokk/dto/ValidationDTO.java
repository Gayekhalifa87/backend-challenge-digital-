package com.challenge_digital.cila_bokk.dto;

import com.challenge_digital.cila_bokk.model.NiveauValidation;
import com.challenge_digital.cila_bokk.model.StatutValidation;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO combinant validation + infos validateur externe
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationDTO {

    // Infos validation
    private Long id;
    private NiveauValidation niveau;
    private StatutValidation statut;
    private LocalDateTime dateValidation;
    private Long idAgentValidateur;
    private String commentaire;
    private Long projetId;

    // Infos validateur (API externe)
    private String validateurNom;
    private String validateurEmail;
    private String validateurMatricule;
    private String validateurFonction;
}