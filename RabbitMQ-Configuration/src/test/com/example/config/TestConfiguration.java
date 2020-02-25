package com.example.config;

import com.example.services.authentication.TokenGenerationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class TestConfiguration {
    @Bean
    public TokenGenerationService buildTokenGenerationService(){
        return new TokenGenerationService();
    }
}
