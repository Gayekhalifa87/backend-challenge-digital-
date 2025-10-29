package com.challenge_digital.cila_bokk.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité pour tracer les actions sur les projets
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "historique")
public class Historique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private LocalDateTime dateAction;

    @Column(nullable = false)
    private Long idAgent;

    @Column(nullable = true)
    private Long projetId;

    @PrePersist
    protected void onCreate() {
        dateAction = LocalDateTime.now();
    }
}