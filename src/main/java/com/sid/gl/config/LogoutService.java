package com.sid.gl.config;

import com.sid.gl.model.NewLocationToken;
import com.sid.gl.repositories.NewTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
public class LogoutService implements LogoutHandler {
    private final NewTokenRepository repository;

    public LogoutService(NewTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
       final String authHeader = request.getHeader("Authorization");
       final String jwt;
       if(authHeader==null || !authHeader.startsWith("Bearer ")){
           return;
       }
       jwt = authHeader.substring(7);
        NewLocationToken newLocationToken = repository.findByToken(jwt);
        if(newLocationToken !=null){
            newLocationToken.setExpired(true);
            newLocationToken.setRevoked(true);
            repository.save(newLocationToken);
            SecurityContextHolder.clearContext();
        }


    }
}
