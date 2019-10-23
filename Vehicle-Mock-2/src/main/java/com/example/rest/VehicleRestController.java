package com.example.rest;

import com.example.data.DataRequest;
import com.example.data.DataResponse;
import com.example.data.ErrorResponse;
import com.example.data.TokenResponse;
import com.example.services.data.DataService;
import com.example.services.token.RequestTokenService;
import com.example.services.validation.TokenValidationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Api(value = "Manipulation & token validation")
@RestController
@RequestMapping(value="/")
public class VehicleRestController {
    Logger log = LoggerFactory.getLogger(VehicleRestController.class);

    @Autowired
    RequestTokenService requestTokenService;

    @Autowired
    TokenValidationService tokenValidationService;

    @Autowired
    DataService dataService;

    @RequestMapping(method = RequestMethod.GET, path = "manipulation")
    @ApiResponses({
            @ApiResponse(code = 200,
                    message = "Verified vehicle",
                    response = TokenResponse.class),
            @ApiResponse(code = 400,
                    message = "",
                    response = ErrorResponse.class)
    })
    public ResponseEntity<String> manipulation() throws Exception {
        log.info("start request Token");
        String token = requestTokenService.requestToken();
        return ResponseEntity.ok(token);
    }

    @RequestMapping(method = RequestMethod.POST, path = "message")
    @ApiResponses({
            @ApiResponse(code = 200,
                    message = "Data received",
                    response = TokenResponse.class),
            @ApiResponse(code = 400,
                    message = "",
                    response = ErrorResponse.class),
            @ApiResponse(code = 401,
                    message = "Invalid Authorization",
                    response = ErrorResponse.class)
    })
    public ResponseEntity<DataResponse> saveMessage(@RequestHeader(AUTHORIZATION) String token, @RequestBody DataRequest dataRequest) {
        log.info("receive data from other point");
        tokenValidationService.validateToken(token);
        dataService.receiveData(dataRequest);
        return ResponseEntity.ok(new DataResponse("received"));
    }



}
