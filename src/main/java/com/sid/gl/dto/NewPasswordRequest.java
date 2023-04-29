package com.sid.gl.dto;

import com.sid.gl.validators.PasswordMatches;
import lombok.Data;


@Data
@PasswordMatches
public class NewPasswordRequest {
    private String password;
    private String confirmPassword;
}
