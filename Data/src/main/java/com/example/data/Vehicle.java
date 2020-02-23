package com.example.data;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;

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

    @JsonGetter(value = "VIN")
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

    @JsonSetter(value = "VIN")
    public void setVin(String vin) {
        this.vin = vin;
    }
}
