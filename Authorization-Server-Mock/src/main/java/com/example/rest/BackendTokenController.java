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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public ResponseEntity requestToken(@RequestParam(value = "password") String password, @RequestParam(value = "vin") String vin, @RequestParam(value = "audience") String aud) throws Exception {
        log.info("received get Token request");
        if (aud == null || aud.length() == 0) return ResponseEntity.status(400).body("invalid audience value");
        String[] auds = aud.split(" ");
        Vehicle v = elasticSearchRepository.findVehicle(index, vin);
        if (v == null) return ResponseEntity.status(400).body("invalid vin id");

        if (v.getPassword() == null || !v.getPassword().equals(password))
            return ResponseEntity.status(400).body("incorrect password");

        if (v.getBlocked())
            return ResponseEntity.status(401).body("you are blocked by the backend, please reach our com.example.service station");
        List<String> storedAudience = Arrays.asList(v.getAudience());
        Set<String> audience = new HashSet<>();

        for (String s : auds)
            if (storedAudience.contains(s))
                audience.add(s);

        TokenResponse tokenResponse = tokenGenerationService.generateToken(vin, audience);
        return ResponseEntity.ok(tokenResponse);

    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/publicKey")
    public ResponseEntity<PublicKeyResponse> requestPublicKey() throws Exception {
        log.info("received get Public Key request");


        String publicKey = tokenGenerationService.getBackendPublicKey();
        String azsId = tokenGenerationService.getIssuer();
        PublicKeyResponse response = new PublicKeyResponse();
        response.setAzsId(azsId);
        response.setPublicKey(publicKey);
        return ResponseEntity.ok(response);
    }

}
