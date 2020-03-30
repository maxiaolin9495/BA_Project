package com.example.rest;

import com.example.data.PublicKeyResponse;
import com.example.data.TokenResponse;
import com.example.data.Vehicle;
import com.example.elasticsearch.ElasticSearchRepository;
import com.example.services.authentication.TokenGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
@RequestMapping(value="/v1")
public class BackendTokenController {

    @Value("${elasticsearch.index}")
    String index;

    Logger log = LoggerFactory.getLogger(BackendTokenController.class);

    @Autowired
    TokenGenerationService tokenGenerationService;

    @Autowired
    ElasticSearchRepository elasticSearchRepository;

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/requestToken")
    public ResponseEntity requestToken(@RequestParam(value = "password") String password, @RequestParam(value = "vin") String vin, @RequestParam(value = "audience") String aud, @RequestParam(value = "requestNumber") String requestNumber) throws Exception {
        Date d1 = new Date(System.currentTimeMillis());
        if (aud == null || aud.length() == 0){
            log.info("invalid audience value");
            return ResponseEntity.status(400).body("invalid audience value");
        }
        String[] auds = aud.split(" ");
        Vehicle v = elasticSearchRepository.findVehicle(index, vin);
        if (v == null){
            log.info("invalid vin id " + vin + " in index " + index);
            return ResponseEntity.status(400).body("invalid vin id");
        }

        if (v.getPassword() == null || !v.getPassword().equals(password)){
            log.info("incorrect password");
            return ResponseEntity.status(400).body("incorrect password");
        }

        List<String> storedAudience = Arrays.asList(v.getAudience());
        Set<String> audience = new HashSet<>();

        for (String s : auds)
            if (storedAudience.contains(s))
                audience.add(s);

        TokenResponse tokenResponse = tokenGenerationService.generateToken(vin, audience);

        Date d2 = new Date(System.currentTimeMillis());

        log.info(requestNumber + ". time used to generate a new Token is " + (d2.getTime() - d1.getTime()) + " ms" );
        return ResponseEntity.ok(tokenResponse);

    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/publicKey")
    public ResponseEntity<PublicKeyResponse> requestPublicKey() throws Exception {

        String publicKey = tokenGenerationService.getBackendPublicKey();
        String azsId = tokenGenerationService.getIssuer();
        PublicKeyResponse response = new PublicKeyResponse();
        response.setAzsId(azsId);
        response.setPublicKey(publicKey);
        return ResponseEntity.ok(response);
    }

}
