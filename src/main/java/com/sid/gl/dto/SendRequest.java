package com.sid.gl.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;


@Data
public class SendRequest {
    @Email
    private String username;
}
