package com.chwihae.client.kakao;

import com.chwihae.client.kakao.config.KakaoFeignConfig;
import com.chwihae.client.kakao.response.KakaoUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
        name = "kakao-userinfo-client",
        url = "#{@kakaoUserInfoEndpoint}",
        configuration = KakaoFeignConfig.class
)
public interface KakaoUserInfoFeignClient {

    @GetMapping(produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_FORM_URLENCODED_VALUE)
    KakaoUserInfoResponse requestUserEmail(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("Content-type") String contentType,
            @RequestParam(name = "property_keys") List<String> propertyKeys);
}
