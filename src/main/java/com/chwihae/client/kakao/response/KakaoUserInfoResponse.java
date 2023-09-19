package com.chwihae.client.kakao.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class KakaoUserInfoResponse {
    private KakaoAccount kakao_account;

    @Setter
    @Getter
    public static class KakaoAccount {
        private String email;
    }

    public KakaoAccount getKakaoAccount() {
        return this.kakao_account;
    }
}
