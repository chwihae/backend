package com.chwihae.service.auth;

import com.chwihae.config.properties.JwtTokenProperties;
import com.chwihae.config.security.JwtTokenHandler;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.dto.auth.response.LoginResponse;
import com.chwihae.service.user.UserService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Long REFRESH_TOKEN_MULTIPLIER = 14L;
    private final JwtTokenHandler jwtTokenHandler;
    private final KakaoAuthHandler kakaoAuthHandler;
    private final UserService userService;
    private final UserRepository userRepository;
    private final String secretKey;
    private final Long tokenExpiredTimeMs;

    public AuthService(JwtTokenHandler jwtTokenHandler,
                       KakaoAuthHandler kakaoAuthHandler,
                       UserService userService,
                       UserRepository userRepository,
                       JwtTokenProperties jwtTokenProperties) {
        this.jwtTokenHandler = jwtTokenHandler;
        this.kakaoAuthHandler = kakaoAuthHandler;
        this.userService = userService;
        this.userRepository = userRepository;
        this.secretKey = jwtTokenProperties.getSecretKey();
        this.tokenExpiredTimeMs = jwtTokenProperties.getTokenExpiredTimeMs();
    }

    public LoginResponse kakaoLogin(String authorizationCode, String redirectionUri) {
        String userEmail = kakaoAuthHandler.getUserEmail(authorizationCode, redirectionUri);
        UserEntity userEntity = findOrCreateUser(userEmail);
        String issuedToken = createToken(userEntity.getId());
        String issuedRefreshToken = createRefreshToken(userEntity.getId());

        return LoginResponse.builder()
                .userId(userEntity.getId())
                .token(issuedToken)
                .refreshToken(issuedRefreshToken)
                .build();
    }

    private UserEntity findOrCreateUser(String email) {
        return userRepository.findByEmail(email).orElseGet(() -> userService.createUser(email));
    }

    private String createToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, secretKey, tokenExpiredTimeMs);
    }

    private String createRefreshToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, secretKey, tokenExpiredTimeMs * REFRESH_TOKEN_MULTIPLIER);
    }
}
