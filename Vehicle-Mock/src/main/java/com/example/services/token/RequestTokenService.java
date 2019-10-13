package com.example.services.token;

import com.example.data.TokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class RequestTokenService {
    private String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCKzWqRyINTGgehOMCVmVbp3pdyYBDPOCjCNuZe5kI1QjA+5f8m1GIqeirOAUuwgoSUUkOz6Q2GWc1Pq5WXkXVmDTpENllsSB7DFo374c0aycYlnrHa8zISceDnPrtXPJgdxarc/N4gAsD+YbFk/5qn4jiMSJGR1SDrxaDRw4NYswIDAQAB";
    private String validShortTermKey="13-10-2019";

    @Value("${backend.token.endpoint:localhost:8080/v1/requestToken}")
    private String tokenEndpoint;

    @Autowired
    RestTemplate restTemplate;

    public String requestToken() throws Exception{
        try{
            Map<String, String> params = new HashMap<String, String>();
            params.put("STK", encryptShortTermKey());
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(tokenEndpoint, params, TokenResponse.class);
            return response.getBody().getToken();
        } catch(HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException();
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
