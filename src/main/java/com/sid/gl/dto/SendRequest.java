package com.sid.gl.dto;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class SendRequest {
    @Email
    private String username;
}
