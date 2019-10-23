package com.example.data;

public class DataRequest {
    private String message;

    public DataRequest(){

    }

    public DataRequest(String message){
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
