package com.sid.gl.dto;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class UserRequest {
    private String lastName;
    private String firstName;
    @Email
    private String username;
    private String password;
    private boolean mfa;
}
