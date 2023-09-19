package com.chwihae.service.auth;

import com.chwihae.config.properties.JwtTokenProperties;
import com.chwihae.config.security.JwtTokenHandler;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.dto.auth.response.LoginResponse;
import com.chwihae.exception.CustomException;
import com.chwihae.service.user.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.chwihae.exception.CustomExceptionError.INVALID_KAKAO_AUTHORIZATION_CODE;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceMockTest {

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

    @Test
    @DisplayName("유효한 인가 코드로 카카오 인증 요청을 하면 카카오로부터 전달받은 사용자의 이메일을 조회/등록 한 후 사용자 아이디, 토큰을 반환한다")
    void kakaoLoginSuccessTest() throws Exception {
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
    void kakaoLoginWithInvalidAuthorizationCodeFailureTest() throws Exception {
        //given
        given(kakaoAuthHandler.getUserEmail(anyString(), anyString()))
                .willThrow(new CustomException(INVALID_KAKAO_AUTHORIZATION_CODE));

        //when //then
        Assertions.assertThatThrownBy(() -> authService.kakaoLogin("authorization code", "redirection uri"))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(INVALID_KAKAO_AUTHORIZATION_CODE);
    }

}