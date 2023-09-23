package com.chwihae.client.kakao.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KakaoTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @Builder
    private KakaoTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
