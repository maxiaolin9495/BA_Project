package com.example.data;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CA {

    private String caId;

    private boolean revoked;

    public CA(){

    }

    public String getCaId() {
        return caId;
    }

    public void setCaId(String caId) {
        this.caId = caId;
    }

    public boolean getRevoked() {
        return revoked;
    }

    public void setCaId(boolean revoked) {
        this.revoked = revoked;
    }
}
