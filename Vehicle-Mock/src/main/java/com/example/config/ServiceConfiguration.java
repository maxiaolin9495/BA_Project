package com.example.config;

import com.example.services.certificate.CertificateService;
import com.example.services.token.TokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {
    @Bean
    public TokenService buildRequestTokenService(){
        return new TokenService();
    }

    @Bean
    CertificateService buildCertificateService(){
        return new CertificateService();
    }
}
