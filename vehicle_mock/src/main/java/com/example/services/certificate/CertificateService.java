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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import sun.security.tools.keytool.CertAndKeyGen;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

public class CertificateService {

    private static PublicKey PUBLIC_KEY;
    private static PrivateKey PRIVATE_KEY;

    Logger logger = LoggerFactory.getLogger(CertificateService.class);

    @Autowired
    RestTemplate restTemplate;

    @Value("${rootca.self.id}")
    private String rootCASelfId;

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

    public void start() {
        rootCertificateStorage = new HashMap<>();
        for (int i = 0; i < caIds.length; i++) {
            rootCertificateStorage.put(caIds[i], null);
        }
    }

    public void requestRootCertificate(String rcaId, String requestNumber) {
        int repeat = 0;
        Date d1 = new Date(System.currentTimeMillis());
        try {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            if (token == null) {
                requestToken(requestNumber);
            }
            if (token == null) {
                return;
            }
            headers.add("Authorization", token);

            map.add("id", rcaId);
            map.add("requestNumber", requestNumber);


            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<CertificateResponse> response = null;
            while (response == null && repeat < 3) {
                try {
                    repeat++;
                    response = restTemplate.exchange(rootCAEndpoint, HttpMethod.POST, request, CertificateResponse.class);
                } catch (ResourceAccessException e) {
                    if (repeat >= 3) {
                        throw e;
                    }
                } catch (HttpClientErrorException | HttpServerErrorException e) {
                    if (e.getStatusCode().value() == 401) {
                        logger.info("Failed, because token expired");
                        token = null;
                    }
                    return;
                } catch (Exception e) {
                    throw e;
                }
            }

            Date d2 = new Date(System.currentTimeMillis());
            logger.info(requestNumber + ". time used to request Root Certificate is " + (d2.getTime() - d1.getTime()) + " ms");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            rootCertificateStorage.put(rcaId, (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
                    Base64.getDecoder().decode(
                            response.getBody().getCertificate()
                    ))));
        } catch (ResourceAccessException e) {
            logger.error("Failed to take Root certificate due to connection error");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update root certificate");
        }

    }

    public void requestLTC(String requestNumber) {
        int repeat = 0;
        try {
            Date d1 = new Date(System.currentTimeMillis());
            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);

            keyGen.generate(1024);
            PUBLIC_KEY = keyGen.getPublicKey();
            PRIVATE_KEY = keyGen.getPrivateKey();
            Date d2 = new Date(System.currentTimeMillis());
            logger.info(requestNumber + ". time used to generate new LTC key pair is " + (d2.getTime() - d1.getTime()) + " ms");


            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            if (token == null) {
               requestToken(requestNumber);
            }

            headers.add("Authorization", token);

            map.add("publicKeyLTC", new String(Base64.getEncoder().encode(PUBLIC_KEY.getEncoded())));
            map.add("vin", vin);
            map.add("requestNumber", requestNumber);
            logger.info("Send LT certificate request");
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            ResponseEntity<CertificateResponse> response = null;
            while (response == null && repeat < 3) {
                try {
                    repeat++;
                    response = restTemplate.exchange(ltCAEndpoint, HttpMethod.POST, request, CertificateResponse.class);
                } catch (ResourceAccessException e) {
                    if (repeat >= 3) {
                        throw e;
                    }
                } catch (HttpClientErrorException | HttpServerErrorException e) {
                    if (e.getStatusCode().value() == 401) {
                        logger.info("Failed, because token expired");
                        token = null;
                    }else throw e;
                    return;
                } catch (Exception e) {
                    throw e;
                }
            }

            Date d3 = new Date(System.currentTimeMillis());
            logger.info(requestNumber + ". time used to request a new LTC is " + (d3.getTime() - d1.getTime()) + " ms");

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            LTC = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
                    Base64.getDecoder().decode(
                            response.getBody().getCertificate()
                    )));

        } catch (ResourceAccessException e) {
            logger.error("Failed to take LT certificate due to connection error");
        } catch (Exception e) {
            logger.info("Failed to take LT certificate");
            throw new RuntimeException("Failed to update LTC certificate");
        }

    }


    public boolean isSelfRootCA(String rootCAId) {
        return this.rootCASelfId.equals(rootCAId);
    }

    public boolean isCAIdValid(String rootCAId) {
        for (String s : caIds) {
            if (s.equals(rootCAId)) {
                return true;
            }
        }
        return false;
    }

    public void clearToken() {
        this.token = null;
    }

    public void requestToken(String requestNumber) {
        int repeat = 0;

        while (token == null && repeat < 3) {
            try {
                repeat++;
                this.token = tokenService.requestToken(password, vin, audience, requestNumber);
            } catch (ResourceAccessException e) {
                if (repeat >= 3) {
                    throw e;
                }
            } catch (Exception e) {
                logger.error("Failed to update token");
            }
        }

    }
}