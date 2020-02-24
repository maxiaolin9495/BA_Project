package com.example.data;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Vehicle {

    private String vin;
    private String password;
    private String[] audience;
    private boolean blocked;

    public Vehicle(){

    }

    public String getPassword() {
        return password;
    }


    public String getVin() {
        return vin;
    }

    public String[] getAudience() {
        return audience;
    }

    public boolean getBlocked() {
        return blocked;
    }

    public void setAudience(String[] audience) {
        this.audience = audience;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }
}
