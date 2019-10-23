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
    private static final String BACKEND_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCkH2MFGJESGuis5KjcMXAvkjRi+UVBywqJmGtjHd58bgae86wk4JwkuzriUFMaHCiDwAVNiDKxIFvvl1Lh7lEqsO3DowUxQh3BLcXfgmaE8c9cIb6vm9XKbJDpiaKOPx9zwDURJ6XdcCcTDWp6Gta2XMmMYCN2AF63/fHfBNh1cwIDAQAB";
    private static final String BACKEND_PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKQfYwUYkRIa6KzkqNwxcC+SNGL5RUHLComYa2Md3nxuBp7zrCTgnCS7OuJQUxocKIPABU2IMrEgW++XUuHuUSqw7cOjBTFCHcEtxd+CZoTxz1whvq+b1cpskOmJoo4/H3PANREnpd1wJxMNanoa1rZcyYxgI3YAXrf98d8E2HVzAgMBAAECgYARYTF/LKbQIAFbraskBig3IWhiwrrOyM2I3Jcim9sfmhchZfRow1BFo34M0Sy7qxevO/pTx4R2tVKHAKNKQY5ep3DES2i6DcO1MSKzRQpOQKVZfPrCIpM9W9KKzOFfigKxErsgchcV79p/bCZjbEI0pB4AsmKU/o3RhfQOXkj3oQJBANvk6OV8dB0umQ/QU8SnAq+YijMfPpzN30hUhNXPyxPhC06/8WgNa1xFfHJ0785KEs6ycTpnwbLfjmYxXokHtFcCQQC/EijvhBxetYDDg8UAFnEudQHymPtZbbsTH6v9l5RFRyWyqtzc+a4reSY4xXRdKZlvmOufMZ637KRc9/gPibZFAkBxitF3LbwHFXiTYc7fTB1m7izuGMQL04Hnpzyv6ovByAI+t32/bK5zBq4rq5XnvvfXIuy0a77ozeaJfhSCTSghAkBEi6hB3NwsoeKSwXfwAx0RdIqBVJ5/Q51kS+1wqWtYuyBsC132rF/uWqT6ouOO9HiGD48f6jYtd2izrYXBVT8dAkEAte+uFeElkSEjQFTalr7CB2aeZDcOviojEiuquyf2ALJMwTmTki1x3ei9MIjx4ecjzCtGVDgX4OQlIJsTK7NbEg==";

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
