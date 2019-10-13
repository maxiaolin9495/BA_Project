package com.example.services.validation;

import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenValidationService {
    Logger logger = LoggerFactory.getLogger(TokenValidationService.class);
    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return true;
        } catch (java.text.ParseException e){
            logger.info("failed to parse token");
            return false;
        } catch (RuntimeException e){
            logger.info("invalid token");
            return false;
        }
    }

}
