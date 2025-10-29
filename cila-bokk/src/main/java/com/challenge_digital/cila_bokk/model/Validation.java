package com.challenge_digital.cila_bokk.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant une validation d'un projet à un niveau donné
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "validations")
public class Validation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NiveauValidation niveau;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutValidation statut = StatutValidation.EN_ATTENTE;

    @Column(nullable = true)
    private LocalDateTime dateValidation;

    @Column(nullable = true)
    private Long idAgentValidateur;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    @JsonBackReference
    private Projet projet;

    @PreUpdate
    protected void onUpdate() {
        if (statut != StatutValidation.EN_ATTENTE && dateValidation == null) {
            dateValidation = LocalDateTime.now();
        }
    }
}