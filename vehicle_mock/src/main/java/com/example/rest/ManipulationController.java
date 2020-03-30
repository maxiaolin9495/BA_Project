package com.example.rest;

import com.example.data.CertificateResponse;
import com.example.data.CertificateUpdateNotification;
import com.example.services.certificate.CertificateService;
import com.example.services.token.TokenService;
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
import java.util.Date;

@RestController
public class ManipulationController {

    private int rootNumber = 1;

    private int ltcNumber = 1;

    private int tokenNumber = 1;

    Logger log = LoggerFactory.getLogger(ManipulationController.class);
    @Value("${rootCAs.id}")
    private String[] caIds;

    @Autowired
    CertificateService certificateService;

    ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(method = RequestMethod.GET, path = "/manipulation")
    public ResponseEntity<CertificateResponse> manipulation()throws Exception {
        log.info("0. receive start up request");

        certificateService.start();
        for(String caId: caIds) {
            log.info("0. Update root certificate of root CA "  + caId);
            certificateService.requestRootCertificate(caId, "0");
        }
        log.info("0. apply LT certificate");
        certificateService.requestLTC("0");
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/updateToken")
    public ResponseEntity<CertificateResponse> requestToken()throws Exception {
        certificateService.requestToken("" + tokenNumber++);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/updateLTC")
    public ResponseEntity<CertificateResponse> requestLTC()throws Exception {
        certificateService.requestLTC("" + ltcNumber++);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/requestRootCertificate")
    public ResponseEntity<CertificateResponse> requestRootCertificate()throws Exception {

        certificateService.requestRootCertificate(caIds[1], "" + rootNumber++);
        return ResponseEntity.ok().build();
    }


    @RabbitListener(queues = "#{autoDeleteQueue1.name}")
    public void receiveDirectQueue(Message message) throws InterruptedException {
        certificateService.clearToken();
        try {
            Date d1 = new Date(System.currentTimeMillis());
            CertificateUpdateNotification certificateUpdateNotification = objectMapper.readValue(message.getBody(), CertificateUpdateNotification.class);
            String rootCAId = certificateUpdateNotification.getRootCAId();
            String requestNumber = "" + certificateUpdateNotification.getRequestNumber();
            log.info(requestNumber + ". receive an root certificate update message");
            if(certificateService.isSelfRootCA(rootCAId)){
                log.info(requestNumber + ". update root certificate of root CA "  + rootCAId);
                certificateService.requestRootCertificate(rootCAId, requestNumber);
                log.info(requestNumber + ". apply LTC");
                certificateService.requestLTC(requestNumber);

            } else if(certificateService.isCAIdValid(rootCAId)){
                log.info(requestNumber + ". request root certificate of root CA from own root CA"  + rootCAId);
                certificateService.requestRootCertificate(rootCAId, requestNumber);
            }
            Date d2 = new Date(System.currentTimeMillis());
            log.info(requestNumber + ". time used to update Certificates is " + (d2.getTime() - d1.getTime()) + " ms");
            return;
        } catch (IOException e) {
            log.error("Failed to holds events from processing services" + e.getMessage());
        }
    }



}
