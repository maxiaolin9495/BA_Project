package com.example.rest;

import com.example.data.CA;
import com.example.data.CertificateResponse;
import com.example.elasticsearch.ElasticSearchRepository;
import com.example.service.certificate.CertificateManagementForRootCA;
import com.example.service.validation.TokenValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Autowired
    CertificateManagementForRootCA certificateManagement;

    @Autowired
    TokenValidationService tokenValidationService;

    @Autowired
    ElasticSearchRepository elasticSearchRepository;


    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            path = "/requestLTCACCertificate")
    public ResponseEntity requestLTCAC(
            @RequestParam(value = "publicKeyLTCAC") String key,
            @RequestParam(value = "LTCA_id") String id) throws Exception {
        log.info("receive LTCAC request");

        CA ca = elasticSearchRepository.findCA(index, id);
        if(ca.getRevoked()) return ResponseEntity.status(401).build();

        X509Certificate certificate = certificateManagement.createIntermediateCertificate(key, id);
        CertificateResponse certificateResponse= new CertificateResponse();
        certificateResponse.setCaId(certificateManagement.getCaId());
        certificateResponse.setCertificate(new String(Base64.getEncoder().encode(certificate.getEncoded())));
        return ResponseEntity.ok(certificateResponse);
    }



    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE, path = "/requestCertificate")
    public ResponseEntity requestCertificate(@RequestHeader(value = "Authroization") String token,
                                             @RequestParam(value = "id") String id) throws Exception {
        log.info("receive LTCAC request");

        if(token == null && !Arrays.asList(caIds).contains(id)) return ResponseEntity.status(400).build();
        else if(!tokenValidationService.validateToken(token)) return ResponseEntity.status(401).build();

        X509Certificate certificate = certificateManagement.getCertificate(id);
        CertificateResponse certificateResponse= new CertificateResponse();
        certificateResponse.setCaId(id);
        certificateResponse.setCertificate(new String(Base64.getEncoder().encode(certificate.getEncoded())));
        return ResponseEntity.ok(certificateResponse);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, path = "/inform")
    public ResponseEntity inform(@RequestParam(value = "id") String id) {
        log.info("receive RCA update info");

        certificateManagement.updateRootCertificate(id);
        return ResponseEntity.ok().build();
    }

}
