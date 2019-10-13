package com.example.rest;

import com.example.data.ErrorResponse;
import com.example.data.TokenResponse;
import com.example.services.token.RequestTokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "Manipulation & token validation")
@RestController
@RequestMapping(value="/")
public class VehicleRestController {
    Logger logger = LoggerFactory.getLogger(VehicleRestController.class);

    @Autowired
    RequestTokenService requestTokenService;

    @RequestMapping(method = RequestMethod.GET, path = "requestToken")
    @ApiResponses({
            @ApiResponse(code = 200,
                    message = "Verified vehicle",
                    response = TokenResponse.class),
            @ApiResponse(code = 400,
                    message = "",
                    response = ErrorResponse.class)
    })
    public ResponseEntity<String> requestToken() throws Exception {
        logger.info("receive get Token request");
        String token = requestTokenService.requestToken();
        return ResponseEntity.ok(token);
    }

}
