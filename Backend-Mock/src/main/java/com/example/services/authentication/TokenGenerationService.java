package com.example.services.authentication;


import com.example.data.TokenResponse;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class TokenGenerationService {
    private String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIrNapHIg1MaB6E4wJWZVunel3JgEM84KMI25l7mQjVCMD7l/ybUYip6Ks4BS7CChJRSQ7PpDYZZzU+rlZeRdWYNOkQ2WWxIHsMWjfvhzRrJxiWesdrzMhJx4Oc+u1c8mB3Fqtz83iACwP5hsWT/mqfiOIxIkZHVIOvFoNHDg1izAgMBAAECgYAbbMzysxCnmcwKIeGDbYHLwcxgr/SCTRFYlmpcM9i/Fy3bL6yDapFe3TaZ742Z+Z/igoKYCKNAeXvkkv2Crh3o2DbLs8fZ7TiwMi01HT7Rf5ctpWFnadRiC48Y2Baxv8m28HwYoEn461+rhwNIYPUF5Lwv/ZG/kccpBAx9qCP5SQJBAPJgb8q2lDgnzYHKEsWDoHqnGYqNH6IPA1UoPt/7CdM3/4mheUOSrbC4Igg5VPfJSWVwGdoRjCVAfKLTaffV0S0CQQCSmqWdIQBVB0fs3yDA85o/xDhA15gA2wUmOlH97XCw+QYNUd0aaYg9BwEbofJ9V32mR0Gf6P9Zqmg7bPQ/fz1fAkBpNMDxrcSyh1xlzO/O+i6LbsLgaBdmAbxBl4GLOW1vWGw8MnHvidiIz7Q9+5zNHXsVY85k4J8DgHVZPAlQQWhVAkB9RbpY7zc6cRNL3Eo/tqlK7d/nwJI5wO3AgbtIUVmnT9OVFCvsAtuQ0mhm9VahNl6+9EqmW1G/i/avOOXpUE/fAkEA73l8gLdJdcJkXrTaXuQbWgXbgtUmGi2TNoobyqNpi6gcjbhq7gaPQ9Tst40QjryouU6bGvgKH4W3JA5Owa/aQg==";
    private String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCKzWqRyINTGgehOMCVmVbp3pdyYBDPOCjCNuZe5kI1QjA+5f8m1GIqeirOAUuwgoSUUkOz6Q2GWc1Pq5WXkXVmDTpENllsSB7DFo374c0aycYlnrHa8zISceDnPrtXPJgdxarc/N4gAsD+YbFk/5qn4jiMSJGR1SDrxaDRw4NYswIDAQAB";
    private String validShortTermKey="13-10-2019";


    public TokenResponse generateToken(String encryptedShortTermKey) throws Exception{
        byte[] inputByte = Base64.getDecoder().decode(encryptedShortTermKey);

        byte[] encPriKey = Base64.getDecoder().decode(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encPriKey));
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        String outStr = new String(cipher.doFinal(inputByte));
        if(outStr.equals(validShortTermKey)){
            return new TokenResponse("1234", "1234");
        }
        return null;
    }



}
