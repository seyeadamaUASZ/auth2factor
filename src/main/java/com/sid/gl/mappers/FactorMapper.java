package com.sid.gl.mappers;

import com.sid.gl.dto.DeviceMetadataResponse;
import com.sid.gl.dto.UserRequest;
import com.sid.gl.dto.UserResponse;
import com.sid.gl.model.DeviceMetadata;
import com.sid.gl.model.User;
import com.sid.gl.utils.MapUtils;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Objects;

public class FactorMapper {

    private FactorMapper(){

    }
    public static UserResponse convertToUserResponse(User user){
        return UserResponse.builder()
                .userName(user.getUsername())
                .name(user.getLastName().concat(" ").concat(user.getLastName()))
                .enabled(user.isEnabled())
                .mfa(user.isMfa())
                .build();
    }

    public static DeviceMetadataResponse buildDeviceResponse(DeviceMetadata deviceMetadata){
        DeviceMetadataResponse response = new DeviceMetadataResponse();
        if(Objects.isNull(deviceMetadata)){
            return response;
        }
        response.setId(deviceMetadata.getId());
        response.setLocation(deviceMetadata.getLocation());
        response.setLastLoggedIn(deviceMetadata.getLastLoggedIn());
        response.setDeviceDetails(deviceMetadata.getDeviceDetails());
        return response;
    }

    public static List<UserResponse> builListUserResponse(List<User> users){
        return MapUtils.buildConvertList(users, FactorMapper::convertToUserResponse);
    }

    public static List<DeviceMetadataResponse> buildListDeviceResponse(List<DeviceMetadata> devices){
        return MapUtils.buildConvertList(devices,FactorMapper::buildDeviceResponse);
    }

    public static  User convertToUser(UserRequest userRequest){
        User user = new User();
        BeanUtils.copyProperties(userRequest,user);
        return user;
    }
}
