package com.sid.gl.dto;

import lombok.Data;

import java.util.Date;

@Data
public class DeviceMetadataResponse {
    private Long id;
    private String deviceDetails;
    private String location;
    private Date lastLoggedIn;
}
