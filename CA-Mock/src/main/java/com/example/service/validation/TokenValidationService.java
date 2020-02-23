package com.example.service.validation;

import com.example.data.PublicKeyResponse;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class TokenValidationService {

    private String BACKEND_PUBLIC_KEY;

    @Value("${backend.ca.id}")
    String clientId;

    @Value("${backend.azs.id}")
    String validIssuer;

    @Value("${backend.azs.public_key_endpoint}")
    String azsPublicKeyEndpoint;

    @Autowired
    RestTemplate restTemplate;

    Logger log = LoggerFactory.getLogger(TokenValidationService.class);

    public boolean validateToken(String token) {
        try {
            log.info("Validation starts");
            SignedJWT signedJWT = SignedJWT.parse(token);
            return validateSignature(signedJWT) &&
                    validateIssuer(signedJWT.getJWTClaimsSet().getIssuer()) &&
                    validateExpiresAt(signedJWT.getJWTClaimsSet().getExpirationTime())
                    && validateAudience(signedJWT.getJWTClaimsSet().getAudience());
        } catch (java.text.ParseException e){
            log.info("failed to parse token");
            return false;
        } catch (RuntimeException e){
            log.info("invalid token");
            return false;
        }
    }

    private boolean validateSignature(SignedJWT signedJWT) {
        if(BACKEND_PUBLIC_KEY == null) {
            requestPublicKey();
        }
        byte[] decBackendPubKey = Base64.getDecoder().decode(BACKEND_PUBLIC_KEY);
        RSAPublicKey publicKey = null;
        try {
            publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec((decBackendPubKey)));
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            return signedJWT.verify(verifier);
        } catch (InvalidKeySpecException e) {
            log.info("invalid public key");
            return false;
        } catch (NoSuchAlgorithmException e) {
            log.info("Algorithm not found");
            return false;
        } catch (JOSEException e) {
            log.info("invalid signature");
            return false;
        }
    }

    private void requestPublicKey() {

        try {
            ResponseEntity<PublicKeyResponse> response = restTemplate.getForEntity(azsPublicKeyEndpoint, PublicKeyResponse.class);
            PublicKeyResponse publicKeyResponse = response.getBody();
            if(!publicKeyResponse.getAzsId().equals(validIssuer)) return;
            BACKEND_PUBLIC_KEY = response.getBody().getPublicKey();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.info("Backend rejects request");
            throw new RuntimeException("Somethings went wrong during request Token " + e.getMessage());
        }
    }
    private boolean validateIssuer(String issuer){
        return this.validIssuer.equals(issuer);
    }

    private boolean validateExpiresAt(Date expirationTime){
        if(expirationTime == null){
            log.info("missing expiration time, invalid token");
            return false;
        }
        if(System.currentTimeMillis() > expirationTime.getTime()){
            log.info("token expired");
            return false;
        }
        return true;
    }

    private boolean validateAudience(List<String> audiences){
        if(audiences == null){
            log.info("missing audience field, invalid token");
            return false;
        }
        if(!audiences.contains(clientId)){
            log.info("invalid audiences value");
            return false;
        }
        return true;
    }

}
