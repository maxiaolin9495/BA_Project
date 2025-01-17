package com.example.rest;

import com.example.data.CertificateResponse;
import com.example.data.CertificateUpdateNotification;
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
import java.util.Date;

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

        Date d1 = new Date(System.currentTimeMillis());


        try {
            CertificateUpdateNotification certificateUpdateNotification = objectMapper.readValue(message.getBody(), CertificateUpdateNotification.class);
            String requestNumber = "" + certificateUpdateNotification.getRequestNumber();
            log.info(requestNumber + ". receive an update notification by root CA");
            if(certificateUpdateNotification.getRootCAId().equals(certificateManagement.getRootCAId())) {
                certificateManagement.requestIntermediateCertificate(requestNumber);
            }
        } catch (IOException e) {
            log.error("Failed to holds new LTC from root CA" + e.getMessage());
        }
    }


    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/requestLTC")
    public ResponseEntity<CertificateResponse> requestLTC(@RequestHeader(value = "Authorization") String token,
                                                            @RequestParam(value = "publicKeyLTC") String key,
                                                            @RequestParam(value = "vin") String id,
                                                            @RequestParam(value = "requestNumber") String requestNumber) throws Exception {
        log.info(requestNumber + ". receive LTC request with vin " + id);

        Date d1 = new Date(System.currentTimeMillis());
        if(token == null){
            log.info(requestNumber + ". request without token, invalid request");
            return ResponseEntity.status(401).build();
        }
        if(!tokenValidationService.validateToken(token)){
            log.info(requestNumber + ". invalid Token, reject the request");
            return ResponseEntity.status(401).build();
        }
        X509Certificate certificate = certificateManagement.createLTC(key, id, requestNumber);
        CertificateResponse certificateResponse= new CertificateResponse();
        certificateResponse.setCaId(certificateManagement.getCaId());
        certificateResponse.setCertificate(new String(Base64.getEncoder().encode(certificate.getEncoded())));

        Date d2 = new Date(System.currentTimeMillis());

        log.info(requestNumber + ". time used to generate a new LTC is " + (d2.getTime() - d1.getTime()) + " ms" );
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
