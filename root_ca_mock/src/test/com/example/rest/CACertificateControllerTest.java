package com.example.rest;

import com.example.data.CA;
import com.example.data.CertificateResponse;
import com.example.elasticsearch.ElasticSearchRepository;
import com.example.service.certificate.CertificateManagementForRootCA;
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

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(SpringJUnit4ClassRunner.class)
public class CACertificateControllerTest {

    private static String LTC_PUBLIC_KEY="PUBLIC_KEY";
    private static String caId="rcaA";
    private static String caId2="rcaB";
    private static String validCAId="ltcaA";
    private static String revokedCAId="ltcaX";
    private static String invalidCAId="ltca";
    private static String VALID_TOKEN="VALID_TOKEN";
    private static String INVALID_TOKEN="INVALID_TOKEN";
    private static String INDEX="index";


    @Mock
    CertificateManagementForRootCA certificateManagement;

    @Mock
    TokenValidationService tokenValidationService;

    @Mock
    ElasticSearchRepository elasticSearchRepository;

    @InjectMocks
    CACertificateController caCertificateController;

    @Before
    public void setup() {
        X509Certificate certificate = null;
        X509Certificate rootCertificate = null;
        X509Certificate rootCertificate2 = null;

        String[] caIds = new String[] {caId, caId2};

        try {
            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
            keyGen.generate(1024);
            certificate = keyGen.getSelfCertificate(new X500Name("cn=" + validCAId), (long) 365 * 24 * 60 * 60);
            keyGen.generate(1024);
            rootCertificate = keyGen.getSelfCertificate(new X500Name("cn=" + "rcaA"), (long) 365 * 24 * 60 * 60);
            keyGen.generate(1024);
            rootCertificate2 = keyGen.getSelfCertificate(new X500Name("cn=" + "rcaB"), (long) 365 * 24 * 60 * 60);

        } catch (Exception e) {
            e.printStackTrace();
        }

        CA ca = new CA();
        ca.setCaId(validCAId);
        ca.setRevoked(false);

        CA blockedCA = new CA();
        blockedCA.setCaId(revokedCAId);
        blockedCA.setRevoked(true);
        setField(caCertificateController, "tokenValidationService", tokenValidationService);
        setField(caCertificateController, "certificateManagement", certificateManagement);
        setField(caCertificateController, "elasticSearchRepository", elasticSearchRepository);
        setField(caCertificateController, "index", INDEX);
        setField(certificateManagement, "caIds", caIds);
        when(certificateManagement.createIntermediateCertificate(LTC_PUBLIC_KEY, validCAId)).thenReturn(certificate);
        when(certificateManagement.getCaId()).thenReturn(caId);
        when(certificateManagement.getCertificate(caId)).thenReturn(rootCertificate);
        when(certificateManagement.getCertificate(caId2)).thenReturn(rootCertificate2);
        when(tokenValidationService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(tokenValidationService.validateToken(INVALID_TOKEN)).thenReturn(false);
        when(elasticSearchRepository.findCA(INDEX, validCAId)).thenReturn(ca);
        when(elasticSearchRepository.findCA(INDEX, invalidCAId)).thenReturn(null);
        when(elasticSearchRepository.findCA(INDEX, revokedCAId)).thenReturn(blockedCA);
    }


    @Test
    public void requestRCACWithValidToken() throws Exception {

        ResponseEntity<CertificateResponse> response =  caCertificateController.requestCertificate(VALID_TOKEN, caId);
        assertEquals(response.getStatusCode().value(), 200);
        CertificateResponse certificateResponse = response.getBody();
        assertNotNull(certificateResponse.getCertificate());
        assertEquals(certificateResponse.getCaId(), caId);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
                        Base64.getDecoder().decode(
                                certificateResponse.getCertificate()
                        )));
        assertEquals(certificate.getIssuerDN().getName(), "CN=" + caId);
        assertEquals(certificate.getSubjectDN().getName(), "CN=" + caId);
    }

    @Test
    public void requestRCAC2WithValidToken() throws Exception {

        ResponseEntity<CertificateResponse> response =  caCertificateController.requestCertificate(VALID_TOKEN, caId2);
        assertEquals(response.getStatusCode().value(), 200);
        CertificateResponse certificateResponse = response.getBody();
        assertNotNull(certificateResponse.getCertificate());
        assertEquals(certificateResponse.getCaId(), caId2);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
                Base64.getDecoder().decode(
                        certificateResponse.getCertificate()
                )));
        assertEquals(certificate.getIssuerDN().getName(), "CN=" + caId2);
        assertEquals(certificate.getSubjectDN().getName(), "CN=" + caId2);
    }


    @Test
    public void requestRCACWithInValidToken() throws Exception {

        ResponseEntity<CertificateResponse> response =  caCertificateController.requestCertificate(INVALID_TOKEN, caId);
        assertEquals(response.getStatusCode().value(), 401);

    }

    @Test
    public void requestRCACWithoutToken() throws Exception {

        ResponseEntity<CertificateResponse> response =  caCertificateController.requestCertificate(null, caId);
        assertEquals(response.getStatusCode().value(), 401);

    }

    @Test
    public void requestRCAC2WithInValidToken() throws Exception {

        ResponseEntity<CertificateResponse> response =  caCertificateController.requestCertificate(INVALID_TOKEN, caId);
        assertEquals(response.getStatusCode().value(), 401);
    }

    @Test
    public void requestLTCWithValidCAId() throws Exception{
        ResponseEntity<CertificateResponse> response = caCertificateController.requestLTCAC(LTC_PUBLIC_KEY, validCAId);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
                Base64.getDecoder().decode(
                        response.getBody().getCertificate()
                )));
        assertEquals(certificate.getSubjectDN().getName(), "CN=" + validCAId);
    }

    @Test
    public void requestLTCWithInvalidCAId() throws Exception{
        ResponseEntity<CertificateResponse> response = caCertificateController.requestLTCAC(LTC_PUBLIC_KEY, invalidCAId);
        assertEquals(response.getStatusCode().value(), 401);
    }

    @Test
    public void requestLTCWithRevokedCAId() throws Exception{
        ResponseEntity<CertificateResponse> response = caCertificateController.requestLTCAC(LTC_PUBLIC_KEY, revokedCAId);
        assertEquals(response.getStatusCode().value(), 401);
    }



}
