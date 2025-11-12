//
package com.challenge_digital.cila_bokk.service.external;

import com.challenge_digital.cila_bokk.config.KeycloakUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentService {

    private final AgentApiClient agentFeignClient;
    private final KeycloakUserService keycloakUserService;

    public List<Map<String, Object>> getAllAgents() {
        try {
            Map<String, Object> response = agentFeignClient.getAllAgents(0, 100000);
            log.info("Réponse brute de l'API agents: {}", response);
            if (response == null || response.get("results") == null) {
                log.warn("Aucun agent reçu de l'API externe.");
                return Collections.emptyList();
            }
            return (List<Map<String, Object>>) response.get("results");
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des agents", e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getAllEntites() {
        try {
            Map<String, Object> response = agentFeignClient.getAllEntites(0, 10000);
            if (response == null || response.get("results") == null) {
                log.warn("Aucune entité reçue de l'API externe.");
                return Collections.emptyList();
            }
            return (List<Map<String, Object>>) response.get("results");
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des entités", e);
            return Collections.emptyList();
        }
    }

    /**
     * 🔹 Récupère l'agent connecté avec TOUTES ses informations enrichies
     */
    public Map<String, Object> getConnectedAgentDetails() {
        String emailFromToken = keycloakUserService.getEmail();
        String usernameOrMatricule = keycloakUserService.getEmailOrUsername();

        if (emailFromToken == null && usernameOrMatricule == null) {
            log.warn("Aucun utilisateur connecté trouvé dans le JWT.");
            return Map.of("message", "Aucun utilisateur connecté");
        }

        List<Map<String, Object>> agents = getAllAgents();
        if (agents.isEmpty()) {
            return Map.of("message", "Aucun agent disponible");
        }

        Map<String, Object> agent = agents.stream()
                .filter(a -> {
                    String agentEmail = String.valueOf(a.getOrDefault("email", "")).toLowerCase();
                    String agentUsername = String.valueOf(a.getOrDefault("username", "")).toLowerCase();
                    String agentMatricule = String.valueOf(a.getOrDefault("matricule", "")).toLowerCase();

                    return (emailFromToken != null && emailFromToken.toLowerCase().equals(agentEmail))
                            || (usernameOrMatricule != null && (
                            usernameOrMatricule.toLowerCase().equals(agentUsername) ||
                                    usernameOrMatricule.toLowerCase().equals(agentMatricule)
                    ));
                })
                .findFirst()
                .orElse(null);

        if (agent == null) {
            return Map.of("message", "Aucun agent trouvé pour " +
                    (emailFromToken != null ? emailFromToken : usernameOrMatricule));
        }

        // ✅ Enrichir avec les informations d'entités
        return enrichAgentWithEntites(agent);
    }

    /**
     * 🔹 Enrichit un agent avec ses informations d'entités complètes
     */
    private Map<String, Object> enrichAgentWithEntites(Map<String, Object> agent) {
        Map<String, Object> enrichedAgent = new HashMap<>(agent);

        List<Map<String, Object>> entites = getAllEntites();
        if (entites.isEmpty()) {
            return enrichedAgent;
        }

        // Récupérer les entités liées (direction, établissement, équipe)
        Map<String, Object> direction = (Map<String, Object>) agent.get("direction");
        Map<String, Object> etablissement = (Map<String, Object>) agent.get("etablissement");
        Map<String, Object> equipe = (Map<String, Object>) agent.get("equipe");

        // Enrichir la direction
        if (direction != null && direction.get("id") != null) {
            enrichedAgent.put("directionDetails", findEntiteById(entites, direction.get("id")));
        }

        // Enrichir l'établissement
        if (etablissement != null && etablissement.get("id") != null) {
            enrichedAgent.put("etablissementDetails", findEntiteById(entites, etablissement.get("id")));
        }

        // Enrichir l'équipe
        if (equipe != null && equipe.get("id") != null) {
            enrichedAgent.put("equipeDetails", findEntiteById(entites, equipe.get("id")));
        }

        return enrichedAgent;
    }

    /**
     * 🔹 Trouve une entité par son ID
     */
    private Map<String, Object> findEntiteById(List<Map<String, Object>> entites, Object entiteId) {
        return entites.stream()
                .filter(e -> {
                    Object id = e.get("id");
                    return id != null && id.toString().equals(entiteId.toString());
                })
                .findFirst()
                .orElse(null);
    }

    public Map<String, Object> getAgentById(Long id) {
        try {
            Map<String, Object> agent = agentFeignClient.getAgentById(id);
            return enrichAgentWithEntites(agent);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'agent avec ID {}", id, e);
            return Map.of("message", "Agent introuvable pour l'ID " + id);
        }
    }

    /**
     * 🔹 Recherche des agents par matricules (pour les équipes)
     * @param matricules Liste des matricules à rechercher
     * @return Liste des agents trouvés avec leurs infos complètes
     */
    public List<Map<String, Object>> getAgentsByMatricules(List<String> matricules) {
        if (matricules == null || matricules.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> agents = getAllAgents();

        return matricules.stream()
                .map(matricule -> agents.stream()
                        .filter(agent -> {
                            String agentMatricule = String.valueOf(agent.getOrDefault("matricule", ""));
                            return agentMatricule.equals(matricule);
                        })
                        .findFirst()
                        .map(this::enrichAgentWithEntites)
                        .orElse(Map.of(
                                "matricule", matricule,
                                "fullName", "Agent inconnu",
                                "error", "Matricule non trouvé"
                        ))
                )
                .collect(Collectors.toList());
    }

    /**
     * 🔹 Valide qu'une équipe ne dépasse pas 3 membres et que tous existent
     * @param matricules Liste des matricules à valider
     * @return Map avec 'valid' (boolean) et 'errors' (liste des erreurs)
     */
    public Map<String, Object> validateEquipe(List<String> matricules) {
        List<String> errors = new ArrayList<>();

        if (matricules == null || matricules.isEmpty()) {
            return Map.of("valid", true);
        }

        if (matricules.size() > 3) {
            errors.add("Une équipe ne peut pas dépasser 3 membres (incluant le porteur)");
        }

        List<Map<String, Object>> agents = getAgentsByMatricules(matricules);
        agents.stream()
                .filter(agent -> agent.containsKey("error"))
                .forEach(agent -> errors.add("Matricule " + agent.get("matricule") + " non trouvé"));

        return Map.of(
                "valid", errors.isEmpty(),
                "errors", errors,
                "foundAgents", agents.stream()
                        .filter(agent -> !agent.containsKey("error"))
                        .collect(Collectors.toList())
        );
    }


    public Map<String, Object> getConnectedAgentWithEntites() {
        return getConnectedAgentDetails();
    }
}