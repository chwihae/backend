package com.chwihae.dto.auth.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResponse {

    private Long userId;
    private String token;
    private String refreshToken;

    @Builder
    public LoginResponse(Long userId, String token, String refreshToken) {
        this.userId = userId;
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
