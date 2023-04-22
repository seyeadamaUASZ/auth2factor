package com.sid.gl.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class UserResponse {
    private String name;
    private String userName;
    private boolean enabled;
    private boolean mfa;
}
