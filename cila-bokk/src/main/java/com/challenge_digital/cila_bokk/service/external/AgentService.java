package com.challenge_digital.cila_bokk.service.external;

import com.challenge_digital.cila_bokk.config.KeycloakUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentService {

    private final AgentApiClient agentFeignClient;
    private final KeycloakUserService keycloakUserService;

    /**
     * 🔹 Récupère tous les agents depuis l'API externe.
     * 🔹 Retourne une liste vide si aucun agent n'est trouvé.
     */
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
     * 🔹 Trouve l'agent correspondant à l'utilisateur Keycloak connecté
     * Recherche par email, puis username ou matricule
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

        return agents.stream()
                .filter(agent -> {
                    // Conversion sécurisée en String pour éviter ClassCastException
                    String agentEmail = String.valueOf(agent.getOrDefault("email", "")).toLowerCase();
                    String agentUsername = String.valueOf(agent.getOrDefault("username", "")).toLowerCase();
                    String agentMatricule = String.valueOf(agent.getOrDefault("matricule", "")).toLowerCase();

                    return (emailFromToken != null && emailFromToken.toLowerCase().equals(agentEmail))
                            || (usernameOrMatricule != null && (
                            usernameOrMatricule.toLowerCase().equals(agentUsername) ||
                                    usernameOrMatricule.toLowerCase().equals(agentMatricule)
                    ));
                })
                .findFirst()
                .orElse(Map.of("message", "Aucun agent trouvé pour " + (emailFromToken != null ? emailFromToken : usernameOrMatricule)));
    }

    public Map<String, Object> getAgentById(Long id) {
        try {
            return agentFeignClient.getAgentById(id);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'agent avec ID {}", id, e);
            return Map.of("message", "Agent introuvable pour l'ID " + id);
        }
    }





    public Map<String, Object> getConnectedAgentWithEntites() {
        // Récupération de l'agent connecté
        Map<String, Object> agent = getConnectedAgentDetails();

        if (agent.containsKey("message")) {
            return agent; // Aucun agent trouvé
        }

        // Récupération de toutes les entités
        List<Map<String, Object>> entites = getAllEntites();
        if (entites.isEmpty()) {
            return Map.of("agent", agent, "entites", Collections.emptyList());
        }

        // Exemple : filtrer les entités liées à l'agent
        // Suppose que l'agent a un champ "entiteId" ou "entitesIds"
        Object agentEntiteId = agent.get("entiteId"); // peut être Integer ou String
        List<Map<String, Object>> agentEntites = entites.stream()
                .filter(entite -> {
                    Object entiteId = entite.get("id");
                    return entiteId != null && entiteId.toString().equals(agentEntiteId.toString());
                })
                .toList();

        return Map.of(
                "agent", agent,
                "entites", agentEntites
        );
    }

}
