package com.example.rest;

import com.example.data.ErrorResponse;
import com.example.data.TokenResponse;
import com.example.services.authentication.TokenGenerationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Api(value = "Token Generation")
@Controller
@RequestMapping(value="/v1")
public class BackendTokenController {
    Logger logger = LoggerFactory.getLogger(BackendTokenController.class);

    TokenGenerationService tokenGenerationService = new TokenGenerationService();

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/requestToken")
    @ApiResponses({
            @ApiResponse(code = 200,
                    message = "Verified vehicle",
                    response = TokenResponse.class),
            @ApiResponse(code = 400,
                    message = "",
                    response = ErrorResponse.class)
    })
    public ResponseEntity<TokenResponse> requestToken(@RequestParam(value = "STK") String encryptedShortTermKey) throws Exception {
        logger.info("receive get Token request");
        TokenResponse tokenResponse = tokenGenerationService.generateToken(encryptedShortTermKey);
        return ResponseEntity.ok(tokenResponse);
    }

}
