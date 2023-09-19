package com.chwihae.client.kakao;

import com.chwihae.client.kakao.response.KakaoUserInfoResponse;
import com.chwihae.exception.CustomException;
import com.chwihae.infra.IntegrationTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;

import static com.chwihae.exception.CustomExceptionError.INVALID_KAKAO_AUTHORIZATION_CODE;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@AutoConfigureWireMock(port = 8089)
@IntegrationTestSupport
class KakaoUserInfoFeignClientTest {

    @Autowired
    KakaoUserInfoFeignClient client;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        reset();
    } // reset all wireMock stubs and scenarios

    @Test
    @DisplayName("유효한 인증 토큰으로 카카오 서버로부터 정상적으로 사용자 이메일을 반환받는다")
    void requestUserEmailSuccessTest() throws Exception {
        //given
        String userEmail = "test@email.com";
        KakaoUserInfoResponse kakaoUserInfoResponse = KakaoUserInfoResponse.builder()
                .kakao_account(KakaoUserInfoResponse.KakaoAccount.builder()
                        .email(userEmail)
                        .build())
                .build();

        stubFor(get(urlPathEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsBytes(kakaoUserInfoResponse))));

        //when
        KakaoUserInfoResponse response = client.requestUserEmail("access token");

        //then
        Assertions.assertThat(response.getKakaoAccount().getEmail()).isEqualTo(userEmail);
    }

    @Test
    @DisplayName("유효하지 않은 인증 토큰으로 요청하면 예외가 발생한다")
    void requestUserEmailWithInvalidAccessTokenFailureTest() throws Exception {
        //given
        String userEmail = "test@email.com";
        String responseBody = null;

        stubFor(get(urlPathEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.UNAUTHORIZED.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        //when //then
        Assertions.assertThatThrownBy(() -> client.requestUserEmail("access token"))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(INVALID_KAKAO_AUTHORIZATION_CODE);
    }
}