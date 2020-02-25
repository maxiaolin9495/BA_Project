package com.example.service.certificate;

import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

public class CertificateManagement {

    Logger logger = LoggerFactory.getLogger(CertificateManagement.class);

    public void createRootCertificate() {
    }

    protected X509Certificate signCertificate(String publicKey, String subjectId, X509Certificate signerCertificate, PrivateKey privateKey) {
        try {
            byte[] decBackendPubKey = Base64.getDecoder().decode(publicKey);
            RSAPublicKey pub = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec((decBackendPubKey)));
            X500Principal x500Principal = new X500Principal("cn=" + subjectId);
            X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
            certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
            certGen.setIssuerDN(signerCertificate.getSubjectX500Principal());
            certGen.setNotBefore(new Date(System.currentTimeMillis()));
            certGen.setNotAfter(new Date(System.currentTimeMillis() + 30 * 24 * 60 * 60));
            certGen.setSubjectDN(x500Principal);
            certGen.setPublicKey(pub);
            certGen.setSignatureAlgorithm(signerCertificate.getSigAlgName());

            return certGen.generate(privateKey);
        } catch (Exception ex) {
            logger.error("Failed to generate certificate");
        }
        return null;
    }

    protected static X509Certificate createSelfSignedCertificate(X509Certificate certificate, X509Certificate issuerCertificate,PrivateKey issuerPrivateKey){
        try{
            Principal issuer = issuerCertificate.getSubjectDN();
            String issuerSigAlg = issuerCertificate.getSigAlgName();

            byte[] inCertBytes = certificate.getTBSCertificate();
            X509CertInfo info = new X509CertInfo(inCertBytes);
            info.set(X509CertInfo.ISSUER, (X500Name) issuer);

            X509CertImpl outCert = new X509CertImpl(info);
            outCert.sign(issuerPrivateKey, issuerSigAlg);

            return outCert;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public X509Certificate getCertificate(String caId) {
        return null;
    }
}
