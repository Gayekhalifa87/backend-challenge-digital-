package com.challenge_digital.cila_bokk.service;

import com.challenge_digital.cila_bokk.model.Historique;
import com.challenge_digital.cila_bokk.repository.HistoriqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoriqueService {

    private final HistoriqueRepository historiqueRepository;

    public void logAction(String action, Long idAgent, Long projetId) {
        Historique historique = new Historique();
        historique.setAction(action);
        historique.setIdAgent(idAgent);
        historique.setProjetId(projetId);
        historiqueRepository.save(historique);
        System.out.println("📝 Action loggée: " + action);
    }

    public List<Historique> getHistoriqueByAgent(Long idAgent) {
        return historiqueRepository.findByIdAgent(idAgent);
    }

    public List<Historique> getHistoriqueByProjet(Long projetId) {
        return historiqueRepository.findByProjetId(projetId);
    }

    public List<Historique> getAllHistorique() {
        return historiqueRepository.findAllByOrderByDateActionDesc();
    }
}