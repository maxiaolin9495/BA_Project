package com.example.rest;

import com.example.RabbitMQ.Sender;
import com.example.data.CA;
import com.example.data.CertificateResponse;
import com.example.data.CertificateUpdateNotification;
import com.example.elasticsearch.ElasticSearchRepository;
import com.example.service.certificate.CertificateManagementForRootCA;
import com.example.service.validation.TokenValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;

@Controller
@RequestMapping(value="/v1")
public class CACertificateController {
    Logger log = LoggerFactory.getLogger(CACertificateController.class);

    @Value("${elasticsearch.index}")
    String index;

    @Value("${rootCAs.id}")
    private String[] caIds;

    @Value("${rabbit.mq.routing_key_ltca}")
    String ltcaRoutingKey;

    @Value("${rabbit.mq.routing_key_vehicle}")
    String vehicleRoutingKey;

    @Value("${rabbit.mq.routing_key_other_rca}")
    String otherRCARoutingKey;

    @Autowired
    CertificateManagementForRootCA certificateManagement;

    @Autowired
    TokenValidationService tokenValidationService;

    @Autowired
    ElasticSearchRepository elasticSearchRepository;

    @Autowired
    Sender sender;

    ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "#{autoDeleteQueue1.name}")
    public void receiveDirectQueue(Message message) throws InterruptedException {

        //sleep for a better logging output
        Thread.sleep(3000);

        try {
            CertificateUpdateNotification certificateUpdateNotification = objectMapper.readValue(message.getBody(), CertificateUpdateNotification.class);
            log.info("Receive a certificate update notification from root CA " + certificateUpdateNotification.getRootCAId());
            if (certificateManagement.isCAIdValid(certificateUpdateNotification.getRootCAId())){
                certificateManagement.updateRootCertificate(certificateUpdateNotification.getRootCAId());
            }
            //provide better logging
            Thread.sleep(2000);
            sender.send(certificateUpdateNotification, vehicleRoutingKey);
        } catch (IOException e) {
            log.error("Failed to read the notification" + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("Failed to let thread sleep " + e.getMessage());
        }

    }


    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            path = "/requestLTCACertificate")
    public ResponseEntity requestLTCAC(
            @RequestParam(value = "publicKeyLTCAC") String key,
            @RequestParam(value = "LTCA_id") String id) throws Exception {
        log.info(id + " applies long-term CA certificate");

        CA ca = elasticSearchRepository.findCA(index, id);
        if(ca == null || ca.getRevoked()) return ResponseEntity.status(401).build();

        X509Certificate certificate = certificateManagement.createIntermediateCertificate(key, id);
        CertificateResponse certificateResponse= new CertificateResponse();
        certificateResponse.setCaId(certificateManagement.getCaId());
        certificateResponse.setCertificate(new String(Base64.getEncoder().encode(certificate.getEncoded())));
        return ResponseEntity.ok(certificateResponse);
    }



    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE, path = "/requestCertificate")
    public ResponseEntity requestCertificate(@RequestHeader(value = "Authorization") String token,
                                             @RequestParam(value = "id") String id) throws Exception {
        log.info("Vehicle requests root certificate of " + id);

        if(token == null) return ResponseEntity.status(401).build();
        if(!tokenValidationService.validateToken(token)) return ResponseEntity.status(401).build();

        X509Certificate certificate = certificateManagement.getCertificate(id);
        CertificateResponse certificateResponse= new CertificateResponse();
        certificateResponse.setCaId(id);
        certificateResponse.setCertificate(new String(Base64.getEncoder().encode(certificate.getEncoded())));
        log.info("Request with valid token, send certificate of " + id + " back");
        return ResponseEntity.ok(certificateResponse);
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE, path = "/requestCertificateForRoot")
    public ResponseEntity requestCertificateForRoot(@RequestParam(value = "rootCAId") String rootCAId,
                                             @RequestParam(value = "id") String id) throws Exception {
        log.info("Receive RCAC request, special for root CA " + rootCAId + ", sent by " + id);
        if(!Arrays.asList(caIds).contains(rootCAId)) return ResponseEntity.status(400).build();

        X509Certificate certificate = certificateManagement.getCertificate(id);
        CertificateResponse certificateResponse= new CertificateResponse();
        certificateResponse.setCaId(id);
        certificateResponse.setCertificate(new String(Base64.getEncoder().encode(certificate.getEncoded())));
        return ResponseEntity.ok(certificateResponse);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/manipulation")
    public ResponseEntity updateRootCertificate() throws Exception {
        log.info("Receive manipulation request, update own root certificate");

        certificateManagement.createRootCertificate();
        CertificateUpdateNotification certificateUpdateNotification = new CertificateUpdateNotification();
        certificateUpdateNotification.setRootCAId(certificateManagement.getCaId());

        log.info("Inform ltca A");
        sender.send(certificateUpdateNotification, ltcaRoutingKey);

        log.info("Inform vehicle A1");
        sender.send(certificateUpdateNotification, vehicleRoutingKey);

        log.info("Inform " + otherRCARoutingKey);
        sender.send(certificateUpdateNotification, otherRCARoutingKey);

        return ResponseEntity.ok().build();
    }


}