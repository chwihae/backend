package com.chwihae.service.auth;

import com.chwihae.client.kakao.KakaoTokenFeignClient;
import com.chwihae.client.kakao.KakaoUserInfoFeignClient;
import com.chwihae.client.kakao.response.KakaoTokenResponse;
import com.chwihae.client.kakao.response.KakaoUserInfoResponse;
import com.chwihae.config.properties.KakaoProperties;
import com.chwihae.exception.CustomException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.chwihae.exception.CustomExceptionError.INVALID_KAKAO_AUTHORIZATION_CODE;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KakaoAuthHandlerTest {

    @InjectMocks
    KakaoAuthHandler kakaoAuthHandler;

    @Mock
    KakaoTokenFeignClient kakaoTokenFeignClient;

    @Mock
    KakaoUserInfoFeignClient kakaoUserInfoFeignClient;

    @Mock
    KakaoProperties kakaoProperties;

    @Test
    @DisplayName("유효한 인가 코드로 카카오 서버로부터 사용자 이메일을 얻어온다")
    void getUserEmailSuccessTest() throws Exception {
        //given
        given(kakaoProperties.getClientId())
                .willReturn("client id");

        given(kakaoProperties.getClientSecret())
                .willReturn("client secret");

        KakaoTokenResponse kakaoTokenResponse = KakaoTokenResponse.builder()
                .access_token("access token")
                .build();

        given(kakaoTokenFeignClient.requestToken(anyString(), anyString(), anyString(), anyString(), anyString()))
                .willReturn(kakaoTokenResponse);

        String userEmail = "test@email.com";
        KakaoUserInfoResponse kakaoUserInfoResponse = KakaoUserInfoResponse.builder()
                .kakao_account(KakaoUserInfoResponse.KakaoAccount.builder()
                        .email(userEmail)
                        .build())
                .build();

        given(kakaoUserInfoFeignClient.requestUserEmail(anyString()))
                .willReturn(kakaoUserInfoResponse);

        //when
        String email = kakaoAuthHandler.getUserEmail("authorization code", "http://localhost:3000");

        //then
        Assertions.assertThat(email).isEqualTo(userEmail);
    }

    @Test
    @DisplayName("유효하지 않은 인가 코드로 인증 요청을 하면 예외가 발생한다")
    void getUserEmailWithInvalidAuthroizationCodeFailureTest() throws Exception {
        //given
        given(kakaoProperties.getClientId())
                .willReturn("client id");

        given(kakaoProperties.getClientSecret())
                .willReturn("client secret");

        given(kakaoTokenFeignClient.requestToken(anyString(), anyString(), anyString(), anyString(), anyString()))
                .willReturn(null);

        //when //then
        Assertions.assertThatThrownBy(() -> kakaoAuthHandler.getUserEmail("authorization code", "http://localhost:3000"))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(INVALID_KAKAO_AUTHORIZATION_CODE);
    }

}