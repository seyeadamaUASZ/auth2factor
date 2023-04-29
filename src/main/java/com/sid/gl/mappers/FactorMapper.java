package com.sid.gl.mappers;

import com.sid.gl.dto.UserRequest;
import com.sid.gl.dto.UserResponse;
import com.sid.gl.model.User;
import com.sid.gl.utils.MapUtils;
import org.springframework.beans.BeanUtils;

import java.util.List;

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

    public static List<UserResponse> builListUserResponse(List<User> users){
        return MapUtils.buildConvertList(users, FactorMapper::convertToUserResponse);
    }

    public static  User convertToUser(UserRequest userRequest){
        User user = new User();
        BeanUtils.copyProperties(userRequest,user);
        return user;
    }
}
