package com.sid.gl.controllers;

import com.sid.gl.ApiResponse;
import com.sid.gl.config.aspect.Auditable;

import com.sid.gl.dto.DeviceMetadataResponse;
import com.sid.gl.dto.UserResponse;

import com.sid.gl.services.impl.UserService;
import com.sid.gl.utils.ApiPaths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//TODO add test for controller user
@RestController
@Slf4j
@RequestMapping(value = ApiPaths.API_VERSION+ApiPaths.API_USER)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping(value = "/{username}")
    public ResponseEntity<ApiResponse> findUser(@PathVariable("username") String username) {
        log.info("retrieving user {}", username);
        ApiResponse apiResponse = new ApiResponse(200,"user with username");
        UserResponse userResponse = userService.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("user with "+username+" not found"));
        apiResponse.setData(userResponse);
        return ResponseEntity.ok(apiResponse);

    }


    @Auditable("load all users")
    @GetMapping
    public ResponseEntity<ApiResponse> allUsers(){
        ApiResponse apiResponse = new ApiResponse(200,"all users retrieve");
        List<UserResponse> userResponses = userService.allUsers();
        apiResponse.setData(userResponses);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<ApiResponse> allDevicesByUsers(@PathVariable("id") Long id){
        ApiResponse apiResponse = new ApiResponse(200,"all devices users");
        List<DeviceMetadataResponse> devices = userService.allDevicesByUser(id);
        apiResponse.setData(devices);
        return ResponseEntity.ok(apiResponse);
    }


}
