package com.sid.gl.services.interfaces;

import com.sid.gl.dto.FactorRequest;
import com.sid.gl.dto.LoginRequest;
import com.sid.gl.dto.UserRequest;
import com.sid.gl.dto.UserResponse;
import com.sid.gl.exceptions.BadRequestException;
import com.sid.gl.exceptions.UserAlreadyExistException;
import com.sid.gl.exceptions.UserNotFoundException;
import com.sid.gl.model.User;

import java.util.List;
import java.util.Optional;

public interface IUser {
    List<UserResponse> allUsers();
    Optional<UserResponse> findByUserName(String username) throws UserNotFoundException;
    UserResponse findUserById(Long id);
    Optional<User> findUserByUserName(String username) throws UserNotFoundException;
    User registerUser(UserRequest userRequest) throws UserAlreadyExistException;

    String login(LoginRequest loginRequest);

    String verifyCode(FactorRequest request) throws BadRequestException;
}
