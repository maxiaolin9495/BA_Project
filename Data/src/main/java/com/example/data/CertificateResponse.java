package com.example.data;

public class CertificateResponse {

    private String certificate;

    private String caId;
    public CertificateResponse(){
    }

    public void setCaId(String caId) {
        this.caId = caId;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getCaId() {
        return caId;
    }

    public String getCertificate() {
        return certificate;
    }
}
