package com.sid.gl.utils;

import com.sid.gl.constants.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;



public interface SecurityHelper {
    static String loadUsername(HttpServletRequest request){
        final String header = request.getHeader(SecurityConstants.HEADER_STRING);
        if(header !=null && header.startsWith(SecurityConstants.TOKEN_PREFIX)){
           String authToken = header.replace(SecurityConstants.TOKEN_PREFIX, StringUtils.EMPTY);
            try{
                String userName = getClaims(authToken).getSubject();
                return userName;
            }catch (Exception e){
                throw new RuntimeException(e.getMessage());
            }
        }
        return null;
    }

    private static Claims getClaims(String token){
        return Jwts.parser()
                .setSigningKey(SecurityConstants.SECRET_KEY.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }
}
