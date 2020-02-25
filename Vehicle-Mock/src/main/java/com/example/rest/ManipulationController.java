package com.example.rest;

import com.example.data.CertificateResponse;
import com.example.data.CertificateUpdateNotifyication;
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

import java.io.IOException;

@RestController
public class ManipulationController {

    Logger log = LoggerFactory.getLogger(ManipulationController.class);
    @Value("${rootCAs.id}")
    private String[] caIds;

    @Autowired
    CertificateService certificateService;

    @RequestMapping(method = RequestMethod.GET, path = "/manipulation")
    public ResponseEntity<CertificateResponse> manipulation()throws Exception {
        log.info("receive start up request");

        certificateService.start();
        for(String caId: caIds) {
            log.info("update root certificate of root CA "  + caId);
            certificateService.requestRootCertificate(caId);
        }
        certificateService.requestLTC();
        return ResponseEntity.ok().build();
    }

    @RabbitListener(queues = "#{autoDeleteQueue1.name}")
    public void receiveDirectQueue(Message message) {


        ObjectMapper objectMapper = new ObjectMapper();
        try {
            CertificateUpdateNotifyication certificateUpdateNotifyication = objectMapper.readValue(message.getBody(), CertificateUpdateNotifyication.class);

            String rootCAId = certificateUpdateNotifyication.getRootCAId();
            log.info("receive certificate update notification, root CA " + rootCAId + "has updated its certificate" );

            if(certificateService.isSelfRootCA(rootCAId)){
                log.info("The root CA in own certificate chain");
                log.info("Need update root certificate and request new LTC");
                log.info("Update own root certificate");
                certificateService.requestRootCertificate(rootCAId);
                log.info("Update own LTC");
                certificateService.requestLTC();
                log.info("Update Successfully");
                return;
            }
            if(certificateService.isCAIdValid(rootCAId)){
                certificateService.requestRootCertificate(rootCAId);
                return;
            }

        } catch (IOException e) {
            log.error("Failed to holds events from processing services" + e.getMessage());
        }
    }


}
