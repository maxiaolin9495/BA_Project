package com.example.services.token;

import com.example.data.TokenResponse;
import com.example.services.validation.TokenValidation;
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
    private String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCKzWqRyINTGgehOMCVmVbp3pdyYBDPOCjCNuZe5kI1QjA+5f8m1GIqeirOAUuwgoSUUkOz6Q2GWc1Pq5WXkXVmDTpENllsSB7DFo374c0aycYlnrHa8zISceDnPrtXPJgdxarc/N4gAsD+YbFk/5qn4jiMSJGR1SDrxaDRw4NYswIDAQAB";
    private String validShortTermKey="13-10-2019";
    private String maskedCLientId="vehicleA";

    @Value("${backend.token.endpoint}")
    private String tokenEndpoint;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    TokenValidation tokenValidation;

    public String requestToken() throws Exception{
        try{
            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            map.add("STK", encryptShortTermKey());
            map.add("vehicle_id", maskedCLientId);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<TokenResponse> response = restTemplate.exchange(tokenEndpoint, HttpMethod.POST, request, TokenResponse.class);
            String token = response.getBody().getToken();
            if(tokenValidation.validateToken(token)) return token;
            else return null;
        } catch(HttpClientErrorException | HttpServerErrorException e) {
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
