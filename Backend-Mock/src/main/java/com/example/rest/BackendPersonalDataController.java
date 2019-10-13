package com.example.rest;

import com.example.data.DataResponse;
import com.example.data.ErrorResponse;
import com.example.data.TokenResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "Get or Post Data")
@RestController
@RequestMapping(value="/v1/")
public class BackendPersonalDataController {
    Logger logger = LoggerFactory.getLogger(BackendPersonalDataController.class);

    @RequestMapping(method= RequestMethod.GET, path="/data", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(
            @ApiResponse(code=200,
            message = "verified request",
            response = TokenResponse.class),
            @ApiResponse(code=400,
            message = "",
            response = ErrorResponse.class)
    )
    public ResponseEntity<DataResponse> requestData(@RequestHeader(AUTHORIZATION) String token){
        logger.info("receive get data request");

        return ResponseEntity.ok(null);
    }

    @RequestMapping(method= RequestMethod.POST, path="/data",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(
            @ApiResponse(code=200,
                    message = "verified request",
                    response = TokenResponse.class),
            @ApiResponse(code=400,
                    message = "",
                    response = ErrorResponse.class)
    )
    public ResponseEntity<DataResponse> requestData(@RequestHeader(AUTHORIZATION) String token){
        logger.info("receive post data request");

        return ResponseEntity.ok(null);
    }
}
