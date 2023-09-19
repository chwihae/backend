package com.chwihae.client.kakao.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class KakaoTokenRequest {
    private String grant_type;
    private String client_id;
    private String redirect_uri;
    private String code;

    @Builder
    private KakaoTokenRequest(String grant_type, String client_id, String redirect_uri, String code) {
        this.grant_type = grant_type;
        this.client_id = client_id;
        this.redirect_uri = redirect_uri;
        this.code = code;
    }
}
