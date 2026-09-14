package com.kindtail.adoptmate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kindtail.adoptmate.common.dto.ApiErrorResponse;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Keeps HTTP error serialization separate from security route configuration. */
@Component
@RequiredArgsConstructor
public class SecurityExceptionHandlers {

    private final ObjectMapper objectMapper;

    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> write(response,
                request.getAttribute("exception") instanceof ErrorCode errorCode ? errorCode : ErrorCode.UNAUTHORIZED);
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> write(response, ErrorCode.UNAUTHORIZED_AUTHOR);
    }

    private void write(HttpServletResponse response, ErrorCode errorCode) throws java.io.IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiErrorResponse.of(errorCode)));
    }
}
