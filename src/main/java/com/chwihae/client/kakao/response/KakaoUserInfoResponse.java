package com.chwihae.client.kakao.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class KakaoUserInfoResponse {

    private Kakao_account kakaoAccount;

    @Setter
    @Getter
    public static class Kakao_account {
        private String email;
    }
}
