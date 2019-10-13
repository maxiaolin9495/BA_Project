package com.example;

import com.example.config.EnableCustomServiceTemplateLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableCustomServiceTemplateLibrary
public class VehicleApplication extends SpringBootServletInitializer {

    @Value("${http.connect.timeout:3000}")
    private int connectTimeout;

    @Value("${http.read.timeout:3000}")
    private int readTimeout;

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

    public static void main(String[] args) {
        SpringApplication.run(VehicleApplication.class, args);
    }
}
