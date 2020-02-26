package com.example.config;

import com.example.service.certificate.CertificateManagementForLTCA;
import com.example.service.validation.TokenValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class BackendConfiguration {

    @Value("${http.connect.timeout:1000}")
    private int connectTimeout;

    @Value("${http.read.timeout:1000}")
    private int readTimeout;

    @Bean
    public TokenValidationService buildTokenValidationService(){
        return new TokenValidationService();
    }

    @Bean
    public CertificateManagementForLTCA buildCertificateManagement(){
        CertificateManagementForLTCA certificateManagement =  new CertificateManagementForLTCA();
        return certificateManagement;
    }


    @Bean
    public HttpComponentsClientHttpRequestFactory buildHttpComponentsClientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }

    @Bean @Qualifier("restTemplate")
    public RestTemplate buildRestTemplate(@Autowired HttpComponentsClientHttpRequestFactory factory) {
        RestTemplate template = new RestTemplate();
        template.setRequestFactory(factory);

        List<HttpMessageConverter<?>> convs = new ArrayList<>();
        convs.add(new MappingJackson2HttpMessageConverter());
        convs.add(new FormHttpMessageConverter());
        convs.add(new ByteArrayHttpMessageConverter());
        convs.add(new Jaxb2RootElementHttpMessageConverter());
        convs.add(new StringHttpMessageConverter());
        convs.add(new ResourceHttpMessageConverter());
        convs.add(new SourceHttpMessageConverter());
        convs.add(new AllEncompassingFormHttpMessageConverter());
        template.setMessageConverters(convs);

        return template;
    }
}
