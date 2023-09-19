package com.chwihae.client.kakao;

import com.chwihae.client.kakao.config.KakaoFeignConfig;
import com.chwihae.client.kakao.response.KakaoTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
        name = "kakao-token-client",
        url = "#{@kakaoOauth2Endpoint}",
        configuration = KakaoFeignConfig.class
)
public interface KakaoTokenFeignClient {

    @PostMapping(consumes = APPLICATION_FORM_URLENCODED_VALUE,
            produces = APPLICATION_JSON_VALUE)
    KakaoTokenResponse requestToken(@RequestParam("grant_type") String grantType,
                                    @RequestParam("client_id") String clientId,
                                    @RequestParam("client_secret") String clientSecret,
                                    @RequestParam("redirect_uri") String redirectUri,
                                    @RequestParam("code") String code);
}