package com.challenge_digital.cila_bokk.service.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentApiClient agentApiClient;

    /**
     * Récupère un agent par son ID
     */
    public AgentApiDto getAgentById(Long agentId) {
        try {
            return agentApiClient.getAgentById(agentId);
        } catch (Exception e) {
            System.err.println("❌ Erreur API externe pour l'agent " + agentId + ": " + e.getMessage());
            // Essayer de récupérer depuis la liste complète
            AgentPageResponse allAgents = getAllAgents();
            if (allAgents != null && allAgents.getContent() != null) {
                return allAgents.getContent().stream()
                        .filter(a -> a.getId().equals(agentId))
                        .findFirst()
                        .orElse(null);
            }
            return null;
        }
    }

    /**
     * ✅ NOUVEAU : Récupère un agent par son MATRICULE
     */
    public AgentApiDto getAgentByMatricule(String matricule) {
        if (matricule == null || matricule.trim().isEmpty()) {
            return null;
        }

        try {
            System.out.println("🔍 Recherche agent avec matricule: " + matricule);
            AgentPageResponse allAgents = getAllAgents();

            if (allAgents != null && allAgents.getContent() != null) {
                AgentApiDto found = allAgents.getContent().stream()
                        .filter(a -> a.getMatricule() != null &&
                                matricule.trim().equals(a.getMatricule().toString()))
                        .findFirst()
                        .orElse(null);

                if (found != null) {
                    System.out.println("✅ Agent trouvé: " + found.getFullName() + " (ID: " + found.getId() + ")");
                } else {
                    System.out.println("⚠️ Aucun agent trouvé avec le matricule: " + matricule);
                }

                return found;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la recherche par matricule " + matricule + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Récupère tous les agents
     */
    public AgentPageResponse getAllAgents() {
        try {
            return agentApiClient.getAllAgents(0, 20000);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des agents: " + e.getMessage());
            return null;
        }
    }

    public boolean agentExists(Long agentId) {
        AgentApiDto agent = getAgentById(agentId);
        return agent != null && Boolean.TRUE.equals(agent.getActive());
    }

    /**
     * ✅ NOUVEAU : Vérifie qu'un agent existe par son matricule
     */
    public boolean agentExistsByMatricule(String matricule) {
        AgentApiDto agent = getAgentByMatricule(matricule);
        return agent != null && Boolean.TRUE.equals(agent.getActive());
    }

    public String getAgentEmail(Long agentId) {
        AgentApiDto agent = getAgentById(agentId);
        return agent != null ? agent.getEmail() : null;
    }

    public String getAgentFullName(Long agentId) {
        AgentApiDto agent = getAgentById(agentId);
        return agent != null ? agent.getFullName() : null;
    }
}