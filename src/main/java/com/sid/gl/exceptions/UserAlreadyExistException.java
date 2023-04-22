package com.sid.gl.exceptions;

public class UserAlreadyExistException extends  RuntimeException{
    public UserAlreadyExistException(String message){
        super(message);
    }
}
