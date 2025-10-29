package com.challenge_digital.cila_bokk.service.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentApiClient agentApiClient;

    public AgentApiDto getAgentById(Long agentId) {
        try {
            return agentApiClient.getAgentById(agentId);
        } catch (Exception e) {
            System.err.println("❌ Erreur API externe pour l'agent " + agentId + ": " + e.getMessage());
            return null;
        }
    }

    public AgentPageResponse getAllAgents() {
        try {
            return agentApiClient.getAllAgents(0, 20000);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des agents: " + e.getMessage());
            return null;
        }
    }

    public boolean agentExists(Long agentId) {
        try {
            AgentApiDto agent = agentApiClient.getAgentById(agentId);
            return agent != null && agent.getActive() != null && agent.getActive();
        } catch (Exception e) {
            return false;
        }
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