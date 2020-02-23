package com.example.data;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicKeyResponse {

    private String azsId;
    private String publicKey;

    public PublicKeyResponse(){
    }

    public String getAzsId() {
        return azsId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setAzsId(String azsId) {
        this.azsId = azsId;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
