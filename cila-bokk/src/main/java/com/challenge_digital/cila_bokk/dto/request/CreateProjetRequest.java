//package com.challenge_digital.cila_bokk.dto.request;
//
//import jakarta.validation.constraints.NotBlank;
//import lombok.*;
//
//import java.util.Set;
//
///**
// * DTO pour la création d'un projet
// */
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class CreateProjetRequest {
//
//    @NotBlank(message = "Le titre est obligatoire")
//    private String titre;
//
//    private String description;
//    private String objectif;
//    private String acteurs;
//    private String gains;
//    private String indicateurs;
//    private String ressources;
//
//    /**
//     * Code de l'entité (obligatoire)
//     * Exemple: "5100" pour Diourbel, "7125" pour Petite Côte
//     */
//    @NotBlank(message = "Le code de l'entité est obligatoire")
//    private String codeEntite;
//
//    /**
//     * Matricules des membres de l'équipe (optionnel)
//     */
//    private Set<String> matriculesMembres;
//}