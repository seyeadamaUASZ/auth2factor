package com.sid.gl;

import lombok.Data;

@Data
public class ApiResponse {
    private int status;
    private String message;
    private Object data;

    public ApiResponse(int status) {
        this.status = status;
    }

    public ApiResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
