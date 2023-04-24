package com.sid.gl.controllers;

import com.sid.gl.ApiResponse;
import com.sid.gl.dto.UserResponse;
import com.sid.gl.services.impl.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(value = "/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping(value = "/{username}")
    public ResponseEntity<?> findUser(@PathVariable("username") String username) {
        log.info("retrieving user {}", username);
        return  userService
                .findByUserName(username)
                .map(user -> ResponseEntity.ok(user))
                .orElseThrow(() -> new UsernameNotFoundException("user with "+username+" not found"));
    }

    @GetMapping
    public ResponseEntity<?> allUsers(){
        ApiResponse apiResponse = new ApiResponse(200,"all users retrieve");
        List<UserResponse> userResponses = userService.allUsers();
        apiResponse.setData(userResponses);
        return ResponseEntity.ok(apiResponse);
    }
}
