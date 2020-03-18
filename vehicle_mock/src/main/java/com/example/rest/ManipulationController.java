package com.example.rest;

import com.example.data.CertificateResponse;
import com.example.data.CertificateUpdateNotification;
import com.example.services.certificate.CertificateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.ws.Response;
import java.io.IOException;

@RestController
public class ManipulationController {

    Logger log = LoggerFactory.getLogger(ManipulationController.class);
    @Value("${rootCAs.id}")
    private String[] caIds;

    @Autowired
    CertificateService certificateService;

    ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(method = RequestMethod.GET, path = "/manipulation")
    public ResponseEntity<CertificateResponse> manipulation()throws Exception {
        log.info("receive start up request");

        certificateService.start();
        for(String caId: caIds) {
            log.info("Update root certificate of root CA "  + caId);
            certificateService.requestRootCertificate(caId);
        }
        log.info("apply LT certificate");
        certificateService.requestLTC();
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/clearToken")
    public ResponseEntity<CertificateResponse> clearToken()throws Exception {

       certificateService.clearToken();
       return ResponseEntity.ok().build();
    }

    @RabbitListener(queues = "#{autoDeleteQueue1.name}")
    public void receiveDirectQueue(Message message) throws InterruptedException {

        log.info("Receive an root certificate update message");

        try {
            CertificateUpdateNotification certificateUpdateNotification = objectMapper.readValue(message.getBody(), CertificateUpdateNotification.class);

            String rootCAId = certificateUpdateNotification.getRootCAId();
            log.info("receive certificate update notification, root CA " + rootCAId + " has updated its certificate" );

            if(certificateService.isSelfRootCA(rootCAId)){
                log.info("Update root certificate of root CA "  + rootCAId);
                certificateService.requestRootCertificate(rootCAId);
                log.info("apply LTC");
                certificateService.requestLTC();
                log.info("Update Successfully");
                return;
            } else if(certificateService.isCAIdValid(rootCAId)){
                log.info("Request root certificate of root CA from own root CA"  + rootCAId);
                certificateService.requestRootCertificate(rootCAId);
                return;
            }
        } catch (IOException e) {
            log.error("Failed to holds events from processing services" + e.getMessage());
        }
    }



}
