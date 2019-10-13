package com.example.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;


@ApiModel(value = "token response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {
    private String exp;
    private String token;

    public TokenResponse(String exp, String token){
        this.exp = exp;
        this.token = token;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExp() {
        return exp;
    }

    public String getToken() {
        return token;
    }
}
