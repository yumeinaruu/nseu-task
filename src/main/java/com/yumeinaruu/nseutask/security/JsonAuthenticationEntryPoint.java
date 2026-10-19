package com.yumeinaruu.nseutask.security;

import com.yumeinaruu.nseutask.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorWriter securityErrorWriter;

    public JsonAuthenticationEntryPoint(SecurityErrorWriter securityErrorWriter) {
        this.securityErrorWriter = securityErrorWriter;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        log.warn("Failed authentication: path={}, type={}",
                request.getRequestURI(), authException.getClass().getSimpleName());
        securityErrorWriter.writeError(request, response, ErrorCode.UNAUTHORIZED);
    }
}
