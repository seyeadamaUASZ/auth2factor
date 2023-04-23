package com.sid.gl.config;

import com.sid.gl.constants.SecurityConstants;

import com.sid.gl.services.impl.JwtTokenManager;
import com.sid.gl.services.impl.UserDetailServiceImpl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
public class JwtTokenAuthentificationFilter extends OncePerRequestFilter {
    private final JwtTokenManager tokenProvider;
    private final UserDetailServiceImpl userDetailsService;

    public JwtTokenAuthentificationFilter( JwtTokenManager tokenProvider, UserDetailServiceImpl userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        //final String requestURI = req.getRequestURI();

        System.out.println("entrer dans le doInternal ");

        final String header = req.getHeader(SecurityConstants.HEADER_STRING);
        System.out.println("header "+header);

        if(header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            chain.doFilter(req, res);  		// If not valid, go to the next filter.
            return;
        }

        String username = null;
        String authToken = null;
        if (Objects.nonNull(header) && header.startsWith(SecurityConstants.TOKEN_PREFIX)) {

            authToken = header.replace(SecurityConstants.TOKEN_PREFIX, StringUtils.EMPTY);
            try {
                username = tokenProvider.getClaimsFromJWT(authToken).getSubject();
                log.info("username authenticated {} ",username);
            }
            catch (Exception e) {
                log.error("Authentication Exception : {}", e.getMessage());
            }
        }

        final SecurityContext securityContext = SecurityContextHolder.getContext();

        if (Objects.nonNull(username) && Objects.isNull(securityContext.getAuthentication())) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (tokenProvider.validateToken(authToken)) {

                final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                log.info("Authentication successful. Logged in username : {} ", username);
                securityContext.setAuthentication(authentication);
            }
        }

        chain.doFilter(req, res);
    }


}
