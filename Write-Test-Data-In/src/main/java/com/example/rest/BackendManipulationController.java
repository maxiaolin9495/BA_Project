package com.example.rest;

import com.example.data.CA;
import com.example.data.Vehicle;
import com.example.elasticsearch.ElasticSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value="/v1")
public class BackendManipulationController {

    Logger log = LoggerFactory.getLogger(BackendManipulationController.class);


    @Autowired
    ElasticSearchRepository elasticSearchRepository;

    @RequestMapping(method = RequestMethod.GET, path = "/addData")
    public ResponseEntity addData() throws Exception {
        log.info("received manipulation request");

        Vehicle a1 = new Vehicle();
        a1.setPassword("password12");
        a1.setVin("vehicleA1");
        a1.setAudience(new String[] {"rcaA", "ltcaA"});
        a1.setBlocked(false);

        Vehicle b1 = new Vehicle();
        b1.setPassword("password12");
        b1.setVin("vehicleB1");
        b1.setAudience(new String[] {"rcaB", "ltcaB"});
        b1.setBlocked(false);

        CA ltcaA = new CA();
        ltcaA.setCaId("ltcaA");
        ltcaA.setRevoked(false);

        CA ltcaB = new CA();
        ltcaB.setCaId("ltcaB");
        ltcaB.setRevoked(false);


        elasticSearchRepository.addCA("automakera", ltcaA);
        elasticSearchRepository.addCA("automakerb", ltcaB);
        elasticSearchRepository.addVehicle("automakera", a1);
        elasticSearchRepository.addVehicle("automakerb", b1);



        return ResponseEntity.ok().build();
    }

}
