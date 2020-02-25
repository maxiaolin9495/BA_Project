package com.example.data;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CertificateUpdateNotifyication implements Serializable {

    private String rootCAId;

    public CertificateUpdateNotifyication(){

    }

    public String getRootCAId() {
        return rootCAId;
    }

    public void setRootCAId(String rootCAId) {
        this.rootCAId = rootCAId;
    }
}
