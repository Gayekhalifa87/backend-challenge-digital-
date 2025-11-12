package com.challenge_digital.cila_bokk.controller;

import com.challenge_digital.cila_bokk.dto.CreateProjetRequest;
import com.challenge_digital.cila_bokk.dto.ProjetDTO;
import com.challenge_digital.cila_bokk.model.StatutProjet;
import com.challenge_digital.cila_bokk.service.ProjetService;
import com.challenge_digital.cila_bokk.service.external.AgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projets")
@RequiredArgsConstructor
@Slf4j
public class ProjetController {

    private final ProjetService projetService;
    private final AgentService agentService;

    @GetMapping
    public ResponseEntity<List<ProjetDTO>> getAllProjets() {
        return ResponseEntity.ok(projetService.getAllProjets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjetDTO> getProjetById(@PathVariable Long id) {
        return projetService.getProjetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<ProjetDTO>> getProjetsByAgent(@PathVariable Long agentId) {
        return ResponseEntity.ok(projetService.getProjetsByAgent(agentId));
    }

    @GetMapping("/agent/{agentId}/brouillons")
    public ResponseEntity<List<ProjetDTO>> getProjetsBrouillon(@PathVariable Long agentId) {
        return ResponseEntity.ok(projetService.getProjetsBrouillon(agentId));
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<ProjetDTO>> getProjetsByStatut(@PathVariable StatutProjet statut) {
        return ResponseEntity.ok(projetService.getProjetsByStatut(statut));
    }

    /**
     * ✅ NOUVEAU: Récupère les projets du rattachement (direction) de l'agent connecté
     * Utilise /api/user pour récupérer le rattachement
     */
    @GetMapping("/ma-direction")
    public ResponseEntity<?> getProjetsMaDirection() {
        try {
            // 1. Récupérer l'agent connecté via /api/user
            Map<String, Object> connectedAgent = agentService.getConnectedAgentDetails();

            log.info("🔍 Agent connecté récupéré: {}", connectedAgent);

            if (connectedAgent == null || connectedAgent.containsKey("message")) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "not_found",
                        "message", "Utilisateur non connecté ou introuvable"
                ));
            }

            // 2. ✅ Extraire le RATTACHEMENT (qui est la direction)
            Object rattachementObj = connectedAgent.get("rattachement");
            log.info("🔍 Rattachement brut: {}", rattachementObj);

            if (rattachementObj == null) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "no_direction",
                        "message", "L'agent connecté n'a pas de rattachement (direction) associé",
                        "agent", Map.of(
                                "id", connectedAgent.get("id"),
                                "fullName", connectedAgent.get("fullName"),
                                "email", connectedAgent.get("email")
                        )
                ));
            }

            Map<String, Object> rattachement = (Map<String, Object>) rattachementObj;

            if (rattachement.get("id") == null) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "invalid_direction",
                        "message", "Rattachement sans ID",
                        "rattachement", rattachement
                ));
            }

            // 3. Convertir l'ID du rattachement
            Long rattachementId = convertToLong(rattachement.get("id"));
            String rattachementName = (String) rattachement.get("name");
            String rattachementCode = (String) rattachement.get("code");

            // Récupérer le type pour confirmer que c'est une DIRECTION
            Map<String, Object> type = (Map<String, Object>) rattachement.get("type");
            String typeName = type != null ? (String) type.get("name") : "N/A";

            log.info("✅ Direction identifiée: {} (ID: {}, Code: {}, Type: {})",
                    rattachementName, rattachementId, rattachementCode, typeName);

            // 4. Récupérer TOUS les agents
            List<Map<String, Object>> allAgents = agentService.getAllAgents();
            log.info("📊 Total agents dans le système: {}", allAgents.size());

            // 5. ✅ Filtrer les agents de ce RATTACHEMENT
            List<Long> agentsInDirection = new ArrayList<>();

            for (Map<String, Object> agent : allAgents) {
                Object agentRattachementObj = agent.get("rattachement");
                if (agentRattachementObj instanceof Map) {
                    Map<String, Object> agentRattachement = (Map<String, Object>) agentRattachementObj;
                    Object agentRattachementIdObj = agentRattachement.get("id");

                    if (agentRattachementIdObj != null) {
                        Long agentRattachementId = convertToLong(agentRattachementIdObj);

                        if (rattachementId.equals(agentRattachementId)) {
                            Long agentId = convertToLong(agent.get("id"));
                            agentsInDirection.add(agentId);
                        }
                    }
                }
            }

            log.info("✅ Agents trouvés dans la direction '{}': {}", rattachementName, agentsInDirection.size());
            log.info("📋 IDs des agents: {}", agentsInDirection);

            if (agentsInDirection.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "agent", Map.of(
                                "id", connectedAgent.get("id"),
                                "fullName", connectedAgent.get("fullName")
                        ),
                        "direction", rattachement,
                        "statistiques", Map.of(
                                "totalAgentsDansDirection", 0,
                                "totalProjets", 0
                        ),
                        "projets", List.of(),
                        "message", "Aucun agent trouvé dans cette direction"
                ));
            }

            // 6. Récupérer TOUS les projets directement (sans filtrage)
            List<ProjetDTO> tousLesProjets = projetService.getAllProjets();
            log.info("📊 Total projets dans le système: {}", tousLesProjets.size());

            // 7. Filtrer les projets de la direction
            List<ProjetDTO> projetsDeLaDirection = new ArrayList<>();

            for (ProjetDTO projet : tousLesProjets) {
                Long projetAgentId = projet.getIdAgentSoumission();
                if (agentsInDirection.contains(projetAgentId)) {
                    projetsDeLaDirection.add(projet);
                }
            }

            log.info("✅ Projets trouvés dans la direction: {}", projetsDeLaDirection.size());

            // 8. Statistiques par statut
            Map<String, Long> statutStats = new HashMap<>();
            for (ProjetDTO projet : projetsDeLaDirection) {
                String statut = projet.getStatut() != null ? projet.getStatut().toString() : "AUCUN_STATUT";
                statutStats.put(statut, statutStats.getOrDefault(statut, 0L) + 1);
            }

            // 9. Construire la réponse avec parent si disponible
            Map<String, Object> directionResponse = new HashMap<>();
            directionResponse.put("id", rattachementId);
            directionResponse.put("name", rattachementName);
            directionResponse.put("code", rattachementCode);
            directionResponse.put("active", rattachement.getOrDefault("active", true));
            directionResponse.put("type", type);

            if (rattachement.get("parent") != null) {
                directionResponse.put("parent", rattachement.get("parent"));
            }

            // 10. Construire la réponse
            return ResponseEntity.ok(Map.of(
                    "agent", Map.of(
                            "id", connectedAgent.get("id"),
                            "fullName", connectedAgent.get("fullName"),
                            "email", connectedAgent.get("email"),
                            "matricule", connectedAgent.getOrDefault("matricule", "N/A")
                    ),
                    "direction", directionResponse,
                    "statistiques", Map.of(
                            "totalAgentsDansDirection", agentsInDirection.size(),
                            "totalProjets", projetsDeLaDirection.size(),
                            "parStatut", statutStats
                    ),
                    "projets", projetsDeLaDirection
            ));

        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des projets de la direction", e);
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "server_error",
                    "message", "Erreur: " + e.getMessage(),
                    "details", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * ✅ Récupère les projets par direction spécifique
     */
    @GetMapping("/direction/{directionId}")
    public ResponseEntity<?> getProjetsByDirection(@PathVariable Long directionId) {
        try {
            log.info("🔍 Recherche des projets pour directionId: {}", directionId);

            // 1. Récupérer tous les agents
            List<Map<String, Object>> allAgents = agentService.getAllAgents();
            log.info("📊 Total agents: {}", allAgents.size());

            // 2. Filtrer les agents de cette direction
            List<Long> agentsInDirection = new ArrayList<>();
            Map<String, Object> directionInfo = null;

            for (Map<String, Object> agent : allAgents) {
                Object directionObj = agent.get("direction");
                if (directionObj instanceof Map) {
                    Map<String, Object> agentDir = (Map<String, Object>) directionObj;
                    Object dirIdObj = agentDir.get("id");

                    if (dirIdObj != null) {
                        Long agentDirectionId = convertToLong(dirIdObj);

                        if (directionId.equals(agentDirectionId)) {
                            Long agentId = convertToLong(agent.get("id"));
                            agentsInDirection.add(agentId);

                            // Capturer les infos de la direction
                            if (directionInfo == null) {
                                directionInfo = new HashMap<>();
                                directionInfo.put("id", agentDir.get("id"));
                                directionInfo.put("name", agentDir.get("name"));
                                directionInfo.put("code", agentDir.get("code"));
                                directionInfo.put("active", agentDir.get("active"));
                            }
                        }
                    }
                }
            }

            log.info("✅ Agents trouvés dans directionId {}: {}", directionId, agentsInDirection.size());

            if (agentsInDirection.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "directionId", directionId,
                        "message", "Aucun agent trouvé dans cette direction",
                        "projets", List.of()
                ));
            }

            // 3. Récupérer les projets
            List<ProjetDTO> tousLesProjets = projetService.getAllProjets();
            List<ProjetDTO> projetsDeLaDirection = new ArrayList<>();

            for (ProjetDTO projet : tousLesProjets) {
                if (agentsInDirection.contains(projet.getIdAgentSoumission())) {
                    projetsDeLaDirection.add(projet);
                }
            }

            log.info("✅ Projets trouvés: {}", projetsDeLaDirection.size());

            // 4. Construire la réponse
            if (directionInfo == null) {
                directionInfo = Map.of(
                        "id", directionId,
                        "name", "Direction inconnue",
                        "code", "N/A",
                        "active", true
                );
            }

            return ResponseEntity.ok(Map.of(
                    "direction", directionInfo,
                    "statistiques", Map.of(
                            "totalAgents", agentsInDirection.size(),
                            "totalProjets", projetsDeLaDirection.size()
                    ),
                    "projets", projetsDeLaDirection
            ));

        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des projets par direction", e);
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "server_error",
                    "message", "Erreur: " + e.getMessage()
            ));
        }
    }

    /**
     * ✅ Statistiques de toutes les directions
     */
    @GetMapping("/directions/stats")
    public ResponseEntity<?> getDirectionsStats() {
        try {
            List<Map<String, Object>> allAgents = agentService.getAllAgents();
            List<ProjetDTO> allProjets = projetService.getAllProjets();

            Map<String, Map<String, Object>> directionsMap = new HashMap<>();

            // Grouper les agents par direction
            for (Map<String, Object> agent : allAgents) {
                Object directionObj = agent.get("direction");
                if (directionObj instanceof Map) {
                    Map<String, Object> direction = (Map<String, Object>) directionObj;
                    Object dirIdObj = direction.get("id");

                    if (dirIdObj != null) {
                        String directionId = dirIdObj.toString();

                        if (!directionsMap.containsKey(directionId)) {
                            Map<String, Object> directionData = new HashMap<>();
                            directionData.put("id", direction.get("id"));
                            directionData.put("name", direction.get("name"));
                            directionData.put("code", direction.get("code"));
                            directionData.put("active", direction.get("active"));
                            directionData.put("agentsCount", 0);
                            directionData.put("projetsCount", 0);

                            directionsMap.put(directionId, directionData);
                        }

                        Map<String, Object> dirStats = directionsMap.get(directionId);
                        dirStats.put("agentsCount", (Integer) dirStats.get("agentsCount") + 1);
                    }
                }
            }

            // Compter les projets par direction
            for (ProjetDTO projet : allProjets) {
                Long agentId = projet.getIdAgentSoumission();

                for (Map<String, Object> agent : allAgents) {
                    Long aId = convertToLong(agent.get("id"));
                    if (aId.equals(agentId)) {
                        Object directionObj = agent.get("direction");
                        if (directionObj instanceof Map) {
                            Map<String, Object> direction = (Map<String, Object>) directionObj;
                            String directionId = direction.get("id").toString();

                            if (directionsMap.containsKey(directionId)) {
                                Map<String, Object> dirStats = directionsMap.get(directionId);
                                dirStats.put("projetsCount", (Integer) dirStats.get("projetsCount") + 1);
                            }
                        }
                        break;
                    }
                }
            }

            List<Map<String, Object>> directionsList = new ArrayList<>(directionsMap.values());
            directionsList.sort((a, b) ->
                    String.valueOf(a.get("name")).compareTo(String.valueOf(b.get("name")))
            );

            return ResponseEntity.ok(Map.of(
                    "totalDirections", directionsList.size(),
                    "totalProjets", allProjets.size(),
                    "totalAgents", allAgents.size(),
                    "directions", directionsList
            ));

        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des statistiques", e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "server_error",
                    "message", "Erreur: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTotalProjets() {
        return ResponseEntity.ok(projetService.getTotalProjets());
    }

    @GetMapping("/count/statut/{statut}")
    public ResponseEntity<Long> countByStatut(@PathVariable StatutProjet statut) {
        return ResponseEntity.ok(projetService.countByStatut(statut));
    }

    @GetMapping("/count/agent/{agentId}/brouillons")
    public ResponseEntity<Long> countBrouillons(@PathVariable Long agentId) {
        return ResponseEntity.ok(projetService.countBrouillons(agentId));
    }

    @PostMapping("/validate-equipe")
    public ResponseEntity<?> validateEquipe(@RequestBody List<String> matricules) {
        try {
            Map<String, Object> validation = agentService.validateEquipe(matricules);
            return ResponseEntity.ok(validation);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur lors de la validation: " + e.getMessage()));
        }
    }

    @PostMapping("/search-agents")
    public ResponseEntity<?> searchAgentsByMatricules(@RequestBody List<String> matricules) {
        try {
            List<Map<String, Object>> agents = agentService.getAgentsByMatricules(matricules);
            return ResponseEntity.ok(agents);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur lors de la recherche: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createProjet(@RequestBody CreateProjetRequest request) {
        try {
            Map<String, Object> connectedAgent = agentService.getConnectedAgentDetails();

            if (connectedAgent == null || connectedAgent.containsKey("message")) {
                return ResponseEntity.status(401).body(Map.of(
                        "message", "Utilisateur non authentifié ou introuvable"
                ));
            }

            Long idAgent = convertToLong(connectedAgent.get("id"));
            request.setIdAgentSoumission(idAgent);

            ProjetDTO created = projetService.createProjet(request);

            return ResponseEntity.ok(Map.of(
                    "message", "Projet créé avec succès",
                    "agent", connectedAgent,
                    "projet", created
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProjet(@PathVariable Long id, @RequestBody CreateProjetRequest request) {
        try {
            ProjetDTO updated = projetService.updateProjet(id, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/soumettre")
    public ResponseEntity<?> soumettreProjet(@PathVariable Long id) {
        try {
            ProjetDTO soumis = projetService.soumettreProjet(id);
            return ResponseEntity.ok(soumis);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProjet(@PathVariable Long id) {
        try {
            boolean deleted = projetService.deleteProjet(id);
            return deleted ?
                    ResponseEntity.noContent().build() :
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMesProjets() {
        try {
            Map<String, Object> connectedAgent = agentService.getConnectedAgentDetails();

            if (connectedAgent.containsKey("message")) {
                return ResponseEntity.status(404).body(connectedAgent);
            }

            Long agentId = convertToLong(connectedAgent.get("id"));
            List<ProjetDTO> projets = projetService.getProjetsByAgent(agentId);

            return ResponseEntity.ok(Map.of(
                    "agent", connectedAgent,
                    "projets", projets
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur lors de la récupération des projets: " + e.getMessage()));
        }
    }

    /**
     * ✅ Utilitaire pour convertir Integer/Long/String en Long
     */
    private Long convertToLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        return Long.valueOf(value.toString());
    }
}