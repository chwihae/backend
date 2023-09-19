package com.chwihae.controller.auth;

import com.chwihae.dto.auth.KakaoLoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @PostMapping("/kakao-login")
    public ResponseEntity<Void> kakaoLogin(@RequestBody @Validated KakaoLoginRequest request) {
        return new ResponseEntity(HttpStatus.OK);
    }
}
