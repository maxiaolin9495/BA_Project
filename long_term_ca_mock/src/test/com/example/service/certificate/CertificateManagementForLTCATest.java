package com.example.service.certificate;

import com.example.data.CertificateResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(SpringJUnit4ClassRunner.class)
public class CertificateManagementForLTCATest{

    private static String vin = "vehicleA1";
    PublicKey PUBLIC_KEY;
    PublicKey LTCAPublicKey;
    PrivateKey PRIVATE_KEY;
    PrivateKey LTCAPrivateKey;

    String caId = "ltcaA";

    @Value("${root.ca.endpoint}")
    String rootCAEndpoint;

    X509Certificate rootCACertificate;

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    CertificateManagementForLTCA certificateManagement;

    @Before
    public void setup(){
        try {
            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
            keyGen.generate(1024);
            PUBLIC_KEY = keyGen.getPublicKey();
            PRIVATE_KEY = keyGen.getPrivateKey();
            rootCACertificate = keyGen.getSelfCertificate(new X500Name("cn=rcaA"), (long) 365 * 24 * 60 * 60);

            keyGen.generate(1024);

            LTCAPublicKey = keyGen.getPublicKey();
            LTCAPrivateKey = keyGen.getPrivateKey();

            X509Certificate intermediateCertificate = certificateManagement.signCertificate(new String (Base64.getEncoder().encode(LTCAPublicKey.getEncoded()))
                    ,caId, rootCACertificate, PRIVATE_KEY);
            CertificateResponse response = new CertificateResponse();
            response.setCaId("rcaA");
            response.setCertificate(new String(Base64.getEncoder().encode(intermediateCertificate.getEncoded())));
            when(restTemplate.exchange(eq(rootCAEndpoint), eq(HttpMethod.POST), any(HttpEntity.class), eq(CertificateResponse.class))).thenReturn(ResponseEntity.ok(response));

            setField(certificateManagement, "restTemplate", restTemplate);
            setField(certificateManagement, "caId", caId);
            setField(certificateManagement, "rootCAEndpoint", rootCAEndpoint);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update root certificate");
        }

    }

    @Test
    public void createLTC() {

        try {
            setField(certificateManagement, "PUBLIC_KEY", LTCAPublicKey);
            setField(certificateManagement, "PRIVATE_KEY", LTCAPrivateKey);
            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
            keyGen.generate(1024);
            PublicKey publicKey = keyGen.getPublicKey();
            certificateManagement.getCertificate();
            setField(certificateManagement, "PUBLIC_KEY", LTCAPublicKey);
            setField(certificateManagement, "PRIVATE_KEY", LTCAPrivateKey);
            X509Certificate ltc = certificateManagement.createLTC(new String(Base64.getEncoder().encode(publicKey.getEncoded())),vin);
            ltc.verify(LTCAPublicKey);
            assertEquals(ltc.getIssuerDN().getName(), "CN=" + caId);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }
    @Test
    public void getCertificate() {
        X509Certificate ltcac = certificateManagement.getCertificate();

        assertEquals(ltcac.getSubjectDN().getName(), "CN="+caId);
        try {
            ltcac.verify(PUBLIC_KEY);
        } catch (Exception e) {
           fail();
        }
    }

    public String getCaId() {
        return caId;
    }
}
