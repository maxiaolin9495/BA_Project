package com.example.services.token;

import com.example.data.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;


public class TokenService {




    Logger logger = LoggerFactory.getLogger(TokenService.class);
    @Value("${backend.token.endpoint}")
    private String tokenEndpoint;

    @Autowired
    RestTemplate restTemplate;


    public String requestToken(String password, String vin, String audience, String requestNumber) throws Exception{
        try{
            Date d1 = new Date(System.currentTimeMillis());
            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            map.add("password", password);
            map.add("vin", vin);
            map.add("audience", audience);
            map.add("requestNumber", requestNumber);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<TokenResponse> response = restTemplate.exchange(tokenEndpoint, HttpMethod.POST, request, TokenResponse.class);
            String token = response.getBody().getToken();
            Date d2 = new Date(System.currentTimeMillis());
            logger.info(requestNumber + ". time used to request Token is " + (d2.getTime()-d1.getTime()) + " ms");
            return token;
        } catch(HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Backend rejects request");
            throw new RuntimeException("Somethings went wrong during request Token " + e.getMessage());
        }
    }


}
