package com.yumeinaruu.nseutask.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final BearerTokenResolver bearerTokenResolver;
    private final SecurityContextAuthenticator securityContextAuthenticator;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            BearerTokenResolver bearerTokenResolver,
            SecurityContextAuthenticator securityContextAuthenticator
    ) {
        this.jwtService = jwtService;
        this.bearerTokenResolver = bearerTokenResolver;
        this.securityContextAuthenticator = securityContextAuthenticator;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        bearerTokenResolver.findBearerToken(request)
                .flatMap(jwtService::findUsernameByToken)
                .ifPresent(username -> securityContextAuthenticator.authenticateUser(username, request));
        filterChain.doFilter(request, response);
    }

}
