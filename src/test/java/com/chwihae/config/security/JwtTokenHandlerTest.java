package com.chwihae.config.security;

import io.jsonwebtoken.JwtException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenHandlerTest {

    @Test
    @DisplayName("유효한 토큰에서 userId를 추출한다")
    void extractUserIdFromToken_returnUserId() {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();

        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        long originalUserId = 123L;
        long expiredTimeMs = 86400000L;

        String token = jwtTokenHandler.generateToken(originalUserId, secretKey, expiredTimeMs);

        //when
        Long userId = jwtTokenHandler.getUserIdFromToken(secretKey, token);

        //then
        Assertions.assertThat(userId).isEqualTo(originalUserId);
    }

    @Test
    @DisplayName("유효하지 않은 토큰에서 userId를 추출할 때 예외가 발생한다")
    void extractUserIdFromToken_withInvalidToken_returnsJwtException() {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();

        String invalidToken = "invalidToken";
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        //when //then
        Assertions.assertThatThrownBy(() -> {
                    jwtTokenHandler.getUserIdFromToken(secretKey, invalidToken);
                })
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("유효한 토큰을 검증한다")
    void validateToken() throws Exception {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();

        long originalUserId = 123L;
        long expiredTimeMs = 86400000L;
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        String token = jwtTokenHandler.generateToken(originalUserId, secretKey, expiredTimeMs);

        //when //then
        jwtTokenHandler.verifyToken(secretKey, token);
    }

    @Test
    @DisplayName("유효하지 않은 토큰을 검증하면 예외가 발생한다")
    void verifyToken_withInvalidToken_returnsJwtException() throws Exception {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();

        String invalidToken = "invalid token";
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        //when //then
        Assertions.assertThatThrownBy(() -> jwtTokenHandler.verifyToken(secretKey, invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("null을 검증하면 JwtException 예외가 발생한다")
    void verifyToken_WithNull_returnsJwtException() throws Exception {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();

        String nullToken = null;
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        //when //then
        Assertions.assertThatThrownBy(() -> jwtTokenHandler.verifyToken(secretKey, nullToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("만료된 JWT 토큰 검증시 JwtException 예외가 발생한다")
    void verifyToken_withExpiredToken_returnsJwtException() {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();

        long userId = 1L;
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        long expiredTimeMs = -1000L;

        String expiredToken = jwtTokenHandler.generateToken(userId, secretKey, expiredTimeMs);

        //when //then
        Assertions.assertThatThrownBy(() -> jwtTokenHandler.verifyToken(secretKey, expiredToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("userId로 토큰을 생성한다")
    void generateToken_returnsToken() {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();

        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        long expiredTimeMs = 86400000L;
        long userId = 123L;

        String tokenPattern = "^[a-zA-Z0-9-_]+(=)*$";

        //when
        String token = jwtTokenHandler.generateToken(userId, secretKey, expiredTimeMs);

        //then
        Assertions.assertThat(token)
                .isNotNull()
                .satisfies(t -> {
                    Assertions.assertThat(t.split("\\."))
                            .hasSize(3)
                            .allMatch(part -> part.matches(tokenPattern));  // verify jwt format
                });
    }

    @Test
    @DisplayName("유효한 userId로 토큰을 생성하고 그 토큰으로 userId를 정상적으로 추출한다")
    void generateAndExtractToken_returnsUserId() {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();

        long originalUserId = 123L;
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        long expiredTimeMs = 86400000L;

        String token = jwtTokenHandler.generateToken(originalUserId, secretKey, expiredTimeMs);

        //when
        Long extractedUserId = jwtTokenHandler.getUserIdFromToken(secretKey, token);

        //then
        Assertions.assertThat(extractedUserId).isEqualTo(originalUserId);
    }

    @Test
    @DisplayName("유효하지 않는 토큰으로 userId를 추출하려고 하면 JwtException이 발생한다")
    void extractUserId_withInvalidToken_returnsJwtException() throws Exception {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();

        String invalidToken = "-1";
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        //when //then
        Assertions.assertThatThrownBy(() -> jwtTokenHandler.getUserIdFromToken(secretKey, invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("만료된 토큰으로 userId를 추출하려고 하면 JwtException이 발생한다")
    void extractUserId_withExpiredToken_returnsJwtException() throws Exception {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();

        long originalUserId = 123L;
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        String expiredToken = jwtTokenHandler.generateToken(originalUserId, secretKey, -1000L);

        //when //then
        Assertions.assertThatThrownBy(() -> jwtTokenHandler.getUserIdFromToken(secretKey, expiredToken))
                .isInstanceOf(JwtException.class);
    }
}