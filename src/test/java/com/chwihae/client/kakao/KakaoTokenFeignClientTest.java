package com.chwihae.client.kakao;

import com.chwihae.client.kakao.response.KakaoTokenResponse;
import com.chwihae.exception.CustomException;
import com.chwihae.infra.IntegrationTest;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;

import static com.chwihae.exception.CustomExceptionError.INVALID_KAKAO_AUTHORIZATION_CODE;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@AutoConfigureWireMock(port = 8089)
class KakaoTokenFeignClientTest extends IntegrationTest {

    @BeforeEach
    public void setup() {
        reset();
    } // reset all wireMock stubs and scenarios

    @Test
    @DisplayName("올바른 인가 코드로 카카오 토큰 서버로 요청 하면 엑세스 토큰을 정상적으로 반환받는다")
    void requestToken_returnsToken() throws Exception {
        //given
        KakaoTokenResponse kakaoTokenResponse = KakaoTokenResponse.builder()
                .access_token("token")
                .build();

        String grantType = "grant type";
        String clientId = "client id";
        String clientSecret = "client secret";
        String authorizationCode = "code";
        String uri = "uri";

        stubFor(WireMock.post(urlPathEqualTo("/"))
                .withQueryParam("grant_type", equalTo(grantType))
                .withQueryParam("client_id", equalTo(clientId))
                .withQueryParam("client_secret", equalTo(clientSecret))
                .withQueryParam("code", equalTo(authorizationCode))
                .withQueryParam("redirect_uri", equalTo(uri))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsBytes(kakaoTokenResponse))));

        //when
        KakaoTokenResponse response = kakaoTokenFeignClient.requestToken(grantType,
                clientId,
                clientSecret,
                authorizationCode,
                uri);

        //then
        Assertions.assertThat(response.getAccessToken()).isEqualTo(kakaoTokenResponse.getAccessToken());
    }

    @Test
    @DisplayName("유효하지 않은 인가 코드로 요청하면 예외가 발생한다")
    void requestToken_withInvalidAuthorizationCode_throwsCustomException() throws Exception {
        //given
        String responseBody = null;

        String grantType = "grant type";
        String clientId = "client id";
        String clientSecret = "client secret";
        String authorizationCode = "code";
        String uri = "uri";

        stubFor(WireMock.post(urlPathEqualTo("/"))
                .withQueryParam("grant_type", equalTo(grantType))
                .withQueryParam("client_id", equalTo(clientId))
                .withQueryParam("client_secret", equalTo(clientSecret))
                .withQueryParam("code", equalTo(authorizationCode))
                .withQueryParam("redirect_uri", equalTo(uri))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.UNAUTHORIZED.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        //when //then
        Assertions.assertThatThrownBy(() -> kakaoTokenFeignClient.requestToken(grantType,
                        clientId,
                        clientSecret,
                        authorizationCode,
                        uri))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(INVALID_KAKAO_AUTHORIZATION_CODE);
    }
}