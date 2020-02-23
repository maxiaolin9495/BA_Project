package com.example.rest;

import com.example.data.TokenResponse;
import com.example.data.Vehicle;
import com.example.elasticsearch.ElasticSearchRepository;
import com.example.services.authentication.TokenGenerationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(SpringJUnit4ClassRunner.class)
public class BackendTokenControllerTest {

    private static final String VALID_VIN = "validVehicle";
    private static final String INVALID_VIN = "invalidVehicle";
    private static final String PASSWORD = "password";
    private static final String INDEX = "automakerA";
    private static final String VALID_AUDIENCE = "rcaA ltcaA";
    private static final String INVALID_AUDIENCE = "rcaB";
    private static final String VALID_TOKEN = "validToken";

    @Value("${public_key}")
    protected String BACKEND_PUBLIC_KEY;
    @Value("${private_key}")
    protected String BACKEND_PRIVATE_KEY;
    @Value("${token.issuer}")
    protected String issuer;

    @InjectMocks
    protected BackendTokenController backendTokenController;

    @Mock
    protected ElasticSearchRepository elasticSearchRepository;
    @Mock
    protected TokenGenerationService tokenGenerationService;

    @Before
    public void setUp() throws Exception {
        Set<String> audience = new HashSet<>();
        audience.add("rcaA");
        audience.add("ltcaA");

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setExp("1234");
        tokenResponse.setToken(VALID_TOKEN);

        when(tokenGenerationService.generateToken(VALID_VIN, audience)).thenReturn(tokenResponse);
        when(tokenGenerationService.generateToken(VALID_VIN, new HashSet<>())).thenThrow(new RuntimeException("Request with invalid audience group"));

        Vehicle v = new Vehicle();
        v.setVin(VALID_VIN);
        v.setAudience(new String[]{"rcaA", "ltcaA"});
        v.setBlocked(false);
        v.setPassword(PASSWORD);

        when(elasticSearchRepository.findVehicle(INDEX, VALID_VIN)).thenReturn(v);
        when(elasticSearchRepository.findVehicle(INDEX, INVALID_VIN)).thenReturn(null);

        setField(backendTokenController, "elasticSearchRepository", elasticSearchRepository);
        setField(backendTokenController, "tokenGenerationService", tokenGenerationService);
        setField(backendTokenController, "index", INDEX);


    }


    @Test
    public void tokenRequestWithValidVinAndValidPassword() {
        try {
            ResponseEntity<TokenResponse> tokenResponse = backendTokenController.requestToken(PASSWORD, VALID_VIN, VALID_AUDIENCE);
            assertEquals(tokenResponse.getBody().getToken(), VALID_TOKEN);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void tokenRequestWithValidVinAndInvalidPassword() {
        try {
            ResponseEntity tokenResponse = backendTokenController.requestToken("abcd", VALID_VIN, VALID_AUDIENCE);

            assertTrue(tokenResponse.getStatusCode() == HttpStatus.BAD_REQUEST);
            assertTrue(tokenResponse.getBody().equals("incorrect password"));
        } catch (Exception e) {
            fail();

        }
    }

    @Test
    public void tokenRequestWithInValidVin() {
        try {
            ResponseEntity tokenResponse = backendTokenController.requestToken(PASSWORD, INVALID_VIN, VALID_AUDIENCE);

            assertTrue(tokenResponse.getStatusCode() == HttpStatus.BAD_REQUEST);
            assertTrue(tokenResponse.getBody().equals("invalid vin id"));
        } catch (Exception e) {
            fail();

        }
    }

    @Test
    public void tokenRequestWithValidVinButInvalidAudience() {
        try {
            ResponseEntity tokenResponse = backendTokenController.requestToken(PASSWORD, VALID_VIN, INVALID_AUDIENCE);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(e.getMessage(), "Request with invalid audience group");
        }
    }

}
