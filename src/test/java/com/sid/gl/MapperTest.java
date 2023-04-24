package com.sid.gl;

import com.sid.gl.dto.UserRequest;
import com.sid.gl.dto.UserResponse;
import com.sid.gl.mappers.FactorMapper;
import com.sid.gl.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class MapperTest {

    @Test
    public void should_test_mapperUser(){
        UserRequest userRequest = new UserRequest();
        userRequest.setPassword("passer123");
        userRequest.setFirstName("Baba");
        userRequest.setLastName("fall");
        userRequest.setMfa(true);
        userRequest.setUsername("aaa@mailto.com");

        User result = FactorMapper.convertToUser(userRequest);
        assertNotNull(result);
        assertEquals("Baba",result.getFirstName());
        assertEquals("fall",result.getLastName());
        assertEquals("aaa@mailto.com",result.getUsername());
    }

    @Test
    public void should_test_userResponse(){
        User user = new User();
        user.setPassword("passer123");
        user.setFirstName("Baba");
        user.setLastName("fall");
        user.setMfa(true);
        user.setUsername("aaa@mailto.com");
        UserResponse result = FactorMapper.convertToUserResponse(user);

        assertNotNull(result);
    }
}
