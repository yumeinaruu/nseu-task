package com.yumeinaruu.nseutask.security;

import com.yumeinaruu.nseutask.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityErrorWriter securityErrorWriter;

    public JsonAccessDeniedHandler(SecurityErrorWriter securityErrorWriter) {
        this.securityErrorWriter = securityErrorWriter;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        log.warn("Failed authorization: path={}, type={}",
                request.getRequestURI(), accessDeniedException.getClass().getSimpleName());
        securityErrorWriter.writeError(request, response, ErrorCode.ACCESS_DENIED);
    }

}
