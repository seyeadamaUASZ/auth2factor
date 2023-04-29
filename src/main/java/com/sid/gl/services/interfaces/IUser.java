package com.sid.gl.services.interfaces;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.sid.gl.dto.FactorRequest;
import com.sid.gl.dto.LoginRequest;
import com.sid.gl.dto.UserRequest;
import com.sid.gl.dto.UserResponse;
import com.sid.gl.exceptions.BadRequestException;
import com.sid.gl.exceptions.UserAlreadyExistException;
import com.sid.gl.exceptions.UserNotFoundException;
import com.sid.gl.model.NewLocationToken;
import com.sid.gl.model.User;
import com.sid.gl.model.UserLocation;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IUser {
    List<UserResponse> allUsers();
    Optional<UserResponse> findByUserName(String username) throws UserNotFoundException;
    UserResponse findUserById(Long id);
    Optional<User> findUserByUserName(String username) throws UserNotFoundException;
    User registerUser(UserRequest userRequest) throws UserAlreadyExistException, IOException;

    String login(LoginRequest loginRequest, HttpServletRequest request) throws IOException, GeoIp2Exception;

    String verifyCode(FactorRequest request) throws BadRequestException;

    NewLocationToken isNewLocation(String username,String ip);
    void addUserLocation(User user,String ip);

    List<UserLocation> listLocationsUser(Long id);
}
