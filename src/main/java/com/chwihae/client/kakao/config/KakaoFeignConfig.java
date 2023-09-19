package com.chwihae.client.kakao.config;

import com.chwihae.client.kakao.decoder.KakaoFeignErrorDecoder;
import com.chwihae.config.properties.KakaoProperties;
import feign.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class KakaoFeignConfig {

    private final KakaoProperties kakaoProperties;

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public KakaoFeignErrorDecoder kakaoFeignErrorDecoder() {
        return new KakaoFeignErrorDecoder();
    }

    @Bean(name = "kakaoOauth2Endpoint")
    public String kakaoOauth2Endpoint() {
        return kakaoProperties.getOauth2().getEndpoint();
    }

    @Bean(name = "kakaoUserInfoEndpoint")
    public String kakaoUserInfoEndpoint() {
        return kakaoProperties.getUserInfo().getEndpoint();
    }
}
