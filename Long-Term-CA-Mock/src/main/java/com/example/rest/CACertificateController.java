package com.example.rest;

import com.example.data.CertificateResponse;
import com.example.data.CertificateUpdateNotifyication;
import com.example.service.certificate.CertificateManagementForLTCA;
import com.example.service.validation.TokenValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Controller
@RequestMapping(value="/v1")
public class CACertificateController {
    Logger log = LoggerFactory.getLogger(CACertificateController.class);

    @Autowired
    CertificateManagementForLTCA certificateManagement;

    @Autowired
    TokenValidationService tokenValidationService;


    private ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "#{autoDeleteQueue1.name}")
    public void receiveDirectQueue(Message message) {

        log.info("Receive an update notification by root CA");

        try {
            CertificateUpdateNotifyication certificateUpdateNotifyication = objectMapper.readValue(message.getBody(), CertificateUpdateNotifyication.class);
            if(certificateUpdateNotifyication.equals(certificateManagement.getRootCAId())) {
                certificateManagement.requestIntermediateCertificate();
            }
        } catch (IOException e) {
            log.error("Failed to holds new LTC from root CA" + e.getMessage());
        }
    }


    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/requestLTC")
    public ResponseEntity<CertificateResponse> requestLTC(@RequestHeader(value = "Authorization") String token,
                                                            @RequestParam(value = "publicKeyLTC") String key,
                                                            @RequestParam(value = "vin") String id) throws Exception {
        log.info("Receive LTC request with vin " + id);

        if(token == null || !tokenValidationService.validateToken(token)){
            log.info("Invalid Token, reject the request");
            return ResponseEntity.status(401).build();
        }
        log.info("Valid token, start issuing certificate");
        X509Certificate certificate = certificateManagement.createLTC(key, id);
        CertificateResponse certificateResponse= new CertificateResponse();
        certificateResponse.setCaId(certificateManagement.getCaId());
        certificateResponse.setCertificate(new String(Base64.getEncoder().encode(certificate.getEncoded())));
        return ResponseEntity.ok(certificateResponse);
    }

//    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/requestCertificate")
//    public ResponseEntity requestCertificate(@RequestHeader(value = "Authroization") String token) throws Exception {
//        log.info("receive LTCAC request");
//
//        if(token == null || !tokenValidationService.validateToken(token)) return ResponseEntity.status(401).build();
//
//        X509Certificate certificate = certificateManagement.getCertificate();
//        CertificateResponse certificateResponse= new CertificateResponse();
//        certificateResponse.setCaId(certificateManagement.getCaId());
//        certificateResponse.setCertificate(new String(Base64.getEncoder().encode(certificate.getEncoded())));
//        return ResponseEntity.ok(certificateResponse);
//    }


    public CertificateManagementForLTCA getCertificateManagement() {
        return certificateManagement;
    }
}
