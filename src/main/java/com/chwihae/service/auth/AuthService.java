package com.chwihae.service.auth;

import com.chwihae.config.properties.JwtTokenProperties;
import com.chwihae.config.security.JwtTokenHandler;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.dto.auth.response.LoginResponse;
import com.chwihae.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private static final Long REFRESH_TOKEN_MULTIPLIER = 14L;
    private final JwtTokenHandler jwtTokenHandler;
    private final KakaoAuthHandler kakaoAuthHandler;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtTokenProperties jwtTokenProperties;

    public LoginResponse kakaoLogin(String authorizationCode, String redirectionUri) {
        String userEmail = kakaoAuthHandler.getUserEmail(authorizationCode, redirectionUri);
        UserEntity userEntity = findOrCreateUser(userEmail);
        String issuedToken = createToken(userEntity.getId());
        String issuedRefreshToken = createRefreshToken(userEntity.getId());
        return LoginResponse.of(userEntity.getId(), userEmail, issuedToken, issuedRefreshToken);
    }

    private UserEntity findOrCreateUser(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    try {
                        return userService.createUser(email);
                    } catch (DataIntegrityViolationException ex) {
                        return userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalStateException("Unexpected error while retrieving the user with email: " + email, ex));
                    }
                });
    }

    private String createToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtTokenProperties.getSecretKey(), jwtTokenProperties.getTokenExpiredTimeMs());
    }

    private String createRefreshToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtTokenProperties.getSecretKey(), jwtTokenProperties.getTokenExpiredTimeMs() * REFRESH_TOKEN_MULTIPLIER);
    }
}
