package com.sid.gl.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;


@Data
public class UserRequest {
    private String lastName;
    private String firstName;
    @Email
    private String username;
    private String password;
    private boolean mfa;
}
