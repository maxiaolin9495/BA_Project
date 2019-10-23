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
    private static final String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIrNapHIg1MaB6E4wJWZVunel3JgEM84KMI25l7mQjVCMD7l/ybUYip6Ks4BS7CChJRSQ7PpDYZZzU+rlZeRdWYNOkQ2WWxIHsMWjfvhzRrJxiWesdrzMhJx4Oc+u1c8mB3Fqtz83iACwP5hsWT/mqfiOIxIkZHVIOvFoNHDg1izAgMBAAECgYAbbMzysxCnmcwKIeGDbYHLwcxgr/SCTRFYlmpcM9i/Fy3bL6yDapFe3TaZ742Z+Z/igoKYCKNAeXvkkv2Crh3o2DbLs8fZ7TiwMi01HT7Rf5ctpWFnadRiC48Y2Baxv8m28HwYoEn461+rhwNIYPUF5Lwv/ZG/kccpBAx9qCP5SQJBAPJgb8q2lDgnzYHKEsWDoHqnGYqNH6IPA1UoPt/7CdM3/4mheUOSrbC4Igg5VPfJSWVwGdoRjCVAfKLTaffV0S0CQQCSmqWdIQBVB0fs3yDA85o/xDhA15gA2wUmOlH97XCw+QYNUd0aaYg9BwEbofJ9V32mR0Gf6P9Zqmg7bPQ/fz1fAkBpNMDxrcSyh1xlzO/O+i6LbsLgaBdmAbxBl4GLOW1vWGw8MnHvidiIz7Q9+5zNHXsVY85k4J8DgHVZPAlQQWhVAkB9RbpY7zc6cRNL3Eo/tqlK7d/nwJI5wO3AgbtIUVmnT9OVFCvsAtuQ0mhm9VahNl6+9EqmW1G/i/avOOXpUE/fAkEA73l8gLdJdcJkXrTaXuQbWgXbgtUmGi2TNoobyqNpi6gcjbhq7gaPQ9Tst40QjryouU6bGvgKH4W3JA5Owa/aQg==";
    private static final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCKzWqRyINTGgehOMCVmVbp3pdyYBDPOCjCNuZe5kI1QjA+5f8m1GIqeirOAUuwgoSUUkOz6Q2GWc1Pq5WXkXVmDTpENllsSB7DFo374c0aycYlnrHa8zISceDnPrtXPJgdxarc/N4gAsD+YbFk/5qn4jiMSJGR1SDrxaDRw4NYswIDAQAB";
    private static final String BACKEND_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCJpG8N3aaSiw/wchkDXKgG3ZoyXxCYpsAW6MjRCNOFFcW0uQSptWK+8/H02ye11ZCNMz3+2420MlkKF4LAsY7EaNmp0DfbPcSEaUHOvfWk3OListT1+EKzVQTjzz8D+a/w1yJWb57JO7GxHrxdUi12HQi2RL/Ywm/VvmuuPNhOFQIDAQAB";
    private static final String BACKEND_PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAImkbw3dppKLD/ByGQNcqAbdmjJfEJimwBboyNEI04UVxbS5BKm1Yr7z8fTbJ7XVkI0zPf7bjbQyWQoXgsCxjsRo2anQN9s9xIRpQc699aTc4uKy1PX4QrNVBOPPPwP5r/DXIlZvnsk7sbEevF1SLXYdCLZEv9jCb9W+a6482E4VAgMBAAECgYAkP2yLn0KdtweUj8esjfsmC7fu6gAp40gVAGraOdPSaYBuboce1kjR9tZSYGCdz0dJGCSMeG9h9BxkvvpNGrV4+ysZd8vgnWSrdJK2DbSLfnKey6FBBcGWTTOTGknuOROVtLV6Gvu/y0Aogw75LUOtXyv1shkf3FHgIyV0qklNQQJBAL/mbC9sOouPGKrc0smx4UtEEqxS+EX0obkwPtx3ndR8bu6QV6HqnW3VI9cu19ktSZqiqYKB2tVfwoXbCXX+yOUCQQC3nl9jom5KRU8gqNmYWf7Tfca4tXOHR9pcCVN5PPTd5w/ttu6OA4PpRfju3C0rUsDxoAxXs1XKCBLKXPBtlg1xAkEAr3zsmkGDGHT3vejK6p+8w7Owz93gJTkdW9j+42aP8u0IFSnyuaNv7Czp5bJ0uapskPkHws01bcNwhDBWao1g8QJAX+3mygfvdO/24go0/EpwPo/khVWPEOsyIolgyeNVzqTMBpZe1WTywqmi3y+6jXrDgLjsGdmAhXdoUb7ygV42kQJAbPjXJfadGE/DO1Vs7HaWkslwtZT9u5NIfwlP65ody4LWBVFDaUYkiqzkgtai1qx59XvhZidKbv6Kn12wdvCTdg==";

    Logger log = LoggerFactory.getLogger(TokenGenerationService.class);
    private String validShortTermKey="13-10-2019";
    private String issuer = "BACKEND_A";
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
