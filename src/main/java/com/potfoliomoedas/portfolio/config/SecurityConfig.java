package com.potfoliomoedas.portfolio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "security.config")
@Data
public class SecurityConfig {
    private String PREFIX;
    private String KEY;
    private Long EXPIRATION;

}