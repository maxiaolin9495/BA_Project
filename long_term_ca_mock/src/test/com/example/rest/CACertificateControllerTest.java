package com.example.rest;

import com.example.data.CertificateResponse;
import com.example.service.certificate.CertificateManagementForLTCA;
import com.example.service.validation.TokenValidationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(SpringJUnit4ClassRunner.class)
public class CACertificateControllerTest {

    private static String LTC_PUBLIC_KEY="PUBLIC_KEY";
    private static String VIN="VehicleA1";
    private static String VALID_TOKEN="VALID_TOKEN";
    private static String INVALID_TOKEN="INVALID_TOKEN";
    String caId = "ltcaA";

    @Mock
    CertificateManagementForLTCA certificateManagement;

    @Mock
    TokenValidationService tokenValidationService;

    @InjectMocks
    CACertificateController caCertificateController;

    @Before
    public void setup() {
        X509Certificate certificate = null;
        try {
            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
            keyGen.generate(1024);
            certificate = keyGen.getSelfCertificate(new X500Name("cn=" + VIN), (long) 365 * 24 * 60 * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setField(caCertificateController, "tokenValidationService", tokenValidationService);
        setField(caCertificateController, "certificateManagement", certificateManagement);
        when(certificateManagement.createLTC(LTC_PUBLIC_KEY, VIN)).thenReturn(certificate);
        when(certificateManagement.getCaId()).thenReturn(caId);
        when(tokenValidationService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(tokenValidationService.validateToken(INVALID_TOKEN)).thenReturn(false);
    }


    @Test
    public void requestLTCWithValidToken() throws Exception {

        ResponseEntity<CertificateResponse> response =  caCertificateController.requestLTC(VALID_TOKEN, LTC_PUBLIC_KEY, VIN);
        assertEquals(response.getStatusCode().value(), 200);
        CertificateResponse certificateResponse = response.getBody();
        assertNotNull(certificateResponse.getCertificate());
        assertEquals(certificateResponse.getCaId(), caId);

    }

    @Test
    public void requestLTCWithInValidToken() throws Exception {

        ResponseEntity<CertificateResponse> response =  caCertificateController.requestLTC(INVALID_TOKEN, LTC_PUBLIC_KEY, VIN);
        assertEquals(response.getStatusCode().value(), 401);

    }


}
