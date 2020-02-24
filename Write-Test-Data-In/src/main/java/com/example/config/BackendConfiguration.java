package com.example.config;

import com.example.elasticsearch.ElasticSearchRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

}
