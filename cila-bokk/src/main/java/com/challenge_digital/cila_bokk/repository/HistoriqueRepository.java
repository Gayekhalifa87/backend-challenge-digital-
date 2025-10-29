package com.challenge_digital.cila_bokk.repository;

import com.challenge_digital.cila_bokk.model.Historique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueRepository extends JpaRepository<Historique, Long> {

    List<Historique> findByIdAgent(Long idAgent);

    List<Historique> findByProjetId(Long projetId);

    List<Historique> findAllByOrderByDateActionDesc();
}