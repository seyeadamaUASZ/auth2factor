package com.sid.gl.mappers;

import com.sid.gl.dto.UserRequest;
import com.sid.gl.dto.UserResponse;
import com.sid.gl.model.User;
import com.sid.gl.utils.MapUtils;
import org.springframework.beans.BeanUtils;

import java.util.List;

public class FactorMapper {
    public static UserResponse convertToUserResponse(User user){
        UserResponse userResponse = UserResponse.builder()
                .userName(user.getUsername())
                .name(user.getLastName().concat(" ").concat(user.getLastName()))
                .enabled(user.isEnabled())
                .mfa(user.isMfa())
                .build();
        return userResponse;
    }

    public static List<UserResponse> builListUserResponse(List<User> users){
        List<UserResponse> userResponses = MapUtils.buildConvertList(users, source -> convertToUserResponse(source));
        return userResponses;
    }

    public static  User convertToUser(UserRequest userRequest){
        User user = new User();
        BeanUtils.copyProperties(userRequest,user);
        return user;
    }
}
