package com.chwihae.client.kakao.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class KakaoTokenResponse {

    private String access_token;

    @Builder
    private KakaoTokenResponse(String access_token) {
        this.access_token = access_token;
    }

    public String getAccessToken() {
        return this.access_token;
    }
}
