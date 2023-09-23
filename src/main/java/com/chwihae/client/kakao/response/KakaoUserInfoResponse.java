package com.chwihae.client.kakao.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KakaoUserInfoResponse {

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Builder
    private KakaoUserInfoResponse(KakaoAccount kakaoAccount) {
        this.kakaoAccount = kakaoAccount;
    }

    @Data
    @NoArgsConstructor
    public static class KakaoAccount {
        private String email;

        @Builder
        private KakaoAccount(String email) {
            this.email = email;
        }
    }
}
