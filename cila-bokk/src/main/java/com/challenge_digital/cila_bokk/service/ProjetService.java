package com.challenge_digital.cila_bokk.service;

import com.challenge_digital.cila_bokk.dto.CreateProjetRequest;
import com.challenge_digital.cila_bokk.dto.ProjetDTO;
import com.challenge_digital.cila_bokk.dto.ValidationDTO;
import com.challenge_digital.cila_bokk.model.*;
import com.challenge_digital.cila_bokk.repository.ProjetRepository;
import com.challenge_digital.cila_bokk.service.external.AgentApiDto;
import com.challenge_digital.cila_bokk.service.external.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjetService {

    private final ProjetRepository projetRepository;
    // ✅ On commente ce champ si ValidationService n’est pas encore implémenté
    // private final ValidationService validationService;
    private final AgentService agentService;

    // ✅ Récupère tous les projets
    public List<ProjetDTO> getAllProjets() {
        return projetRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<ProjetDTO> getProjetById(Long id) {
        return projetRepository.findById(id).map(this::convertToDTO);
    }

    public List<ProjetDTO> getProjetsByAgent(Long idAgent) {
        return projetRepository.findByIdAgentSoumission(idAgent)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProjetDTO> getProjetsByStatut(StatutProjet statut) {
        return projetRepository.findByStatut(statut)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProjetDTO> getProjetsBrouillon(Long idAgent) {
        return projetRepository.findByIdAgentSoumissionAndSoumis(idAgent, false)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

@Transactional
public ProjetDTO createProjet(CreateProjetRequest request) {
    long nbProjets = projetRepository.countByIdAgentSoumissionAndSoumis(request.getIdAgentSoumission(), true);
    if (nbProjets >= 2) {
        throw new IllegalArgumentException("L'agent a déjà soumis 2 projets maximum.");
    }

    if (request.getMembresEquipe() != null && request.getMembresEquipe().size() > 2) {
        throw new IllegalArgumentException("Une équipe ne peut pas dépasser 2 membres.");
    }

    Projet projet = new Projet();
    projet.setTeamName(request.getTeamName());
    projet.setTitre(request.getTitre());
    projet.setProblematique(request.getProblematique());
    projet.setDescription(request.getDescription());
    projet.setObjectif(request.getObjectif());
    projet.setRessources(request.getRessources());
    projet.setGains(request.getGains());
    projet.setActeurs(request.getActeurs());
    projet.setIndicateurs(request.getIndicateurs());
    projet.setIdAgentSoumission(request.getIdAgentSoumission());
    projet.setSoumis(true);

    // ✅ Récupérer les IDs réels via AgentService
    if (request.getMembresEquipe() != null && !request.getMembresEquipe().isEmpty()) {
        List<String> matricules = request.getMembresEquipe().stream()
                .map(m -> m.getMatricule())
                .collect(Collectors.toList());

        // Appel au service pour trouver les agents correspondants
        List<Map<String, Object>> agentsTrouves = agentService.getAgentsByMatricules(matricules);

        if (!agentsTrouves.isEmpty()) {
            if (agentsTrouves.size() >= 1) {
                Map<String, Object> membre1 = agentsTrouves.get(0);
                Object id1 = membre1.get("id");
                Object matricule1 = membre1.get("matricule");
                if (id1 != null) projet.setMembre1Id(Long.valueOf(id1.toString()));
                if (matricule1 != null) projet.setMembre1Matricule(matricule1.toString());
            }
            if (agentsTrouves.size() >= 2) {
                Map<String, Object> membre2 = agentsTrouves.get(1);
                Object id2 = membre2.get("id");
                Object matricule2 = membre2.get("matricule");
                if (id2 != null) projet.setMembre2Id(Long.valueOf(id2.toString()));
                if (matricule2 != null) projet.setMembre2Matricule(matricule2.toString());
            }
        }
    }

    Projet saved = projetRepository.save(projet);
    return convertToDTO(saved);
}


    @Transactional
    public ProjetDTO updateProjet(Long id, CreateProjetRequest request) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        if (projet.getStatut() == StatutProjet.VALIDE || projet.getStatut() == StatutProjet.REJETE) {
            throw new IllegalStateException("Impossible de modifier ce projet : il a déjà été validé ou rejeté.");
        }

        projet.setTitre(request.getTitre());
        projet.setDescription(request.getDescription());
        projet.setProblematique(request.getProblematique());
        projet.setObjectif(request.getObjectif());
        projet.setRessources(request.getRessources());
        projet.setGains(request.getGains());
        projet.setIndicateurs(request.getIndicateurs());
        projet.setActeurs(request.getActeurs());

        // ✅ Mettre à jour les membres
        projet.setMembre1Id(null);
        projet.setMembre1Matricule(null);
        projet.setMembre2Id(null);
        projet.setMembre2Matricule(null);

        if (request.getMembresEquipe() != null) {
            if (request.getMembresEquipe().size() >= 1) {
                projet.setMembre1Id(Long.parseLong(request.getMembresEquipe().get(0).getMatricule()));
                projet.setMembre1Matricule(request.getMembresEquipe().get(0).getMatricule());
            }
            if (request.getMembresEquipe().size() == 2) {
                projet.setMembre2Id(Long.parseLong(request.getMembresEquipe().get(1).getMatricule()));
                projet.setMembre2Matricule(request.getMembresEquipe().get(1).getMatricule());
            }
        }

        Projet updated = projetRepository.save(projet);
        return convertToDTO(updated);
    }


    // ✅ Soumission d’un projet
    @Transactional
    public ProjetDTO soumettreProjet(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        if (projet.getSoumis()) {
            throw new IllegalStateException("Ce projet a déjà été soumis.");
        }

        projet.setSoumis(true);
        projet.setStatut(StatutProjet.EN_ATTENTE_MANAGER);

        Validation validation = new Validation();
        validation.setNiveau(NiveauValidation.NIVEAU_1);
        validation.setStatut(StatutValidation.EN_ATTENTE);
        projet.addValidation(validation);

        Projet saved = projetRepository.save(projet);
        return convertToDTO(saved);
    }

    // ✅ Suppression
    @Transactional
    public boolean deleteProjet(Long id) {
        return projetRepository.findById(id)
                .map(projet -> {
                    // Autoriser la suppression uniquement si le statut est EN_ATTENTE_MANAGER ou REJETE
                    if (projet.getStatut() != StatutProjet.EN_ATTENTE_MANAGER &&
                            projet.getStatut() != StatutProjet.REJETE) {
                        throw new IllegalStateException(
                                "Impossible de supprimer un projet en cours de validation (dont le statut n'est ni EN_ATTENTE_MANAGER ni REJETE)."
                        );
                    }
                    projetRepository.delete(projet);
                    return true;
                })
                .orElse(false);
    }


    // ✅ Compteurs
    public long getTotalProjets() {
        return projetRepository.count();
    }

    public long countByStatut(StatutProjet statut) {
        return projetRepository.countByStatut(statut);
    }

    public long countBrouillons(Long idAgent) {
        return projetRepository.countByIdAgentSoumissionAndSoumis(idAgent, false);
    }

    // ✅ Conversion entité → DTO
    private ProjetDTO convertToDTO(Projet projet) {
        ProjetDTO dto = new ProjetDTO();
        dto.setId(projet.getId());
        dto.setTitre(projet.getTitre());
        dto.setDescription(projet.getDescription());
        dto.setProblematique(projet.getProblematique());
        dto.setObjectif(projet.getObjectif());
        dto.setRessources(projet.getRessources());
        dto.setGains(projet.getGains());
        dto.setIndicateurs(projet.getIndicateurs());
        dto.setActeurs(projet.getActeurs());
        dto.setStatut(projet.getStatut());
        dto.setDateSoumission(projet.getDateSoumission());
        dto.setDateModification(projet.getDateModification());
        dto.setIdAgentSoumission(projet.getIdAgentSoumission());
        dto.setSoumis(projet.getSoumis());

        // ✅ Agent principal
        Map<String, Object> agent = agentService.getAgentById(projet.getIdAgentSoumission());
        if (agent != null && agent.get("fullName") != null) {
            dto.setAgentNom((String) agent.get("fullName"));
            dto.setAgentEmail((String) agent.get("email"));
            dto.setAgentMatricule(String.valueOf(agent.get("matricule")));
            dto.setAgentDirection(agent.get("direction") != null ? agent.get("direction").toString() : null);
            dto.setAgentService(agent.get("rattachement") != null ? agent.get("rattachement").toString() : null);
            dto.setAgentFonction(agent.get("fonction") != null ? agent.get("fonction").toString() : null);
        }


        // ✅ Validations (désactivé si service manquant)
        dto.setValidations(List.of());

        return dto;
    }




}
