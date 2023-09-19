package com.chwihae.controller.auth;

import com.chwihae.controller.ApiResponse;
import com.chwihae.dto.auth.request.KakaoLoginRequest;
import com.chwihae.dto.auth.response.LoginResponse;
import com.chwihae.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/kakao-login")
    public ApiResponse<LoginResponse> kakaoLogin(@RequestBody @Validated KakaoLoginRequest request) {
        return ApiResponse.ok(authService.kakaoLogin(request.getAuthorizationCode(), request.getRedirectionUri()));
    }
}
