package com.example.rest;

import com.example.data.CertificateResponse;
import com.example.service.certificate.CertificateManagementForLTCA;
import com.example.service.validation.TokenValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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



    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/requestLTC")
    public ResponseEntity<CertificateResponse> requestLTC(@RequestHeader(value = "Authroization") String token,
                                                            @RequestParam(value = "publicKeyLTC") String key,
                                                            @RequestParam(value = "vin") String id) throws Exception {
        log.info("receive LTC request");

        if(token == null || !tokenValidationService.validateToken(token)) return ResponseEntity.status(401).build();

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

}
