package com.example.service.certificate;

import com.example.data.CertificateResponse;
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

public class CertificateManagementForLTCA extends CertificateManagement {

    PublicKey PUBLIC_KEY;
    PrivateKey PRIVATE_KEY;

    @Value("${backend.ca.id}")
    String caId;

    @Value("${root.ca.endpoint}")
    String rootCAEndpoint;

    X509Certificate longTermCACertificate;

    Logger logger = LoggerFactory.getLogger(CertificateManagement.class);

    @Autowired
    RestTemplate restTemplate;



    public void requestIntermediateCertificate(){
        try {
            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
            keyGen.generate(1024);
            PUBLIC_KEY = keyGen.getPublicKey();
            PRIVATE_KEY = keyGen.getPrivateKey();

            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);


            map.add("publicKeyLTCAC", new String(Base64.getEncoder().encode(PUBLIC_KEY.getEncoded())));
            map.add("LTCA_id", caId);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<CertificateResponse> response = restTemplate.exchange(rootCAEndpoint, HttpMethod.POST, request, CertificateResponse.class);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            longTermCACertificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
                    Base64.getDecoder().decode(
                            response.getBody().getCertificate()
                    )));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update root certificate");
        }

    }

    public X509Certificate createLTC(String publicKey, String vin){
        if(longTermCACertificate == null) requestIntermediateCertificate();
        try {
            X509Certificate intermediateCertificate = signCertificate(publicKey, vin, longTermCACertificate, PRIVATE_KEY);
            return intermediateCertificate;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create long-term certificate");
        }

    }

    public X509Certificate getCertificate() {
        if(longTermCACertificate == null) requestIntermediateCertificate();
        return longTermCACertificate;
    }

    public String getCaId() {
        return caId;
    }
}
