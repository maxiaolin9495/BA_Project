package com.example.rest;

import com.example.data.CertificateResponse;
import com.example.services.certificate.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.cert.X509Certificate;
import java.util.Base64;

@RestController
public class ManipulationController {

    @Autowired
    CertificateService certificateService;

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/manipulation")
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

}
