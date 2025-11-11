package com.challenge_digital.cila_bokk.controller;

import com.challenge_digital.cila_bokk.service.external.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    /**
     * 🔹 Récupère directement l'agent connecté à partir du JWT Keycloak
     * 🔹 Retourne un message clair si aucun agent n'est trouvé
     */
    @GetMapping("/api/user")
    public Map<String, Object> getConnectedAgent() {
        return agentService.getConnectedAgentDetails();
    }


    @GetMapping("/api/user-connecte")
    public Map<String, Object> getConnectedAgentWithEntites() {
        return agentService.getConnectedAgentWithEntites();
    }

}
