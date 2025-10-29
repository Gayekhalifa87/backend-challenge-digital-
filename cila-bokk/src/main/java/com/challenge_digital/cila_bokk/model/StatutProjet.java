package com.challenge_digital.cila_bokk.model;

/**
 * Statut d'un projet dans le workflow de validation
 */
public enum StatutProjet {
    EN_ATTENTE_MANAGER,       // Soumis, en attente validation niveau 1
    EN_ATTENTE_DTO_DPD,       // Validé N1, en attente validation niveau 2
    EN_ATTENTE_COMITE,       // Validé N2, en attente validation niveau 3
    VALIDE,              // Validé à tous les niveaux
    REJETE               // Rejeté à un niveau quelconque
}