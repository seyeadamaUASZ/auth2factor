package com.sid.gl.services.interfaces;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.sid.gl.dto.DeviceMetadataResponse;

import com.sid.gl.model.User;
import jakarta.servlet.http.HttpServletRequest;


import java.io.IOException;

import java.util.List;

public interface IDevice {
    List<DeviceMetadataResponse> listDevicesByUser(Long idUser);
    void verifyDevice(User user, HttpServletRequest request) throws IOException, GeoIp2Exception;
}
