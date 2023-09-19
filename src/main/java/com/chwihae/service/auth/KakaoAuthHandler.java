package com.chwihae.service.auth;

import com.chwihae.client.kakao.KakaoTokenFeignClient;
import com.chwihae.client.kakao.KakaoUserInfoFeignClient;
import com.chwihae.client.kakao.request.KakaoTokenRequest;
import com.chwihae.client.kakao.response.KakaoTokenResponse;
import com.chwihae.client.kakao.response.KakaoUserInfoResponse;
import com.chwihae.config.properties.KakaoProperties;
import com.chwihae.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import static com.chwihae.exception.CustomExceptionError.INVALID_KAKAO_AUTHORIZATION_CODE;

@RequiredArgsConstructor
@Component
public class KakaoAuthHandler {

    private static final String BEARER = "Bearer ";
    private static final String GRANT_TYPE = "authorization_code";

    private final KakaoProperties kakaoProperties;
    private final KakaoTokenFeignClient kakaoTokenFeignClient;
    private final KakaoUserInfoFeignClient kakaoUserInfoFeignClient;

    public String getUserEmail(String authorizationCode, String redirectionUri) {
        String token = requestTokenOrException(authorizationCode, redirectionUri);
        return requestEmailOrException(token);
    }

    private String requestTokenOrException(String authorizationCode, String redirectionUri) {
        KakaoTokenRequest request = buildTokenRequest(authorizationCode, redirectionUri);

        return Optional.ofNullable(kakaoTokenFeignClient.requestToken(request))
                .filter(Objects::nonNull)
                .map(KakaoTokenResponse::getAccessToken)
                .filter(Objects::nonNull)
                .orElseThrow(() -> new CustomException(INVALID_KAKAO_AUTHORIZATION_CODE));
    }

    private String requestEmailOrException(String accessToken) {
        MediaType mediaType = new MediaType(MediaType.APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8);

        return Optional.ofNullable(kakaoUserInfoFeignClient.requestUserEmail(BEARER + accessToken, mediaType.toString()))
                .filter(Objects::nonNull)
                .map(KakaoUserInfoResponse::getKakaoAccount)
                .filter(Objects::nonNull)
                .map(KakaoUserInfoResponse.Kakao_account::getEmail)
                .filter(Objects::nonNull)
                .orElseThrow(() -> new CustomException(INVALID_KAKAO_AUTHORIZATION_CODE));
    }

    private KakaoTokenRequest buildTokenRequest(String authorizationCode, String redirectionUri) {
        return KakaoTokenRequest.builder()
                .client_id(kakaoProperties.getClientId())
                .code(authorizationCode)
                .redirect_uri(redirectionUri)
                .grant_type(GRANT_TYPE)
                .build();
    }
}
