package com.chwihae.controller.auth;

import com.chwihae.dto.auth.request.KakaoLoginRequest;
import com.chwihae.infra.AbstractMockMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.chwihae.exception.CustomExceptionError.INVALID_ARGUMENT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends AbstractMockMvcTest {

    @Test
    @DisplayName("POST /api/v1/auth/kakao-login - 실패 (유효하지 않은 파라미터)")
    void kakaoLogin_withInvalidRequestParameter_returnsInvalidArgumentCode() throws Exception {
        //given
        KakaoLoginRequest request = KakaoLoginRequest.builder()
                .authorizationCode("")
                .redirectionUri("")
                .build();

        //when //then
        mockMvc.perform(post("/api/v1/auth/kakao-login")
                        .contentType(APPLICATION_JSON)
                        .content(body(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }
}