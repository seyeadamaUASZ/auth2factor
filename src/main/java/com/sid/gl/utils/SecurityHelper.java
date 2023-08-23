package com.sid.gl.utils;

import com.sid.gl.constants.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import javax.servlet.http.HttpServletRequest;



public interface SecurityHelper {
    static String loadUserAuthenticated(HttpServletRequest request){
        final String header = request.getHeader(SecurityConstants.HEADER_STRING);
        if(header==null || !checkHeader(header)){
            return null;
        }
        if(checkHeader(header)){
           String authToken = header.replace(SecurityConstants.TOKEN_PREFIX, StringUtils.EMPTY);
            try{
                return getClaims(authToken).getSubject();
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
    private static boolean checkHeader(String header){
        return header.startsWith(SecurityConstants.TOKEN_PREFIX);
    }
}
