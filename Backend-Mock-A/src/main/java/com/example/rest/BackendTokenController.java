package com.example.rest;

import com.example.data.ErrorResponse;
import com.example.data.TokenResponse;
import com.example.services.authentication.TokenGenerationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@Api(value = "Token Generation")
@Controller
@RequestMapping(value="/v1")
public class BackendTokenController {
    Logger log = LoggerFactory.getLogger(BackendTokenController.class);

    @Autowired
    TokenGenerationService tokenGenerationService;

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/requestToken")
    @ApiResponses({
            @ApiResponse(code = 200,
                    message = "Verified vehicle",
                    response = TokenResponse.class),
            @ApiResponse(code = 400,
                    message = "",
                    response = ErrorResponse.class)
    })
    public ResponseEntity<TokenResponse> requestToken(@RequestParam(value = "STK") String encryptedShortTermKey, @RequestParam(value = "vehicle_id") String maskedVehicleId) throws Exception {
        log.info("receive get Token request");
        if(encryptedShortTermKey.equals("") | maskedVehicleId.equals("")){
            throw new RuntimeException("Bad Request");
        }
        TokenResponse tokenResponse = tokenGenerationService.generateToken(encryptedShortTermKey, maskedVehicleId);
        return ResponseEntity.ok(tokenResponse);
    }

}
