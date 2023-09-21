package com.chwihae.controller.auth;

import com.chwihae.dto.auth.request.KakaoLoginRequest;
import com.chwihae.dto.auth.response.LoginResponse;
import com.chwihae.infra.AbstractRestDocsTest;
import com.chwihae.service.auth.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerDocsTest extends AbstractRestDocsTest {

    private final AuthService authService = Mockito.mock(AuthService.class);

    @Override
    protected Object initController() {
        return new AuthController(authService);
    }

    @Test
    @DisplayName("카카오 로그인 API")
    void kakaoLogin_restDocs() throws Exception {
        //given
        String email = "user@email.com";
        Long userId = 1L;
        String token = "token";
        String refreshToken = "refresh token";

        String authorizationCode = "kakao authorization code";
        String redirectionUri = "http://localhost:3000";

        KakaoLoginRequest request = KakaoLoginRequest.builder()
                .authorizationCode(authorizationCode)
                .redirectionUri(redirectionUri)
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .email(email)
                .userId(userId)
                .token(token)
                .refreshToken(refreshToken)
                .build();

        given(authService.kakaoLogin(anyString(), anyString()))
                .willReturn(loginResponse);

        //when //then
        mockMvc.perform(post("/api/v1/auth/kakao-login")
                        .contentType(APPLICATION_JSON)
                        .content(body(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("kakao-login",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("authorizationCode").type(JsonFieldType.STRING).description("카카오 인가 코드"),
                                fieldWithPath("redirectionUri").type(JsonFieldType.STRING).description("리디렉션 URI")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("사용자 아이디"),
                                fieldWithPath("data.token").type(JsonFieldType.STRING).description("인증 토큰"),
                                fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰")
                        ))
                );
    }
}
