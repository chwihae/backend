package com.chwihae.config.security;


import com.chwihae.dto.user.UserContext;
import com.chwihae.service.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final String secretKey;
    private final JwtTokenHandler jwtTokenHandler;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        try {
            String token = req.getHeader(HttpHeaders.AUTHORIZATION);

            if (token == null) {
                chain.doFilter(req, res);
                return;
            }

            jwtTokenHandler.verifyToken(secretKey, token);
            Long userId = jwtTokenHandler.getUserIdFromToken(secretKey, token);
            UserContext userContext = userService.getUserContextOrException(userId);
            setAuthentication(userContext);
        } catch (Exception e) {
            chain.doFilter(req, res);
            return;
        }
        chain.doFilter(req, res);
    }

    private void setAuthentication(UserContext userContext) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(userContext, userContext.getPassword(), userContext.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
