package com.sid.gl.controllers;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.sid.gl.dto.*;
import com.sid.gl.model.User;
import com.sid.gl.services.impl.DeviceService;
import com.sid.gl.services.impl.TopManagerService;
import com.sid.gl.services.impl.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping(value = "/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final TopManagerService topManagerService;

    private final DeviceService deviceService;

    @PostMapping("/login")
    public ResponseEntity<?> signin(@Valid @RequestBody final LoginRequest loginRequest, HttpServletRequest request) throws IOException, GeoIp2Exception {
        String token = userService.login(loginRequest,request);
        return ResponseEntity.ok(new AuthResponse(token, StringUtils.isEmpty(token)));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody final FactorRequest request){
        String token = userService.verifyCode(request);
        return ResponseEntity.ok(new AuthResponse(token,StringUtils.isEmpty(token)));
    }


    @PostMapping(value = "/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest userRequest,final HttpServletRequest request) throws IOException {
        log.info("register user {}", userRequest.getUsername());
        User userSaved = userService.registerUser(userRequest);
        //and add userLocation
        userService.addUserLocation(userSaved, getClientIP(request));

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{username}")
                .buildAndExpand(userSaved.getUsername()).toUri();

        return ResponseEntity
                .created(location)
                .body(new SignupResponse(userSaved.isMfa(),
                        topManagerService.getUriForImage(userSaved.getSecret())));
    }

    // for next feature flipping this feature
    @GetMapping("/devices/{id}")
    public ResponseEntity<?> getDevicesUser(@PathVariable("id") Long id){
        return ResponseEntity.ok(deviceService.listDevicesByUser(id));
    }

    @GetMapping("/location/{id}")
    public ResponseEntity<?> findLocationUser(@PathVariable("id")Long id){
        return  ResponseEntity.ok(userService.listLocationsUser(id));
    }

    private String getClientIP(HttpServletRequest request) {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
