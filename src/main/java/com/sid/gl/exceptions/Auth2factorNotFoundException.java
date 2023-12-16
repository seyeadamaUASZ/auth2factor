package com.sid.gl.exceptions;

public class Auth2factorNotFoundException extends  RuntimeException{
    public Auth2factorNotFoundException(String message){
        super(message);
    }
}
