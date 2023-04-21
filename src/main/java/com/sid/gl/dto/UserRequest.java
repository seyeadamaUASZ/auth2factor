package com.sid.gl.dto;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class UserRequest {
    private String name;
    @Email
    private String username;
    private String password;
    private boolean mfa;
}
