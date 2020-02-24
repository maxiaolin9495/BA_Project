package com.example.rest;

import com.example.data.CertificateResponse;
import com.example.services.certificate.CertificateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ManipulationController {

    Logger log = LoggerFactory.getLogger(ManipulationController.class);
    @Value("${rootCAs.id}")
    private String[] caIds;

    @Autowired
    CertificateService certificateService;

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/manipulation")
    public ResponseEntity<CertificateResponse> requestLTC()throws Exception {
        log.info("receive LTC request");

        certificateService.start();
        for(String caId: caIds)
            certificateService.requestRootCertificate(caId);
        certificateService.requestLTC();
        return ResponseEntity.ok().build();
    }

}
