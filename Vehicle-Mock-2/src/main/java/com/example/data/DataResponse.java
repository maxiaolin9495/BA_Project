package com.example.data;

public class DataResponse {

    private String message;

    public DataResponse(String message){
        this.message = message;
    }

    public DataResponse(){

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
