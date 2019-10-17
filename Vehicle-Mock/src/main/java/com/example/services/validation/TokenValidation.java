package com.example.services.validation;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

public class TokenValidation {

    private static final String BACKEND_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCJpG8N3aaSiw/wchkDXKgG3ZoyXxCYpsAW6MjRCNOFFcW0uQSptWK+8/H02ye11ZCNMz3+2420MlkKF4LAsY7EaNmp0DfbPcSEaUHOvfWk3OListT1+EKzVQTjzz8D+a/w1yJWb57JO7GxHrxdUi12HQi2RL/Ywm/VvmuuPNhOFQIDAQAB";
    private String validIssuer = "BACKEND_A";
    Logger log = LoggerFactory.getLogger(TokenValidation.class);

    public boolean validateToken(String token) {
        try {
            log.info("Validation starts");
            SignedJWT signedJWT = SignedJWT.parse(token);
            return validateSignature(signedJWT) &&
                    validateIssuer(signedJWT.getJWTClaimsSet().getIssuer()) &&
                    validateExpiresAt(signedJWT.getJWTClaimsSet().getExpirationTime());
        } catch (java.text.ParseException e){
            log.info("failed to parse token");
            return false;
        } catch (RuntimeException e){
            log.info("invalid token");
            return false;
        }
    }

    private boolean validateSignature(SignedJWT signedJWT) {
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

    private boolean validateIssuer(String issuer){
        return this.validIssuer.equals(issuer);
    }

    private boolean validateExpiresAt(Date expirationTime){
        if(expirationTime == null){
            log.info("missing exp field, invalid token");
            return false;
        }
        if(System.currentTimeMillis() > expirationTime.getTime()){
            log.info("token expired");
            return false;
        }
        return true;
    }

}
