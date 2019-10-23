package com.example.services.token;

import com.example.data.TokenResponse;
import com.example.services.validation.TokenValidationService;

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

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


public class RequestTokenService {
    private String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCOMoXeeiHl3LWgrpisBhQRUIejVBCmNnSk6h24SiDwaNXTqNqkmtBQO2s5BuPsRb6m7j9kf6yk183KqG7Q6Q3aCoU5Awz7s3HzHi4W6GarPYu0V3Udae54cUEye4JZRWKHtkGaqRwrsSBYiul1PkY+9i6DhkLbrdY5UBXf3tlefwIDAQAB";
    private String validShortTermKey="13-10-2019";
    private String maskedVehicleId="vehicleB";

    Logger log = LoggerFactory.getLogger(RequestTokenService.class);
    @Value("${backend.token.endpoint}")
    private String tokenEndpoint;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    TokenValidationService tokenValidationService;

    public String requestToken() throws Exception{
        try{
            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            log.info("encypt the short term key");
            map.add("STK", encryptShortTermKey());
            map.add("vehicle_id", maskedVehicleId);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<TokenResponse> response = restTemplate.exchange(tokenEndpoint, HttpMethod.POST, request, TokenResponse.class);
            String token = response.getBody().getToken();
            if(tokenValidationService.validateToken(token)) return token;
            else return null;
        } catch(HttpClientErrorException | HttpServerErrorException e) {
            log.info("Backend rejects request");
            throw new RuntimeException("Somethings went wrong during request Token " + e.getMessage());
        }
    }

    public String encryptShortTermKey() throws Exception{
        byte[] decoded = Base64.getDecoder().decode(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        String outStr = Base64.getEncoder().encodeToString(cipher.doFinal(validShortTermKey.getBytes("UTF-8")));
        return outStr;
    }

}
