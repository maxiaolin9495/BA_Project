package com.example.services.authentication;


import com.example.data.TokenResponse;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;

import java.security.spec.PKCS8EncodedKeySpec;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TokenGenerationService {
    private static final String privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAI4yhd56IeXctaCumKwGFBFQh6NUEKY2dKTqHbhKIPBo1dOo2qSa0FA7azkG4+xFvqbuP2R/rKTXzcqobtDpDdoKhTkDDPuzcfMeLhboZqs9i7RXdR1p7nhxQTJ7gllFYoe2QZqpHCuxIFiK6XU+Rj72LoOGQtut1jlQFd/e2V5/AgMBAAECgYAxKEYfIxmU3Tfs1G7zCSbm1XAOhoE69JheapDbpMS+V3+ULEtpnBtnoSLwE/G3PGUsUDCaDYkhc3kxFgk5L1gaA+DwjeE9fOZFpHlnvGihm2XU2vYhL4+q1wbH8sNkpPYZ8ArusSTPhnYWkpiQyIuUTEC4r09YDod81cLFnM24AQJBANQazNOEQfgkLCtVvpxoe7i6KSy6b231NhuGD/xE8dNjTBk26kHMiNKHkNpCx7LpEFlGojUIXQzgVgtTpktHICcCQQCroBFC28LKzzHa/c+75ftnztQyj1VVGA6Nk7vhdkUAKHc0fVMo5gv2ZdSb3q9W+chgpUsUZFrbLiqIBHJZf+3pAkEA0TRx7PrZmqgLDN8w99KynXy42g41lC41pSYxJDDYmRiq2X/pxOa52XWvcShHEneBEZ1ypn5OUUIAfIGQV1WbJwJAYiNGm4eUjGoMJSBLgDNTHhn75ullLyJxd+2JWrRJYnzBJUegnNtm7b9u4q5kbwSn2vPJyeBUv85XTa3VQW5QuQJBAMxbFziN5euq/4zUDB7jBZGbugx5nCPYq2i37VUQKNfVEyYfPNB8uOW81gztFRjywObVHlxl1OZfiGE1Ywcov58=";
    private static final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCOMoXeeiHl3LWgrpisBhQRUIejVBCmNnSk6h24SiDwaNXTqNqkmtBQO2s5BuPsRb6m7j9kf6yk183KqG7Q6Q3aCoU5Awz7s3HzHi4W6GarPYu0V3Udae54cUEye4JZRWKHtkGaqRwrsSBYiul1PkY+9i6DhkLbrdY5UBXf3tlefwIDAQAB";
    private static final String BACKEND_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCNPx8Kce+duv6B/jvfrAx4t0q44BipygZEKiRAGwfg6RsOpDhTI7j1Zt2czBxAVcUtcM6owJrAS0lRARnUrjDnUr3HsXsY0cq2AftibWyb9fBvlGj7tujkEnWMHv7Ksxt0Km1sxve+rE51fk3NiuAmNms0xqTJtSAnAhLKO2v33wIDAQAB";
    private static final String BACKEND_PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAI0/Hwpx7526/oH+O9+sDHi3SrjgGKnKBkQqJEAbB+DpGw6kOFMjuPVm3ZzMHEBVxS1wzqjAmsBLSVEBGdSuMOdSvcexexjRyrYB+2JtbJv18G+UaPu26OQSdYwe/sqzG3QqbWzG976sTnV+Tc2K4CY2azTGpMm1ICcCEso7a/ffAgMBAAECgYBTEQIiVGtE6cnvjSKNIlObUsAhBbm7cNJKmDYFdWAmxzBgZfex/VozCDHgmrEqrMZvON5wpATefeg6OVmFmIOtQx5JW4E3QHd31k3Qrl1Gdc2SfSAsTM0YD1jug3t/fwJ6Q62eHOQd9pBm7JIwb+CfY08qc6+hoJp7OPYWxdW90QJBAOd250wd9nF5wK/2RtdWbL5u9DY4uoTyyxRUYAVN8jlHOIlnHioc4kPvvEo/Rjk0vltPzg2U/xFcNVcguTZNLP0CQQCcOAjw1La1tzRvaCzC9gSUpCNyybRSq8sXVh/Brs5+/ywcrMJ1/0EH82daVZ51rQ7Mizq05zZ+HgMZr/Pmyf0LAkEAujqfpE1jMC+uiaTToIQHoDxzPoDsCPWz0RqF2x64qb8UzPBrg6STo+dqDi7UmEhIc/1EowbjJhuTSxOoEPmNHQJAWQVNgv+URqTpynqQ12CeZ53JXYY3rcyb5OZ9Hj/hCFmc55JDFj6o7kD0KnQT4ncFseW9RI1lbiToG2E7Gc8M1QJBAMRpWW7oEQkX6FzhvdJCqv+f11yN5SAVZUuiOyoMsTER41zD6vkwKxQhscPgL+LLMVXKu60GAInp0OANa+pGGfA=";

    Logger log = LoggerFactory.getLogger(TokenGenerationService.class);
    private String validShortTermKey="13-10-2019";
    private String issuer = "BACKEND_B";
    private List<String> audience = new ArrayList<>();


    public TokenResponse generateToken(String encryptedShortTermKey, String maskedVehicleId) throws Exception{
        byte[] inputByte = Base64.getDecoder().decode(encryptedShortTermKey);
        byte[] encPriKey = Base64.getDecoder().decode(privateKey);

        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encPriKey));
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);

        log.info("ShortTermKey is decrypted");
        String shortTermKey = new String(cipher.doFinal(inputByte));

        if(shortTermKey.equals(validShortTermKey)){
            log.info("valid ShortTermKey");
            return buildToken(shortTermKey, maskedVehicleId);
        }
        log.info("invalid ShortTermKey");
        return null;
    }



    private TokenResponse buildToken(String shortTermKey, String maskedVehicleId) throws Exception{
        checkAudience();

        byte[] clientPrivateKey = Base64.getDecoder().decode(BACKEND_PRIVATE_KEY);
        JWSSigner jwsSigner = new RSASSASigner((RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(clientPrivateKey)));


        //set issuedTime and expiredTime, normally the token is valid in 24 hours
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + 1000*3600*24);

        //add necessary infos in Payload
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(maskedVehicleId)
                .issuer(issuer)
                .audience(audience)
                .issueTime(now)
                .expirationTime(exp)
                .claim("STK", shortTermKey)
                .build();


        //generate signature for token
        SignedJWT signedJWT =  new SignedJWT(new JWSHeader(JWSAlgorithm.RS512), claimsSet);
        signedJWT.sign(jwsSigner);

        log.info("token is generated successfully");
        return new TokenResponse(exp.toString(), signedJWT.serialize());
    }


    private void checkAudience(){
        if(audience.size() <= 1){
            audience = new ArrayList<>();
           audience.add(issuer);
           audience.add("vehicle");
        }
    }

}
