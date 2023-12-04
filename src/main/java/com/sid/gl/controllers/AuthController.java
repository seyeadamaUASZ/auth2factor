package com.sid.gl.controllers;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.sid.gl.dto.*;
import com.sid.gl.model.User;
import com.sid.gl.model.UserLocation;

import com.sid.gl.services.impl.TopManagerService;
import com.sid.gl.services.impl.UserService;
import com.sid.gl.utils.ApiPaths;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;

//TODO add Test for controller
@RestController
@RequestMapping(value = ApiPaths.API_VERSION+"/auth/")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final TopManagerService topManagerService;


    @PostMapping("login")
    public ResponseEntity<AuthResponse> signin(@Valid @RequestBody final LoginRequest loginRequest, HttpServletRequest request) throws IOException, GeoIp2Exception {
        String token = userService.login(loginRequest,request);
        return ResponseEntity.ok(new AuthResponse(token, StringUtils.isEmpty(token)));
    }

    @PostMapping("verify")
    public ResponseEntity<AuthResponse> verify(@Valid @RequestBody final FactorRequest request){
        String token = userService.verifyCode(request);
        return ResponseEntity.ok(new AuthResponse(token,StringUtils.isEmpty(token)));
    }


    @PostMapping(value = "register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest userRequest,final HttpServletRequest request) throws IOException {
        log.info("register user {}", userRequest.getUsername());
        User userSaved = userService.registerUser(userRequest);
        //ajouter la localisation de l'utilisateur
        userService.addUserLocation(userSaved,request);
        //for uri after register
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path(ApiPaths.API_VERSION
                        +ApiPaths.API_USER+"/{username}")
                .buildAndExpand(userSaved.getUsername()).toUri();
        return ResponseEntity
                .created(location)
                .body(new SignupResponse(userSaved.isMfa(),
                        topManagerService.getUriForImage(userSaved.getSecret())));
    }

    @GetMapping("location/{id}")
    public ResponseEntity<List<UserLocation>> findLocationUser(@PathVariable("id")Long id){
        return  ResponseEntity.ok(userService.listLocationsUser(id));
    }

    @PostMapping("/sendMail")
    public ResponseEntity<String> sendMailForChangePassword(@Valid @RequestBody final SendRequest request) throws IOException {
     return ResponseEntity.ok(userService.sendEmailForPassword(request));
    }
   @PostMapping("/changePassword/{username}")
    public ResponseEntity<String> changePassword(@Valid @RequestBody NewPasswordRequest request, @PathVariable("username") String username){
          return ResponseEntity.ok(userService.changeUserPassword(username,request));
    }

    /*private String getClientIP(HttpServletRequest request) {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }*/
}
