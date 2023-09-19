package com.chwihae.dto.auth;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class KakaoLoginRequest {

    @NotEmpty(message = "인가 코드는 필수 값 입니다")
    private String authorizationCode;
}
