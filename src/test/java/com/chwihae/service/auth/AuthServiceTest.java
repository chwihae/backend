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
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.chwihae.exception.CustomExceptionError.INVALID_KAKAO_AUTHORIZATION_CODE;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;

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

        UserEntity userEntity = UserEntity.builder()
                .email(userEmail)
                .build();

        ReflectionTestUtils.setField(userEntity, "id", userId);

        given(jwtTokenProperties.getSecretKey())
                .willReturn("secret key");

        given(jwtTokenProperties.getTokenExpiredTimeMs())
                .willReturn(87654321L);

        given(kakaoAuthHandler.getUserEmail(anyString(), anyString()))
                .willReturn(userEmail);

        given(userRepository.findByEmail(userEmail))
                .willReturn(Optional.of(userEntity));

        given(jwtTokenHandler.generateToken(anyLong(), anyString(), anyLong()))
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
    @DisplayName("존재하지 않는 이메일로 로그인 시도 시 새로운 사용자를 생성한다")
    void kakaoLogin_withNotExistingEmail() {
        //given
        String redirectionUri = "redirection uri";
        String accessToken = "dummy access token";

        given(jwtTokenProperties.getSecretKey())
                .willReturn("secret key");

        given(jwtTokenProperties.getTokenExpiredTimeMs())
                .willReturn(87654321L);

        given(kakaoAuthHandler.getUserEmail(anyString(), anyString()))
                .willReturn("nonexisting@email.com");

        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());

        given(userService.createUser(anyString()))
                .willReturn(UserEntity.builder()
                        .email("nonexisting@email.com")
                        .build());

        //when
        authService.kakaoLogin(accessToken, redirectionUri);

        //then
        Mockito.verify(userService, times(1)).createUser(anyString());
    }

    @Test
    @DisplayName("JWT 토큰 생성 실패 시 예외가 발생한다")
    void generateToken_whenJwtTokenGeneratedFailed_throwsJwtException() {
        //given
        String secretKey = "secret key";
        given(jwtTokenProperties.getSecretKey())
                .willReturn(secretKey);

        given(jwtTokenProperties.getTokenExpiredTimeMs())
                .willReturn(87654321L);

        given(kakaoAuthHandler.getUserEmail(anyString(), anyString()))
                .willReturn("nonexisting@email.com");

        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());

        UserEntity userEntity = UserEntity.builder()
                .email("nonexisting@email.com")
                .build();

        ReflectionTestUtils.setField(userEntity, "id", 1L);

        given(userService.createUser(anyString()))
                .willReturn(userEntity);

        given(jwtTokenHandler.generateToken(eq(userEntity.getId()), anyString(), anyLong()))
                .willThrow(new JwtException("Token generation failed"));

        //when // then
        Assertions.assertThatThrownBy(() -> authService.kakaoLogin("dummy", "dummy"))
                .isInstanceOf(JwtException.class);
    }
}