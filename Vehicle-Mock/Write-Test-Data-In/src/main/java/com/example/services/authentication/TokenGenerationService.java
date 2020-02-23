package com.example.services.authentication;


import com.example.data.TokenResponse;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

public class TokenGenerationService {
    @Value("${public_key}")
    private String BACKEND_PUBLIC_KEY;
    @Value("${private_key}")
    private String BACKEND_PRIVATE_KEY;

    @Value("${token.issuer}")
    String issuer;

    Logger log = LoggerFactory.getLogger(TokenGenerationService.class);
    private List<String> audience = new ArrayList<>();


    public TokenResponse generateToken(String vin, Set<String> audience) throws Exception{
        if (audience.size() == 0) {
            throw new RuntimeException("Request with invalid audience group");
        }

        return buildToken(vin, audience);
    }



    private TokenResponse buildToken(String vin, Set<String> audience) throws Exception{

        byte[] clientPrivateKey = Base64.getDecoder().decode(BACKEND_PRIVATE_KEY);
        JWSSigner jwsSigner = new RSASSASigner((RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(clientPrivateKey)));


        //set issuedTime and expiredTime, normally the token is valid in 24 hours
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + 1000*3600*24);

        String[] audienceArray = new String [audience.size()];
        audience.toArray(audienceArray);
        //add necessary infos in Payload
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(vin)
                .issuer(issuer)
                .audience(Arrays.asList(audienceArray))
                .issueTime(now)
                .expirationTime(exp)
                .build();


        //generate signature for token
        SignedJWT signedJWT =  new SignedJWT(new JWSHeader(JWSAlgorithm.RS512), claimsSet);
        signedJWT.sign(jwsSigner);

        log.info("token is generated successfully");
        return new TokenResponse(exp.toString(), signedJWT.serialize());
    }


    public String getBackendPublicKey() {
        return BACKEND_PUBLIC_KEY;
    }

    public String getIssuer() {
        return issuer;
    }
}
