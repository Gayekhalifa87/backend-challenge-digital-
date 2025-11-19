package com.challenge_digital.cila_bokk.service;

import com.challenge_digital.cila_bokk.dto.CreateProjetRequest;
import com.challenge_digital.cila_bokk.dto.ProjetDTO;
import com.challenge_digital.cila_bokk.dto.ValidationDTO;
import com.challenge_digital.cila_bokk.model.*;
import com.challenge_digital.cila_bokk.repository.ProjetRepository;
import com.challenge_digital.cila_bokk.service.external.AgentApiDto;
import com.challenge_digital.cila_bokk.service.external.AgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
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

//@Transactional
//public ProjetDTO createProjet(CreateProjetRequest request) {
//    long nbProjets = projetRepository.countByIdAgentSoumissionAndSoumis(request.getIdAgentSoumission(), true);
//    if (nbProjets >= 2) {
//        throw new IllegalArgumentException("L'agent a déjà soumis 2 projets maximum.");
//    }
//
//    if (request.getMembresEquipe() != null && request.getMembresEquipe().size() > 2) {
//        throw new IllegalArgumentException("Une équipe ne peut pas dépasser 2 membres.");
//    }
//
//    Projet projet = new Projet();
//    projet.setTeamName(request.getTeamName());
//    projet.setTitre(request.getTitre());
//    projet.setProblematique(request.getProblematique());
//    projet.setDescription(request.getDescription());
//    projet.setObjectif(request.getObjectif());
//    projet.setRessources(request.getRessources());
//    projet.setGains(request.getGains());
//    projet.setActeurs(request.getActeurs());
//    projet.setIndicateurs(request.getIndicateurs());
//    projet.setIdAgentSoumission(request.getIdAgentSoumission());
//    projet.setSoumis(true);
//
//    // ✅ Récupérer les IDs réels via AgentService
//    if (request.getMembresEquipe() != null && !request.getMembresEquipe().isEmpty()) {
//        List<String> matricules = request.getMembresEquipe().stream()
//                .map(m -> m.getMatricule())
//                .collect(Collectors.toList());
//
//        // Appel au service pour trouver les agents correspondants
//        List<Map<String, Object>> agentsTrouves = agentService.getAgentsByMatricules(matricules);
//
//        if (!agentsTrouves.isEmpty()) {
//            if (agentsTrouves.size() >= 1) {
//                Map<String, Object> membre1 = agentsTrouves.get(0);
//                Object id1 = membre1.get("id");
//                Object matricule1 = membre1.get("matricule");
//                if (id1 != null) projet.setMembre1Id(Long.valueOf(id1.toString()));
//                if (matricule1 != null) projet.setMembre1Matricule(matricule1.toString());
//            }
//            if (agentsTrouves.size() >= 2) {
//                Map<String, Object> membre2 = agentsTrouves.get(1);
//                Object id2 = membre2.get("id");
//                Object matricule2 = membre2.get("matricule");
//                if (id2 != null) projet.setMembre2Id(Long.valueOf(id2.toString()));
//                if (matricule2 != null) projet.setMembre2Matricule(matricule2.toString());
//            }
//        }
//    }
//
//    Projet saved = projetRepository.save(projet);
//    return convertToDTO(saved);
//}


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

//    public long countBrouillons(Long idAgent) {
//        return projetRepository.countByIdAgentSoumissionAndSoumis(idAgent, false);
//    }

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


    private void verifierDroitValidationManager(Projet projet) {

        // 1️⃣ Récupérer l’agent connecté (manager)
        Map<String, Object> connected = agentService.getConnectedAgentDetails();
        if (connected == null || connected.containsKey("message")) {
            throw new IllegalStateException("Impossible d'identifier l'utilisateur connecté.");
        }

        Map<String, Object> rattachementManager = (Map<String, Object>) connected.get("rattachement");
        if (rattachementManager == null) {
            throw new IllegalStateException("Aucune direction associée au manager connecté.");
        }

        Long directionManagerId = Long.valueOf(rattachementManager.get("id").toString());

        // 2️⃣ Récupérer l’agent qui a soumis le projet
        Long idAgentProjet = projet.getIdAgentSoumission();
        Map<String, Object> agentProjet = agentService.getAgentById(idAgentProjet);

        if (agentProjet == null || agentProjet.containsKey("message")) {
            throw new IllegalStateException("Impossible d'obtenir l'agent du projet.");
        }

        Map<String, Object> rattachementAgent = (Map<String, Object>) agentProjet.get("rattachement");
        if (rattachementAgent == null) {
            throw new IllegalStateException("Aucune direction trouvée pour l'agent du projet.");
        }

        Long directionProjet = Long.valueOf(rattachementAgent.get("id").toString());

        // 3️⃣ Vérifier les droits
        if (!directionManagerId.equals(directionProjet)) {
            throw new IllegalStateException(
                    "❌ Vous ne pouvez valider ou rejeter que les projets de votre propre direction."
            );
        }
    }




//    @Transactional
//    public ProjetDTO validerParManager(Long id) {
//        Projet projet = projetRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));
//
//        if (projet.getStatut() != StatutProjet.EN_ATTENTE_MANAGER) {
//            throw new IllegalStateException(
//                    "Le projet doit être au statut EN_ATTENTE_MANAGER pour être transmis au comité."
//            );
//        }
//
//        projet.setStatut(StatutProjet.EN_ATTENTE_COMITE);
//        projetRepository.save(projet);
//
//        return convertToDTO(projet);
//    }

    public ProjetDTO validerParManager(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        // 🔒 Empêcher la validation d’un projet d'une autre direction
        verifierDroitValidationManager(projet);

        if (projet.getStatut() != StatutProjet.EN_ATTENTE_MANAGER) {
            throw new IllegalStateException(
                    "Le projet doit être au statut EN_ATTENTE_MANAGER pour être transmis au comité."
            );
        }

        projet.setStatut(StatutProjet.EN_ATTENTE_COMITE);
        projetRepository.save(projet);

        return convertToDTO(projet);
    }




    @Transactional
    public ProjetDTO validerParComite(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        if (projet.getStatut() != StatutProjet.EN_ATTENTE_COMITE) {
            throw new IllegalStateException(
                    "Le projet doit être au statut EN_ATTENTE_COMITE pour être validé."
            );
        }

        projet.setStatut(StatutProjet.VALIDE);
        projetRepository.save(projet);
        return convertToDTO(projet);
    }


    @Transactional
    public ProjetDTO rejeterParComite(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        if (projet.getStatut() != StatutProjet.EN_ATTENTE_COMITE) {
            throw new IllegalStateException(
                    "Le projet doit être au statut EN_ATTENTE_COMITE pour être rejeté par le comité."
            );
        }

        projet.setStatut(StatutProjet.REJETE);
        projetRepository.save(projet);
        return convertToDTO(projet);
    }



    public ProjetDTO rejeterParManager(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        // 🔒 Empêcher le rejet d’un projet d'une autre direction
        verifierDroitValidationManager(projet);

        if (projet.getStatut() != StatutProjet.EN_ATTENTE_MANAGER) {
            throw new IllegalStateException(
                    "Le projet doit être au statut EN_ATTENTE_MANAGER pour être rejeté par le manager."
            );
        }

        projet.setStatut(StatutProjet.REJETE);
        projetRepository.save(projet);
        return convertToDTO(projet);
    }


//    @Transactional
//    public ProjetDTO annulerAction(Long id) {
//        Projet projet = projetRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));
//
//        switch (projet.getStatut()) {
//            case EN_ATTENTE_COMITE:
//                // Annulation de la validation par le manager → revenir à EN_ATTENTE_MANAGER
//                projet.setStatut(StatutProjet.EN_ATTENTE_MANAGER);
//                break;
//
//            case VALIDE:
//                // Annulation de la validation par le comité → revenir à EN_ATTENTE_COMITE
//                projet.setStatut(StatutProjet.EN_ATTENTE_COMITE);
//                break;
//
//            case REJETE:
//                // Ici on peut différencier si le rejet vient du manager ou du comité
//                // Si nécessaire, on peut ajouter un champ dernierRejet pour savoir
//                // Pour simplifier, on revient à EN_ATTENTE_MANAGER si le projet est rejeté par manager
//                // ou EN_ATTENTE_COMITE si rejeté par comité
//                // Si tu n’as pas ce champ, tu peux choisir une valeur par défaut :
//                projet.setStatut(StatutProjet.EN_ATTENTE_MANAGER);
//                break;
//
//            default:
//                throw new IllegalStateException("Impossible d'annuler ce statut : " + projet.getStatut());
//        }
//
//        Projet updated = projetRepository.save(projet);
//        return convertToDTO(updated);
//    }



    public List<ProjetDTO> getProjetsParEntiteParent(String nomEntite, String codeEntite) {
        List<ProjetDTO> allProjets = getAllProjets();
        List<Map<String, Object>> allAgents = agentService.getAllAgents();

        List<Long> agentsCorrespondants = new ArrayList<>();

        for (Map<String, Object> agent : allAgents) {
            Object rattachementObj = agent.get("rattachement");
            if (rattachementObj instanceof Map) {
                Map<String, Object> rattachement = (Map<String, Object>) rattachementObj;
                Object parentObj = rattachement.get("parent");
                if (parentObj instanceof Map) {
                    Map<String, Object> parent = (Map<String, Object>) parentObj;
                    String parentName = parent.get("name") != null ? parent.get("name").toString() : "";
                    String parentCode = parent.get("code") != null ? parent.get("code").toString() : "";

                    if (nomEntite.equalsIgnoreCase(parentName) && codeEntite.equals(parentCode)) {
                        Object idObj = agent.get("id");
                        if (idObj != null) {
                            Long agentId = Long.valueOf(idObj.toString()); // <-- remplacement de convertToLong
                            agentsCorrespondants.add(agentId);
                        }
                    }

                }
            }
        }

        // Filtrer les projets dont l'agent de soumission correspond
        return allProjets.stream()
                .filter(p -> agentsCorrespondants.contains(p.getIdAgentSoumission()))
                .collect(Collectors.toList());
    }







    //Ajouter la gestion par les dt


    /**
     * ✅ Vérifie si un agent est rattaché à une entité DTO
     * @param agentId ID de l'agent à vérifier
     * @return true si l'agent est rattaché à une entité dont le parent a le code "70" (DTO)
     */
    private boolean isAgentRattacheDTO(Long agentId) {
        try {
            Map<String, Object> agent = agentService.getAgentById(agentId);
            if (agent == null || agent.containsKey("message")) {
                return false;
            }

            // Récupérer le rattachement de l'agent
            Map<String, Object> rattachement = (Map<String, Object>) agent.get("rattachement");
            if (rattachement == null) {
                return false;
            }

            // Vérifier si le parent existe
            Map<String, Object> parent = (Map<String, Object>) rattachement.get("parent");
            if (parent == null) {
                return false;
            }

            // Vérifier si le code du parent est "70" (DTO)
            String parentCode = (String) parent.get("code");
            String parentName = (String) parent.get("name");

            log.info("🔍 Vérification DTO - Agent: {}, Parent Code: {}, Parent Name: {}",
                    agentId, parentCode, parentName);

            return "70".equals(parentCode) && "DTO".equalsIgnoreCase(parentName);

        } catch (Exception e) {
            log.error("❌ Erreur lors de la vérification du rattachement DTO pour l'agent {}", agentId, e);
            return false;
        }
    }

    /**
     * ✅ MODIFIÉ: Création de projet avec gestion du statut DT
     */
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

        // ✅ NOUVEAU : Définir le statut selon le rattachement
        if (isAgentRattacheDTO(request.getIdAgentSoumission())) {
            projet.setStatut(StatutProjet.EN_ATTENTE_DT);
            log.info("✅ Projet soumis par un agent DTO → Statut: EN_ATTENTE_DT");
        } else {
            projet.setStatut(StatutProjet.EN_ATTENTE_MANAGER);
            log.info("✅ Projet soumis par un agent non-DTO → Statut: EN_ATTENTE_MANAGER");
        }

        // ✅ Récupérer les IDs réels via AgentService
        if (request.getMembresEquipe() != null && !request.getMembresEquipe().isEmpty()) {
            List<String> matricules = request.getMembresEquipe().stream()
                    .map(m -> m.getMatricule())
                    .collect(Collectors.toList());

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

    /**
     * ✅ NOUVEAU: Valider un projet DT et le transférer au manager
     * Endpoint: PATCH /api/projets/{id}/valider-dt
     */
    @Transactional
    public ProjetDTO validerParDT(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        if (projet.getStatut() != StatutProjet.EN_ATTENTE_DT) {
            throw new IllegalStateException(
                    "Le projet doit être au statut EN_ATTENTE_DT pour être validé par le DT. Statut actuel: " + projet.getStatut()
            );
        }

        // Vérifier que l'utilisateur connecté a le droit de valider (optionnel)
        verifierDroitValidationDT(projet);

        projet.setStatut(StatutProjet.EN_ATTENTE_MANAGER);
        projetRepository.save(projet);

        log.info("✅ Projet {} validé par le DT → Statut: EN_ATTENTE_MANAGER", id);

        return convertToDTO(projet);
    }

    /**
     * ✅ NOUVEAU: Rejeter un projet DT
     * Endpoint: PATCH /api/projets/{id}/rejeter-dt
     */
    @Transactional
    public ProjetDTO rejeterParDT(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        if (projet.getStatut() != StatutProjet.EN_ATTENTE_DT) {
            throw new IllegalStateException(
                    "Le projet doit être au statut EN_ATTENTE_DT pour être rejeté par le DT."
            );
        }

        verifierDroitValidationDT(projet);

        projet.setStatut(StatutProjet.REJETE);
        projetRepository.save(projet);

        log.info("✅ Projet {} rejeté par le DT → Statut: REJETE", id);

        return convertToDTO(projet);
    }

    /**
     * ✅ NOUVEAU: Vérifie les droits de validation DT
     */
    private void verifierDroitValidationDT(Projet projet) {
        // Récupérer l'utilisateur connecté
        Map<String, Object> connected = agentService.getConnectedAgentDetails();
        if (connected == null || connected.containsKey("message")) {
            throw new IllegalStateException("Impossible d'identifier l'utilisateur connecté.");
        }

        // Vérifier que l'utilisateur connecté est bien un DT
        Map<String, Object> rattachementDT = (Map<String, Object>) connected.get("rattachement");
        if (rattachementDT == null) {
            throw new IllegalStateException("Aucun rattachement trouvé pour l'utilisateur connecté.");
        }

        // Vérifier que le parent du rattachement est bien "DTO"
        Map<String, Object> parent = (Map<String, Object>) rattachementDT.get("parent");
        if (parent == null) {
            throw new IllegalStateException("L'utilisateur connecté n'est pas rattaché à une entité DTO.");
        }

        String parentCode = (String) parent.get("code");
        if (!"70".equals(parentCode)) {
            throw new IllegalStateException("Seuls les DT peuvent valider ou rejeter ce type de projet.");
        }

        // Vérifier que le projet provient bien d'un agent du même rattachement
        Long idAgentProjet = projet.getIdAgentSoumission();
        Map<String, Object> agentProjet = agentService.getAgentById(idAgentProjet);

        if (agentProjet == null || agentProjet.containsKey("message")) {
            throw new IllegalStateException("Impossible d'obtenir l'agent du projet.");
        }

        Map<String, Object> rattachementAgent = (Map<String, Object>) agentProjet.get("rattachement");
        if (rattachementAgent == null) {
            throw new IllegalStateException("Aucun rattachement trouvé pour l'agent du projet.");
        }

        Long rattachementIdDT = Long.valueOf(rattachementDT.get("id").toString());
        Long rattachementIdProjet = Long.valueOf(rattachementAgent.get("id").toString());

        if (!rattachementIdDT.equals(rattachementIdProjet)) {
            throw new IllegalStateException(
                    "❌ Vous ne pouvez valider que les projets de votre propre rattachement."
            );
        }
    }

    /**
     * ✅ MODIFIÉ: Annulation pour gérer le statut EN_ATTENTE_DT
     */
    @Transactional
    public ProjetDTO annulerAction(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        switch (projet.getStatut()) {
            case EN_ATTENTE_MANAGER:
                // Si le projet vient du DT, revenir à EN_ATTENTE_DT
                if (isAgentRattacheDTO(projet.getIdAgentSoumission())) {
                    projet.setStatut(StatutProjet.EN_ATTENTE_DT);
                    log.info("✅ Annulation: EN_ATTENTE_MANAGER → EN_ATTENTE_DT");
                } else {
                    throw new IllegalStateException("Impossible d'annuler ce statut pour un projet non-DT");
                }
                break;

            case EN_ATTENTE_COMITE:
                projet.setStatut(StatutProjet.EN_ATTENTE_MANAGER);
                break;

            case VALIDE:
                projet.setStatut(StatutProjet.EN_ATTENTE_COMITE);
                break;

            case REJETE:
                // Revenir au statut précédent selon le type d'agent
                if (isAgentRattacheDTO(projet.getIdAgentSoumission())) {
                    projet.setStatut(StatutProjet.EN_ATTENTE_DT);
                } else {
                    projet.setStatut(StatutProjet.EN_ATTENTE_MANAGER);
                }
                break;

            default:
                throw new IllegalStateException("Impossible d'annuler ce statut : " + projet.getStatut());
        }

        Projet updated = projetRepository.save(projet);
        return convertToDTO(updated);
    }


    public long getNombreProjetsAgent(Long agentId) {
        return projetRepository.countByAgentId(agentId);
    }

}
