package com.example.service.certificate;

import com.example.data.CertificateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class CertificateManagementForRootCA extends CertificateManagement {

    PublicKey PUBLIC_KEY;
    PrivateKey PRIVATE_KEY;

    @Value("${backend.ca.id}")
    String caId;

    @Value("${rootCAs.id}")
    private String[] caIds;

    @Value("${rootCAs.url}")
    private String[] urls ;

    @Autowired
    RestTemplate restTemplate;

    private Map<String, String> caEndpoints;

    private Map<String, X509Certificate> certificateStore;

    Logger logger = LoggerFactory.getLogger(CertificateManagement.class);


    public void start(){
        certificateStore = new HashMap<>();
        caEndpoints = new HashMap<>();
        for(int i = 0; i< caIds.length; i++) {
            caEndpoints.put(caIds[i], urls[1]);
            certificateStore.put(caIds[i], null);
        }
    }

    @Override
    public void createRootCertificate() {
        if(certificateStore == null) {
            start();
        }
        try {
            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
            keyGen.generate(1024);
            PUBLIC_KEY = keyGen.getPublicKey();
            PRIVATE_KEY = keyGen.getPrivateKey();
            //PUBLIC_KEY = new String(Base64.getEncoder().encode(keyGen.getPublicKey().getEncoded()));
            //PRIVATE_KEY = new String(Base64.getEncoder().encode(keyGen.getPrivateKey().getEncoded()));

            X509Certificate rootCertificate = keyGen.getSelfCertificate(new X500Name("cn=" + caId), (long) 365 * 24 * 60 * 60);
            rootCertificate  = super.createSelfSignedCertificate(rootCertificate,rootCertificate, PRIVATE_KEY );
            this.certificateStore.put(this.caId, rootCertificate);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update root certificate");
        }
    }

    public X509Certificate createIntermediateCertificate(String publicKey, String caId){
        if(certificateStore == null) {
            start();
            createRootCertificate();
        }
        try {
            X509Certificate intermediateCertificate = super.signCertificate(publicKey, caId, certificateStore.get(this.caId), PRIVATE_KEY);
            return intermediateCertificate;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update root certificate");
        }
    }
    public void updateRootCertificate(String caId){
        if(certificateStore == null) {
            start();
            createRootCertificate();
        }
        try {
            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            map.add("rootCAId", this.caId);
            map.add("id", caId);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<CertificateResponse> response = restTemplate.exchange(caEndpoints.get(caId), HttpMethod.POST, request, CertificateResponse.class);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
                    Base64.getDecoder().decode(
                            response.getBody().getCertificate()
                    )));
            this.certificateStore.put(caId, certificate);
        } catch(HttpClientErrorException | HttpServerErrorException e) {
            logger.info("Backend rejects request");
            throw new RuntimeException("Somethings went wrong during request Token " + e.getMessage());
        } catch(CertificateException e){
            logger.info("received a wrong certificate.");
        }
    }

    public X509Certificate getCertificate(String caId) {
        if(certificateStore == null) {
            start();
            createRootCertificate();
        }
        X509Certificate certificate = this.certificateStore.get(caId);
        if(!caId.equals(this.caId) && certificate == null){
            updateRootCertificate(caId);
            certificate = this.certificateStore.get(caId);
        }else if(certificate == null){
            createRootCertificate();
            certificate = this.certificateStore.get(caId);
        }
        return certificate;
    }

    public String getCaId() {
        return caId;
    }

    public boolean isCAIdValid(String rootCAId) {

        for (String s : caIds) {
            if (s.equals(rootCAId)) {
                return true;
            }
        }
        return false;


    }
}
