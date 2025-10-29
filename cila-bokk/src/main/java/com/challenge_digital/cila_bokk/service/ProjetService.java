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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final ValidationService validationService;
    private final AgentService agentService;

    public List<ProjetDTO> getAllProjets() {
        return projetRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Optional<ProjetDTO> getProjetById(Long id) {
        return projetRepository.findById(id).map(this::convertToDTO);
    }

    public List<ProjetDTO> getProjetsByAgent(Long idAgent) {
        return projetRepository.findByIdAgentSoumission(idAgent).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ProjetDTO> getProjetsByStatut(StatutProjet statut) {
        return projetRepository.findByStatut(statut).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ProjetDTO> getProjetsBrouillon(Long idAgent) {
        return projetRepository.findByIdAgentSoumissionAndSoumis(idAgent, false).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public ProjetDTO createProjet(CreateProjetRequest request) {
        // Vérifier que l'agent soumissionnaire n'a pas déjà 2 projets soumis
        long nbProjets = projetRepository.countByIdAgentSoumissionAndSoumis(request.getIdAgentSoumission(), true);
        if (nbProjets >= 2) {
            throw new IllegalArgumentException("L'agent a déjà soumis 2 projets maximum.");
        }

        // Vérifier que l'agent soumissionnaire existe
        if (!agentService.agentExists(request.getIdAgentSoumission())) {
            throw new IllegalArgumentException("L'agent soumissionnaire avec l'ID " + request.getIdAgentSoumission() + " n'existe pas.");
        }

        Projet projet = new Projet();
        projet.setTitre(request.getTitre());
        projet.setDescription(request.getDescription());
        projet.setObjectif(request.getObjectif());
        projet.setRessources(request.getRessources());
        projet.setGains(request.getGains());
        projet.setActeurs(request.getActeurs());
        projet.setIndicateurs(request.getIndicateurs());
        projet.setIdAgentSoumission(request.getIdAgentSoumission());
        projet.setSoumis(false);

        // ✅ Gestion des membres d'équipe (max 2 membres)
        List<Long> membresIds = new ArrayList<>();

        if (request.getMembresEquipe() != null && !request.getMembresEquipe().isEmpty()) {
            // Vérifier que l'équipe ne dépasse pas 2 membres
            if (request.getMembresEquipe().size() > 2) {
                throw new IllegalArgumentException("Une équipe ne peut pas dépasser 2 membres.");
            }

            // Valider chaque membre via son matricule
            for (CreateProjetRequest.MembreDTO membre : request.getMembresEquipe()) {
                if (membre.getMatricule() == null || membre.getMatricule().trim().isEmpty()) {
                    throw new IllegalArgumentException("Le matricule est obligatoire pour tous les membres de l'équipe.");
                }

                // ✅ Rechercher l'agent par matricule dans l'API externe
                AgentApiDto agentMembre = agentService.getAgentByMatricule(membre.getMatricule());

                if (agentMembre == null) {
                    throw new IllegalArgumentException("Aucun agent trouvé avec le matricule : " + membre.getMatricule());
                }

                if (!Boolean.TRUE.equals(agentMembre.getActive())) {
                    throw new IllegalArgumentException("L'agent avec le matricule " + membre.getMatricule() + " n'est pas actif.");
                }

                // ✅ Vérifier que le nom et prénom correspondent (optionnel mais recommandé)
                if (membre.getNom() != null && !membre.getNom().trim().isEmpty()) {
                    if (!agentMembre.getFullName().toLowerCase().contains(membre.getNom().toLowerCase())) {
                        throw new IllegalArgumentException(
                                "Le nom fourni (" + membre.getNom() + ") ne correspond pas au matricule " + membre.getMatricule()
                        );
                    }
                }

                // Ajouter l'ID de l'agent validé
                membresIds.add(agentMembre.getId());
            }
        }

        projet.setMembresEquipe(membresIds);

        Projet saved = projetRepository.save(projet);
        return convertToDTO(saved);
    }

    @Transactional
    public ProjetDTO updateProjet(Long projetId, CreateProjetRequest request) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        if (projet.getStatut() == StatutProjet.VALIDE || projet.getStatut() == StatutProjet.REJETE) {
            throw new IllegalStateException("Impossible de modifier ce projet : il a déjà été validé ou rejeté.");
        }

        projet.setTitre(request.getTitre());
        projet.setDescription(request.getDescription());
        projet.setObjectif(request.getObjectif());
        projet.setRessources(request.getRessources());
        projet.setGains(request.getGains());
        projet.setIndicateurs(request.getIndicateurs());
        projet.setActeurs(request.getActeurs());

        // ✅ Mise à jour des membres d'équipe
        List<Long> membresIds = new ArrayList<>();

        if (request.getMembresEquipe() != null && !request.getMembresEquipe().isEmpty()) {
            if (request.getMembresEquipe().size() > 2) {
                throw new IllegalArgumentException("Une équipe ne peut pas dépasser 2 membres.");
            }

            for (CreateProjetRequest.MembreDTO membre : request.getMembresEquipe()) {
                if (membre.getMatricule() == null || membre.getMatricule().trim().isEmpty()) {
                    throw new IllegalArgumentException("Le matricule est obligatoire pour tous les membres.");
                }

                AgentApiDto agentMembre = agentService.getAgentByMatricule(membre.getMatricule());

                if (agentMembre == null) {
                    throw new IllegalArgumentException("Aucun agent trouvé avec le matricule : " + membre.getMatricule());
                }

                if (!Boolean.TRUE.equals(agentMembre.getActive())) {
                    throw new IllegalArgumentException("L'agent avec le matricule " + membre.getMatricule() + " n'est pas actif.");
                }

                membresIds.add(agentMembre.getId());
            }
        }

        projet.setMembresEquipe(membresIds);

        Projet updated = projetRepository.save(projet);
        return convertToDTO(updated);
    }

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

    @Transactional
    public boolean deleteProjet(Long id) {
        return projetRepository.findById(id).map(projet -> {
            if (projet.getSoumis() && projet.getStatut() != StatutProjet.REJETE) {
                throw new IllegalStateException("Impossible de supprimer un projet en cours de validation.");
            }
            projetRepository.delete(projet);
            return true;
        }).orElse(false);
    }

    public long getTotalProjets() {
        return projetRepository.count();
    }

    public long countByStatut(StatutProjet statut) {
        return projetRepository.countByStatut(statut);
    }

    public long countBrouillons(Long idAgent) {
        return projetRepository.countByIdAgentSoumissionAndSoumis(idAgent, false);
    }

    private ProjetDTO convertToDTO(Projet projet) {
        ProjetDTO dto = new ProjetDTO();
        dto.setId(projet.getId());
        dto.setTitre(projet.getTitre());
        dto.setDescription(projet.getDescription());
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

        // Agent principal
        AgentApiDto agent = agentService.getAgentById(projet.getIdAgentSoumission());
        if (agent != null) {
            dto.setAgentNom(agent.getFullName());
            dto.setAgentEmail(agent.getEmail());
            dto.setAgentMatricule(agent.getMatricule() != null ? agent.getMatricule().toString() : null);
            dto.setAgentDirection(agent.getDirection() != null ? agent.getDirection().getName() : null);
            dto.setAgentService(agent.getRattachement() != null ? agent.getRattachement().getName() : null);
            dto.setAgentFonction(agent.getFonction() != null ? agent.getFonction().getName() : null);
        }

        // Membres équipe
        List<ProjetDTO.MembreEquipeDTO> membresDTO = (projet.getMembresEquipe() != null && !projet.getMembresEquipe().isEmpty())
                ? projet.getMembresEquipe().stream()
                .map(id -> {
                    AgentApiDto membre = agentService.getAgentById(id);
                    if (membre != null) {
                        // ✅ Séparer le fullName en nom et prénom
                        String[] nameParts = membre.getFullName().split("\\s+", 2);
                        String prenom = nameParts.length > 0 ? nameParts[0] : membre.getFullName();
                        String nom = nameParts.length > 1 ? nameParts[1] : "";

                        return new ProjetDTO.MembreEquipeDTO(
                                nom,
                                prenom,
                                membre.getMatricule() != null ? membre.getMatricule().toString() : null
                        );
                    } else {
                        return new ProjetDTO.MembreEquipeDTO("Inconnu", "", null);
                    }
                })
                .collect(Collectors.toList())
                : List.of();
        dto.setMembresEquipe(membresDTO);

        // Validations
        List<ValidationDTO> validations = projet.getValidations().stream()
                .map(validationService::convertToDTO)
                .collect(Collectors.toList());
        dto.setValidations(validations);

        return dto;
    }
}