package com.example.config;

import com.example.services.authentication.TokenGenerationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BackendConfiguration {

    @Bean
    public TokenGenerationService buildTokenGenerationService(){
        return new TokenGenerationService();
    }
}
