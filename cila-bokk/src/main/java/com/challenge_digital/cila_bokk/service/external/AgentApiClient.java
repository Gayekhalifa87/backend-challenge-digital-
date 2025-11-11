package com.challenge_digital.cila_bokk.service.external;

import com.challenge_digital.cila_bokk.config.FeignAuthInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        name = "agent-api",
        url = "http://10.106.136.126:9003/api/v1/agent2",
        configuration = FeignAuthInterceptor.class
)
public interface AgentApiClient {

    @GetMapping("/agent")
    Map<String, Object> getAllAgents(

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100000") int size
    );

    @GetMapping("/agent/{id}")
    Map<String, Object> getAgentById(@PathVariable("id") Long id);

    @GetMapping("/entite")
    Map<String, Object> getAllEntites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size
    );
}
