package com.sid.gl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("adama")
public class ApiKeyCredential {
    private String apikey;
    private String email;
}
