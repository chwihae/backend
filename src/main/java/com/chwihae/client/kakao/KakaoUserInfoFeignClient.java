package com.chwihae.client.kakao;

import com.chwihae.client.kakao.config.KakaoFeignConfig;
import com.chwihae.client.kakao.response.KakaoUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "kakao-userinfo-client",
        url = "#{@kakaoUserInfoEndpoint}",
        configuration = KakaoFeignConfig.class
)
public interface KakaoUserInfoFeignClient {

    @GetMapping
    KakaoUserInfoResponse requestUserEmail(@RequestHeader("Authorization") String accessToken,
                                           @RequestHeader("Content-type") String contentType);
}
