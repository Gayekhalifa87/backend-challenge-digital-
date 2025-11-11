package com.challenge_digital.cila_bokk.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un projet soumis par un agent
 * Un projet passe par plusieurs niveaux de validation
 * Version simplifiée : membres stockés directement dans la table projets
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "projets")
public class Projet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String objectif;

    @Column(columnDefinition = "TEXT")
    private String ressources;

    @Column(columnDefinition = "TEXT")
    private String gains;

    @Column(columnDefinition = "TEXT")
    private String indicateurs;

    @Column(columnDefinition = "TEXT")
    private String acteurs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private StatutProjet statut = StatutProjet.EN_ATTENTE_MANAGER;

    @Column(nullable = false)
    private LocalDateTime dateSoumission;

    @Column(nullable = false)
    private LocalDateTime dateModification;

    @Column(nullable = false)
    private Long idAgentSoumission;

    @Column(name = "membre1_id")
    private Long membre1Id;

    @Column(name = "membre1_matricule", length = 50)
    private String membre1Matricule;

    @Column(name = "membre2_id")
    private Long membre2Id;

    @Column(name = "membre2_matricule", length = 50)
    private String membre2Matricule;

    @Column(nullable = false)
    private Boolean soumis = true;

    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Validation> validations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dateSoumission = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    public void addValidation(Validation validation) {
        validations.add(validation);
        validation.setProjet(this);
    }

    public void removeValidation(Validation validation) {
        validations.remove(validation);
        validation.setProjet(null);
    }
}
