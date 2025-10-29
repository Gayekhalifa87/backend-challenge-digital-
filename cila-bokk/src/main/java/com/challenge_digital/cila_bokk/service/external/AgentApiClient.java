package com.challenge_digital.cila_bokk.service.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "agent-api", url = "http://10.106.136.126:9003")
public interface AgentApiClient {

    @GetMapping("/api/v1/agent2/agent")
    AgentPageResponse getAllAgents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20000") int size
    );

    @GetMapping("/api/v1/agent2/agent/{id}")
    AgentApiDto getAgentById(@PathVariable Long id);
}