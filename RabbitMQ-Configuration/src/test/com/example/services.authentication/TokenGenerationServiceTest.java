package com.example.services.authentication;

import com.example.data.TokenResponse;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ActiveProfiles({"test"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TokenGenerationServiceTest {

    private static final String VALID_VIN = "validVehicle";
    private static final String VALID_AUDIENCE = "rcaA ltcaA";

    @Value("${public_key}")
    protected String BACKEND_PUBLIC_KEY;
    @Value("${private_key}")
    protected String BACKEND_PRIVATE_KEY;
    @Value("${token.issuer}")
    protected String issuer;

    protected TokenGenerationService tokenGenerationService = new TokenGenerationService();

    @Before
    public void setUp() throws Exception {

        setField(tokenGenerationService, "BACKEND_PUBLIC_KEY", BACKEND_PUBLIC_KEY);
        setField(tokenGenerationService, "BACKEND_PRIVATE_KEY", BACKEND_PRIVATE_KEY);
        setField(tokenGenerationService, "issuer", issuer);
    }

    @Test
    public void generateTokenWithValidAudience() {
        try {
            Set<String> audience = new HashSet<>();
            audience.add("rcaA");
            audience.add("ltcaA");

            TokenResponse tokenResponse = tokenGenerationService.generateToken(VALID_VIN, audience);

            SignedJWT signedJWT = SignedJWT.parse(tokenResponse.getToken());
            if(! (validateSignature(signedJWT) &&
                    validateIssuer(signedJWT.getJWTClaimsSet().getIssuer()) &&
                    validateExpiresAt(signedJWT.getJWTClaimsSet().getExpirationTime()) &&
                    validateAudience(signedJWT.getJWTClaimsSet().getAudience())))
                fail();

        } catch (Exception e) {
            fail();

        }
    }



    @Test
    public void generateTokenWithInvalidAudience() {
        try {
            TokenResponse tokenResponse = tokenGenerationService.generateToken(VALID_VIN, new HashSet<>());
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(e.getMessage(), "Request with invalid audience group");
        }
    }

    private boolean validateSignature(SignedJWT signedJWT) {

        String BACKEND_PUBLIC_KEY = tokenGenerationService.getBackendPublicKey();
        byte[] decBackendPubKey = Base64.getDecoder().decode(BACKEND_PUBLIC_KEY);
        RSAPublicKey publicKey = null;
        try {
            publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec((decBackendPubKey)));
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            return signedJWT.verify(verifier);
        } catch (InvalidKeySpecException e) {
            return false;
        } catch (NoSuchAlgorithmException e) {
            return false;
        } catch (JOSEException e) {
            return false;
        }
    }

    private boolean validateIssuer(String issuer){
        return this.issuer.equals(issuer);
    }

    private boolean validateExpiresAt(Date expirationTime){
        if(expirationTime == null){
            return false;
        }
        if(System.currentTimeMillis() > expirationTime.getTime()){
            return false;
        }
        return true;
    }

    private boolean validateAudience(List<String> audiences){
        if(audiences == null){
            return false;
        }
        for (String s: VALID_AUDIENCE.split(" "))
            if(!audiences.contains(s)) {
                return false;
            }
        return true;
    }


}
