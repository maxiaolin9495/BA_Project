package com.example.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;



@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CertificateUpdateNotification implements Serializable {

    private String rootCAId;

    private int requestNumber;

    public CertificateUpdateNotification(){

    }

    public String getRootCAId() {
        return rootCAId;
    }

    public void setRootCAId(String rootCAId) {
        this.rootCAId = rootCAId;
    }

    public int getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(int requestNumber) {
        this.requestNumber = requestNumber;
    }
}
