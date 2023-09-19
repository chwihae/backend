package com.chwihae.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResponse {

    private User user;
    private Token token;

    @Builder
    public LoginResponse(Long userId, String token, String refreshToken) {
        this.user = new User(userId);
        this.token = new Token(token, refreshToken);
    }

    @AllArgsConstructor
    @Getter
    public static class User {
        private Long id;
    }

    @AllArgsConstructor
    @Getter
    public static class Token {
        private String token;
        private String refreshToken;
    }
}
