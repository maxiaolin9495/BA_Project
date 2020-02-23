package com.example.config;

import com.example.elasticsearch.ElasticSearchRepository;
import com.example.service.certificate.CertificateManagementForRootCA;
import com.example.service.validation.TokenValidationService;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final int ADDRESS_LENGTH = 2;
    private static final String HTTP_SCHEME = "http";
    private static final int TIME_OUT = 5 * 60 * 1000;

    @Value("${elasticsearch.ip}")
    String ipAddress;

    @Value("${spring.data.elasticsearch.client.reactive.username}")
    String userName;

    @Value("${spring.data.elasticsearch.client.reactive.password}")
    String password;

    @Bean
    public TokenValidationService buildTokenValidationService(){
        return new TokenValidationService();
    }

    @Bean
    public CertificateManagementForRootCA buildCertificateManagement(){
        return new CertificateManagementForRootCA();
    }

    private HttpHost makeHttpHost(String s) {
        assert (s != null && s != "");
        String[] address = s.split(":");
        if (address.length == ADDRESS_LENGTH) {
            String ip = address[0];
            int port = Integer.parseInt(address[1]);
            System.err.println(ip + "+" + port);
            return new HttpHost(ip, port, HTTP_SCHEME);
        } else {
            return null;
        }
    }

    @Bean
    public RestClientBuilder restClientBuilder() {
        System.err.println(ipAddress);
        final CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(userName, password));
        RestClientBuilder builder = RestClient.builder(
                new HttpHost(makeHttpHost(ipAddress)))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(
                            HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder
                                .setDefaultCredentialsProvider(credentialsProvider);
                    }
                });

        builder.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                .setConnectionRequestTimeout(10000)
                .setConnectTimeout(10000)
                .setSocketTimeout(10000));

        builder.setFailureListener(new RestClient.FailureListener() {
            Logger log = LoggerFactory.getLogger(RestClient.class);
            @Override
            public void onFailure(Node node) {
                log.error("elasticsearch server occur error.");
                super.onFailure(node);
            }
        });
        return builder;
    }

    @Bean(name = "highLevelClient")
    public RestHighLevelClient highLevelClient(@Autowired RestClientBuilder restClientBuilder) {
        restClientBuilder.setRequestConfigCallback(
                new RestClientBuilder.RequestConfigCallback() {
                    @Override
                    public RequestConfig.Builder customizeRequestConfig(
                            RequestConfig.Builder requestConfigBuilder) {
                        return requestConfigBuilder.setSocketTimeout(TIME_OUT);
                    }
                });
        return new RestHighLevelClient(restClientBuilder);
    }

    @Bean
    public ElasticSearchRepository elasticSearchRepositoryBuilder(){
        return new ElasticSearchRepository();
    }

    @Value("${http.connect.timeout:1000}")
    private int connectTimeout;

    @Value("${http.read.timeout:1000}")
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
}
