package com.chwihae.config.security;

import com.chwihae.controller.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

import static com.chwihae.exception.CustomExceptionError.INVALID_TOKEN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException authException) throws IOException, ServletException {
        ApiResponse<Object> body = ApiResponse.of(INVALID_TOKEN.code(), INVALID_TOKEN.message(), null);
        res.setContentType(APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(UTF_8.name());
        res.setStatus(INVALID_TOKEN.status().value());
        res.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

