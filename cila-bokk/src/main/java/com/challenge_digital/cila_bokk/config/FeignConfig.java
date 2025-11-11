package com.challenge_digital.cila_bokk.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@Configuration
@EnableFeignClients(basePackages = "com.challenge_digital.cila_bokk.service.external")
public class FeignConfig {
}
