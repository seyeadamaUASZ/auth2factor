package com.sid.gl.constants;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityConstants {

    public static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    /**
     * Secret key for signature
     */
    public static final String SECRET_KEY = "secretkey";

    public static final String HEADER_STRING = "Authorization";

    public static final String TOKEN_PREFIX = "Bearer ";

    private SecurityConstants() {

        throw new UnsupportedOperationException();
    }

    /**
     * @return authenticated username from Security Context
     */
    public static String getAuthenticatedUsername() {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return userDetails.getUsername();
    }


}
