package com.example.service.validation;

import com.example.data.PublicKeyResponse;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import sun.security.tools.keytool.CertAndKeyGen;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;


@RunWith(SpringJUnit4ClassRunner.class)
public class TokenValidationServiceTest {

    private static String VIN = "vehicleA";

    private String BACKEND_PUBLIC_KEY;
    private String BACKEND_PRIVATE_KEY;
    private String token;

    @Value("${backend.ca.id}")
    String clientId;

    @Value("${backend.azs.id}")
    String validIssuer;

    @Value("${backend.azs.public_key_endpoint}")
    String azsPublicKeyEndpoint;

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    TokenValidationService tokenValidationService;

    Logger log = LoggerFactory.getLogger(TokenValidationServiceTest.class);

    @Before
    public void setup(){
        try {
            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
            keyGen.generate(1024);
            BACKEND_PUBLIC_KEY = new String (Base64.getEncoder().encode(keyGen.getPublicKey().getEncoded()));
            BACKEND_PRIVATE_KEY = new String (Base64.getEncoder().encode(keyGen.getPrivateKey().getEncoded()));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        PublicKeyResponse publicKeyResponse = new PublicKeyResponse();
        publicKeyResponse.setPublicKey(BACKEND_PUBLIC_KEY);
        publicKeyResponse.setAzsId(validIssuer);

        when(restTemplate.getForEntity(azsPublicKeyEndpoint, PublicKeyResponse.class)).thenReturn(ResponseEntity.ok(publicKeyResponse));


        setField(tokenValidationService, "restTemplate", restTemplate);
        setField(tokenValidationService, "clientId", clientId);
        setField(tokenValidationService, "validIssuer", validIssuer);
        setField(tokenValidationService, "azsPublicKeyEndpoint", azsPublicKeyEndpoint);




        byte[] clientPrivateKey = Base64.getDecoder().decode(BACKEND_PRIVATE_KEY);
        try {
            JWSSigner jwsSigner = new RSASSASigner((RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(clientPrivateKey)));
            //set issuedTime and expiredTime, normally the token is valid in 24 hours
            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);
            Date exp = new Date(nowMillis + 1000*3600*24);

            String[] audienceArray = new String [2];
            audienceArray[0] =  clientId;
            audienceArray[1] = "rcaA";
            //add necessary infos in Payload
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(VIN)
                    .issuer(validIssuer)
                    .audience(Arrays.asList(audienceArray))
                    .issueTime(now)
                    .expirationTime(exp)
                    .build();


            //generate signature for token
            SignedJWT signedJWT =  new SignedJWT(new JWSHeader(JWSAlgorithm.RS512), claimsSet);
            signedJWT.sign(jwsSigner);
            token = signedJWT.serialize();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void validateToken() {
        try {
            assertTrue(tokenValidationService.validateToken(token));
        } catch (RuntimeException e){
            fail();
        }
    }



}
