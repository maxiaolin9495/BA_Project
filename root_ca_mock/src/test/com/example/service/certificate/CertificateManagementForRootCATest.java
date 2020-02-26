package com.example.service.certificate;

import com.example.data.CertificateResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(SpringJUnit4ClassRunner.class)
public class CertificateManagementForRootCATest {


    String caId = "rcaA";
    String caId2 = "rcaB";
    String ltcaId = "ltcaA";


    String rootCAEndpoint = "http://localhost:8890/v1/requestCertificate";
    String rootCAEndpoint2 = "http://localhost:8891/v1/requestCertificate";


    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    CertificateManagementForRootCA certificateManagement;

    @Before
    public void setup(){
        try {

            String[] caIds = new String[] {caId, caId2};
            String[] urls = new String[] {rootCAEndpoint, rootCAEndpoint2};

            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
            keyGen.generate(1024);
            X509Certificate rootCACertificate2 = keyGen.getSelfCertificate(new X500Name("cn="+caId2), (long) 365 * 24 * 60 * 60);

            CertificateResponse response = new CertificateResponse();
            response.setCaId(caId2);
            response.setCertificate(new String(Base64.getEncoder().encode(rootCACertificate2.getEncoded())));
            when(restTemplate.exchange(eq(rootCAEndpoint2), eq(HttpMethod.POST), any(HttpEntity.class), eq(CertificateResponse.class))).thenReturn(ResponseEntity.ok(response));

            setField(certificateManagement, "restTemplate", restTemplate);
            setField(certificateManagement, "caId", caId);
            setField(certificateManagement, "caIds", caIds);
            setField(certificateManagement, "urls", urls);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update root certificate");
        }

    }

    @Test
    public void createRCACTest() {
        try {
            certificateManagement.createRootCertificate();
            X509Certificate rootCertificate = certificateManagement.getCertificate(caId);
            assertNotNull(rootCertificate);
            assertEquals(rootCertificate.getIssuerDN().getName(), "CN=" + caId);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }
    @Test
    public void createLTCACertificate() {
        try {

            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
            keyGen.generate(1024);
            PublicKey publicKey = keyGen.getPublicKey();
            X509Certificate ltcac = certificateManagement.createIntermediateCertificate(
                    new String(Base64.getEncoder().encode(publicKey.getEncoded())), ltcaId);
            assertEquals(ltcac.getSubjectDN().getName(), "CN="+ltcaId);
            assertEquals(ltcac.getIssuerDN().getName(), "CN="+caId);
            ltcac.verify(certificateManagement.getCertificate(caId).getPublicKey());
        } catch (Exception e) {
           fail();
        }
    }

    @Test
    public void getAnotherRCAC(){
        try {

            X509Certificate rcacB = certificateManagement.getCertificate(caId2);
            assertEquals(rcacB.getSubjectDN().getName(), "CN="+caId2);
            rcacB.verify(rcacB.getPublicKey());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void getCaId() {
        assertEquals(caId, certificateManagement.getCaId());
    }
}
