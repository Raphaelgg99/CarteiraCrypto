package com.potfoliomoedas.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApiConfig {

    @Bean // Agora o Spring gerencia o RestTemplate
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
