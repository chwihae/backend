package com.chwihae.client.kakao;

import com.chwihae.client.kakao.config.KakaoFeignConfig;
import com.chwihae.client.kakao.request.KakaoTokenRequest;
import com.chwihae.client.kakao.response.KakaoTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "kakao-token-client",
        url = "#{@kakaoOauth2Endpoint}",
        configuration = KakaoFeignConfig.class
)
public interface KakaoTokenFeignClient {

    @PostMapping
    KakaoTokenResponse requestToken(@RequestBody KakaoTokenRequest kakaoTokenRequest);
}
x