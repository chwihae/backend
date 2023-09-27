package com.chwihae.service.auth;

import com.chwihae.config.properties.JwtTokenProperties;
import com.chwihae.config.security.JwtTokenHandler;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.dto.auth.response.LoginResponse;
import com.chwihae.dto.user.UserContext;
import com.chwihae.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private static final Long REFRESH_TOKEN_MULTIPLIER = 14L;
    private final JwtTokenHandler jwtTokenHandler;
    private final KakaoAuthHandler kakaoAuthHandler;
    private final UserService userService;
    private final JwtTokenProperties jwtTokenProperties;

    public LoginResponse kakaoLogin(String authorizationCode, String redirectionUri) {
        String userEmail = kakaoAuthHandler.getUserEmail(authorizationCode, redirectionUri);
        UserEntity userEntity = userService.getOrCreateUser(userEmail);
        String issuedToken = createToken(userEntity.getId());
        String issuedRefreshToken = createRefreshToken(userEntity.getId());
        userService.setUserContext(UserContext.fromEntity(userEntity));
        return LoginResponse.of(userEntity.getId(), userEmail, issuedToken, issuedRefreshToken);
    }

    private String createToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtTokenProperties.getSecretKey(), jwtTokenProperties.getTokenExpiredTimeMs());
    }

    private String createRefreshToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtTokenProperties.getSecretKey(), jwtTokenProperties.getTokenExpiredTimeMs() * REFRESH_TOKEN_MULTIPLIER);
    }
}
