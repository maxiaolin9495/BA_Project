package com.example.data;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "error response")
public class ErrorResponse {
    @ApiModelProperty(
            allowableValues = "1-500",
            required = true
    )
    private int errorCode;
    @ApiModelProperty(
            required = true
    )
    private String message;
    public ErrorResponse(int errorCode, String message){
        this.errorCode = errorCode;
        this.message = message;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
