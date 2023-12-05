package com.sid.gl.services.interfaces;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.sid.gl.dto.*;
import com.sid.gl.exceptions.BadRequestException;
import com.sid.gl.exceptions.UserAlreadyExistException;
import com.sid.gl.exceptions.Auth2factorNotFoundException;
import com.sid.gl.model.NewLocationToken;
import com.sid.gl.model.User;
import com.sid.gl.model.UserLocation;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IUser {
    List<UserResponse> allUsers();
    Optional<UserResponse> findByUserName(String username) throws Auth2factorNotFoundException;
    UserResponse findUserById(Long id);
    User findUserByUserName(String username) throws Auth2factorNotFoundException;
    User registerUser(UserRequest userRequest) throws UserAlreadyExistException, IOException;

    String login(LoginRequest loginRequest, HttpServletRequest request) throws IOException, GeoIp2Exception;

    String verifyCode(FactorRequest request) throws BadRequestException;

    NewLocationToken isNewLocation(String username,String ip);
    void addUserLocation(User user,HttpServletRequest request);

    List<UserLocation> listLocationsUser(Long id);

    String sendEmailForPassword(SendRequest request) throws IOException;

    String changeUserPassword(String username,NewPasswordRequest request);

    List<DeviceMetadataResponse> allDevicesByUser(Long idUser);
}
