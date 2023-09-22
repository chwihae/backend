package com.chwihae.dto.auth.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResponse {

    private Long userId;
    private String email;
    private String token;
    private String refreshToken;

    public static LoginResponse of(Long userId, String email, String token, String refreshToken) {
        return LoginResponse.builder()
                .userId(userId)
                .email(email)
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }
    
    @Builder
    private LoginResponse(Long userId, String email, String token, String refreshToken) {
        this.email = email;
        this.userId = userId;
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
