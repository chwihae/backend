package com.chwihae.controller.auth;

import com.chwihae.controller.ApiResponse;
import com.chwihae.dto.auth.request.KakaoLoginRequest;
import com.chwihae.dto.auth.response.LoginResponse;
import com.chwihae.exception.CustomException;
import com.chwihae.service.auth.AuthService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.chwihae.exception.CustomExceptionError.INVALID_KAKAO_AUTHORIZATION_CODE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthControllerMockTest {

    @InjectMocks
    AuthController authController;

    @Mock
    AuthService authService;

    @Test
    @DisplayName("POST /api/v1/auth/kakao-login - 성공")
    void kakaoLogin_returnSuccessCode() throws Exception {
        //given
        KakaoLoginRequest request = KakaoLoginRequest.builder()
                .authorizationCode("authorization code")
                .redirectionUri("redirection uri")
                .build();

        Long userId = 1L;
        String token = "token";
        String refreshToken = "refresh token";

        LoginResponse loginResponse = LoginResponse.builder()
                .userId(userId)
                .token(token)
                .refreshToken(refreshToken)
                .build();

        given(authService.kakaoLogin(anyString(), anyString()))
                .willReturn(loginResponse);

        //when
        ApiResponse<LoginResponse> response = authController.kakaoLogin(request);

        //then
        Assertions.assertThat(response.getData())
                .extracting("userId", "token", "refreshToken")
                .containsExactly(userId, token, refreshToken);
    }

    @Test
    @DisplayName("POST /api/v1/auth/kakao-login - 실패 (유효하지 않은 인가 코드)")
    void kakaoLogin_withInvalidAuthorizationCode_returnInvalidKakaoAuthorizationCode() throws Exception {
        //given
        KakaoLoginRequest request = KakaoLoginRequest.builder()
                .authorizationCode("authorization code")
                .redirectionUri("redirection uri")
                .build();

        given(authService.kakaoLogin(anyString(), anyString()))
                .willThrow(new CustomException(INVALID_KAKAO_AUTHORIZATION_CODE));

        //when //then
        Assertions.assertThatThrownBy(() -> authController.kakaoLogin(request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(INVALID_KAKAO_AUTHORIZATION_CODE);
    }
}