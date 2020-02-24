package com.example.services.certificate;

import com.example.data.CertificateResponse;
import com.example.services.token.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sun.security.tools.keytool.CertAndKeyGen;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class CertificateService {

    private static PublicKey PUBLIC_KEY;
    private static PrivateKey PRIVATE_KEY;

    Logger logger = LoggerFactory.getLogger(CertificateService.class);

    @Autowired
    RestTemplate restTemplate;

    @Value("${rootca.endpoint}")
    private String rootCAEndpoint;

    @Value("${ltca.endpoint}")
    private String ltCAEndpoint;

    @Value("${vehicle.vin}")
    private String vin;
    @Value("${vehicle.password}")
    private String password;

    @Value("${vehicle.audience}")
    private String audience;

    @Value("${rootCAs.id}")
    private String[] caIds;

    @Autowired
    TokenService tokenService;

    private String token;

    private Map<String, X509Certificate> rootCertificateStorage;

    private X509Certificate LTC;

    public void start(){
        rootCertificateStorage = new HashMap<>();
        for(int i = 0; i< caIds.length; i++) {
            rootCertificateStorage.put(caIds[i], null);
        }
    }

    public void requestRootCertificate(String rcaId){
        try {

            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            if(token == null) {
                token = tokenService.requestToken(password, vin, audience);
            }
            headers.add("Authorization", token);

            map.add("id", rcaId);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<CertificateResponse> response = restTemplate.exchange(rootCAEndpoint, HttpMethod.POST, request, CertificateResponse.class);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            rootCertificateStorage.put(rcaId ,(X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
                    Base64.getDecoder().decode(
                            response.getBody().getCertificate()
                    ))));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update root certificate");
        }

    }

    public void requestLTC(){
        try {
            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
            keyGen.generate(1024);
            PUBLIC_KEY = keyGen.getPublicKey();
            PRIVATE_KEY = keyGen.getPrivateKey();


            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            if(token == null) {
                token = tokenService.requestToken(password, vin, audience);
            }
            headers.add("Authorization", token);

            map.add("publicKeyLTC", new String(Base64.getEncoder().encode(PUBLIC_KEY.getEncoded())));
            map.add("vin", vin);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<CertificateResponse> response = restTemplate.exchange(ltCAEndpoint, HttpMethod.POST, request, CertificateResponse.class);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            LTC = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
                    Base64.getDecoder().decode(
                            response.getBody().getCertificate()
                    )));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update LTC certificate");
        }

    }





}
