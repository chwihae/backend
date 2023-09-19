package com.chwihae.dto.auth.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class KakaoLoginRequest {

    @NotEmpty
    private String authorizationCode;

    @NotEmpty
    private String redirectionUri;
}
