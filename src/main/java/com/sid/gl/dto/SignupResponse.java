package com.sid.gl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignupResponse {
        private boolean mfa;
        private String secretImageUri;

}
