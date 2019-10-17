package com.example.config;

import com.example.services.token.RequestTokenService;
import com.example.services.validation.TokenValidation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {
    @Bean
    public RequestTokenService buildRequestTokenService(){
        return new RequestTokenService();
    }

    @Bean TokenValidation buildTokenValidation(){
        return new TokenValidation();
    }
}
