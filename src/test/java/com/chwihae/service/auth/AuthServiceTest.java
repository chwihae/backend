package com.chwihae.service.auth;

import com.chwihae.config.properties.JwtTokenProperties;
import com.chwihae.config.redis.UserContextCacheRepository;
import com.chwihae.config.security.JwtTokenHandler;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.dto.auth.response.LoginResponse;
import com.chwihae.exception.CustomException;
import com.chwihae.infra.test.AbstractMockTest;
import com.chwihae.service.user.UserService;
import io.jsonwebtoken.JwtException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static com.chwihae.exception.CustomExceptionError.INVALID_KAKAO_AUTHORIZATION_CODE;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AuthServiceTest extends AbstractMockTest {

    @InjectMocks
    AuthService authService;

    @Mock
    JwtTokenHandler jwtTokenHandler;

    @Mock
    KakaoAuthHandler kakaoAuthHandler;

    @Mock
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    JwtTokenProperties jwtTokenProperties;

    @Mock
    UserContextCacheRepository userContextCacheRepository;

    @Test
    @DisplayName("유효한 인가 코드로 카카오 인증 요청을 하면 카카오로부터 전달받은 사용자의 이메일을 조회/등록 한 후 사용자 아이디, 토큰을 반환한다")
    void kakaoLogin_withValidCode_returnsUserIdAndToken() throws Exception {
        //given
        String userEmail = "test@email.com";
        Long userId = 1L;
        String token = "token";

        UserEntity userEntity = mock(UserEntity.class);

        given(userEntity.getId())
                .willReturn(userId);

        given(kakaoAuthHandler.getUserEmail(anyString(), anyString()))
                .willReturn(userEmail);

        given(userService.getOrCreateUser(userEmail))
                .willReturn(userEntity);

        given(jwtTokenProperties.getSecretKey())
                .willReturn("secret key");

        given(jwtTokenProperties.getTokenExpiredTimeMs())
                .willReturn(87654321L);

        given(jwtTokenHandler.generateToken(eq(userId), anyString(), anyLong()))
                .willReturn(token);

        //when
        LoginResponse response = authService.kakaoLogin("authorization code", "redirection uri");

        //then
        Assertions.assertThat(response)
                .extracting("userId", "token", "refreshToken")
                .containsExactly(userId, token, token);
    }

    @Test
    @DisplayName("유효하지 않은 인가 코드로 카카오 인증 요청을 하면 예외가 발생한다")
    void kakaoLogin_withInvalidAuthorizationCode_throwsCustomException() throws Exception {
        //given
        given(kakaoAuthHandler.getUserEmail(anyString(), anyString()))
                .willThrow(new CustomException(INVALID_KAKAO_AUTHORIZATION_CODE));

        //when //then
        Assertions.assertThatThrownBy(() -> authService.kakaoLogin("authorization code", "redirection uri"))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(INVALID_KAKAO_AUTHORIZATION_CODE);
    }

    @Test
    @DisplayName("JWT 토큰 생성 실패 시 예외가 발생한다")
    void generateToken_whenJwtTokenGeneratedFailed_throwsJwtException() {
        //given
        String userEmail = "test@email.com";
        Long userId = 1L;
        String secretKey = "secret key";

        UserEntity userEntity = mock(UserEntity.class);

        given(userEntity.getId())
                .willReturn(userId);

        given(kakaoAuthHandler.getUserEmail(anyString(), anyString()))
                .willReturn(userEmail);

        given(userService.getOrCreateUser(userEmail))
                .willReturn(userEntity);

        given(jwtTokenProperties.getSecretKey())
                .willReturn(secretKey);

        given(jwtTokenProperties.getTokenExpiredTimeMs())
                .willReturn(87654321L);

        given(jwtTokenHandler.generateToken(eq(userEntity.getId()), anyString(), anyLong()))
                .willThrow(new JwtException("Token generation failed"));

        //when // then
        Assertions.assertThatThrownBy(() -> authService.kakaoLogin("dummy", "dummy"))
                .isInstanceOf(JwtException.class);
    }
}