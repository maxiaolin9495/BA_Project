package com.example.rest;

import com.example.data.ErrorResponse;
import com.example.data.TokenResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "Token Generation")
@RestController
@RequestMapping(value="/v1/")
public class BackendTokenController {
    Logger logger = LoggerFactory.getLogger(BackendTokenController.class);

    @RequestMapping(method= RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path="/requestToken")
    @ApiResponses(
            @ApiResponse(code=200,
            message = "Verified vehicle",
            response = TokenResponse.class),
            @ApiResponse(code=400,
            message = "",
            response = ErrorResponse.class)
    )
    public ResponseEntity<TokenResponse> requestToken(){
        logger.info("receive get Token request");

        return ResponseEntity.ok(null);
    }

}
