package com.chwihae.client.kakao.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class KakaoUserInfoResponse {
    private KakaoAccount kakao_account;

    @Builder
    private KakaoUserInfoResponse(KakaoAccount kakao_account) {
        this.kakao_account = kakao_account;
    }

    @NoArgsConstructor
    @Setter
    @Getter
    public static class KakaoAccount {
        private String email;

        @Builder
        private KakaoAccount(String email) {
            this.email = email;
        }
    }

    public KakaoAccount getKakaoAccount() {
        return this.kakao_account;
    }
}
