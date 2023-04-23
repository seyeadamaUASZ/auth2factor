package com.sid.gl.controllers;

import com.sid.gl.dto.*;
import com.sid.gl.model.User;
import com.sid.gl.services.impl.TopManagerService;
import com.sid.gl.services.impl.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

    @PostMapping("/login")
    public ResponseEntity<?> signin(@Valid @RequestBody final LoginRequest loginRequest){
        String token = userService.login(loginRequest);
        return ResponseEntity.ok(new AuthResponse(token, StringUtils.isEmpty(token)));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody final FactorRequest request){
        String token = userService.verifyCode(request);
        return ResponseEntity.ok(new AuthResponse(token,StringUtils.isEmpty(token)));
    }


    @PostMapping(value = "/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest userRequest) throws IOException {
        log.info("register user {}", userRequest.getUsername());
        User userSaved = userService.registerUser(userRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{username}")
                .buildAndExpand(userSaved.getUsername()).toUri();

        return ResponseEntity
                .created(location)
                .body(new SignupResponse(userSaved.isMfa(),
                        topManagerService.getUriForImage(userSaved.getSecret())));
    }
}
