package com.challenge_digital.cila_bokk.repository;

import com.challenge_digital.cila_bokk.model.Projet;
import com.challenge_digital.cila_bokk.model.StatutProjet;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {

    List<Projet> findByIdAgentSoumission(Long idAgent);

    List<Projet> findByStatut(StatutProjet statut);

    List<Projet> findByIdAgentSoumissionAndStatut(Long idAgent, StatutProjet statut);

    long countByIdAgentSoumission(Long idAgent);

    long countByStatut(StatutProjet statut);

    // ✅ Nouvelles méthodes pour gérer les projets non soumis (brouillons)
    List<Projet> findByIdAgentSoumissionAndSoumis(Long idAgent, Boolean soumis);

    long countByIdAgentSoumissionAndSoumis(Long idAgent, Boolean soumis);

    List<Projet> findBySoumis(Boolean soumis);

    @Query("SELECT COUNT(p) FROM Projet p WHERE p.idAgentSoumission = :agentId OR :agentId MEMBER OF p.membresEquipe")
    long countByAgentId(@Param("agentId") Long agentId);

}