package com.example.config;

import com.example.services.data.DataService;
import com.example.services.token.RequestTokenService;
import com.example.services.validation.TokenValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {
    @Bean
    public RequestTokenService buildRequestTokenService(){
        return new RequestTokenService();
    }

    @Bean
    TokenValidationService buildTokenValidationService(){
        return new TokenValidationService();
    }

    @Bean
    DataService buildDataService(){
        return new DataService();
    }
}
