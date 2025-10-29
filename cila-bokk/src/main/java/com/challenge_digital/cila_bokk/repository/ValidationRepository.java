package com.challenge_digital.cila_bokk.repository;

import com.challenge_digital.cila_bokk.model.NiveauValidation;
import com.challenge_digital.cila_bokk.model.StatutValidation;
import com.challenge_digital.cila_bokk.model.Validation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValidationRepository extends JpaRepository<Validation, Long> {

    List<Validation> findByProjetId(Long projetId);

    Optional<Validation> findByProjetIdAndNiveau(Long projetId, NiveauValidation niveau);

    List<Validation> findByIdAgentValidateurAndStatut(Long idAgent, StatutValidation statut);

    List<Validation> findByNiveauAndStatut(NiveauValidation niveau, StatutValidation statut);

    long countByIdAgentValidateurAndStatut(Long idAgent, StatutValidation statut);
}