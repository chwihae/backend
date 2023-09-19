package com.chwihae.config.security;


import com.chwihae.controller.ApiResponse;
import com.chwihae.dto.user.UserContext;
import com.chwihae.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chwihae.exception.CustomExceptionError.INVALID_TOKEN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private final String secretKey;
    private final JwtTokenHandler jwtTokenHandler;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final Map<RegexRequestMatcher, List<HttpMethod>> whitelistMap = new HashMap<>();

    public JwtTokenFilter(String secretKey,
                          JwtTokenHandler jwtTokenHandler,
                          UserService userService,
                          ObjectMapper objectMapper,
                          Map<String, List<HttpMethod>> whitelistMap) {
        this.secretKey = secretKey;
        this.jwtTokenHandler = jwtTokenHandler;
        this.userService = userService;
        this.objectMapper = objectMapper;
        whitelistMap.forEach((url, methods) -> this.whitelistMap.put(new RegexRequestMatcher(url, null), methods));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        try {
            String token = req.getHeader(HttpHeaders.AUTHORIZATION);

            if (token == null && isWhitelistRequest(req)) {
                chain.doFilter(req, res);
                return;
            }

            jwtTokenHandler.verifyToken(secretKey, token);
            Long userId = jwtTokenHandler.getUserIdFromToken(secretKey, token);
            UserContext userContext = userService.getUserContextOrException(userId);
            setAuthentication(userContext);
            chain.doFilter(req, res);

        } catch (Exception e) {
            log.error("Error in JwtTokenFilter while processing the request [{}] [{}] Cause: [{}]", req.getRequestURI(), req.getRemoteAddr(), e.getMessage());
            returnInvalidTokenError(res);
        }
    }

    private void returnInvalidTokenError(HttpServletResponse res) throws IOException {
        ApiResponse<Object> body = ApiResponse.of(INVALID_TOKEN.code(), INVALID_TOKEN.message(), null);
        res.setStatus(UNAUTHORIZED.value());
        res.setCharacterEncoding(UTF_8.name());
        res.setContentType(APPLICATION_JSON_VALUE);
        res.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private boolean isWhitelistRequest(HttpServletRequest req) {
        return whitelistMap.entrySet().stream().anyMatch(it -> {
            RequestMatcher matcher = it.getKey();
            List<HttpMethod> allowedMethods = it.getValue();
            return matcher.matches(req) && allowedMethods.contains(HttpMethod.valueOf(req.getMethod()));
        });
    }

    private void setAuthentication(UserContext userContext) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(userContext, userContext.getPassword(), userContext.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}