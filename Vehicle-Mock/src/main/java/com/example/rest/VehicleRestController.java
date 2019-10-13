package com.example.rest;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "Manipulation & token validation")
@RestController
@RequestMapping(value="/")
public class VehicleRestController {
    Logger logger = LoggerFactory.getLogger(VehicleRestController.class);



}
