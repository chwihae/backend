package com.chwihae.controller.user;

import com.chwihae.dto.user.UserStatisticsResponse;
import com.chwihae.infra.test.AbstractRestDocsTest;
import com.chwihae.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import static com.chwihae.domain.user.UserLevel.DOCTOR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerDocsTest extends AbstractRestDocsTest {

    private final UserService userService = mock(UserService.class);

    @Override
    protected Object initController() {
        return new UserController(userService);
    }

    @Test
    @DisplayName("사용자 활동 API")
    void getUserStatistics_restDocs() throws Exception {
        //given
        given(userService.getUserStatistics(any()))
                .willReturn(UserStatisticsResponse.builder()
                        .level(DOCTOR)
                        .voteCount(120)
                        .commentCount(50)
                        .build());

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/statistics")
                                .header(AUTHORIZATION, token(1L))
                )
                .andExpect(status().isOk())
                .andDo(document("user-statistics",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("[Required] 인증 토큰 (타입: 문자열)")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.level").type(JsonFieldType.STRING).description("유저 레벨 [BACHELOR, MASTER, DOCTOR, PROFESSOR]"),
                                fieldWithPath("data.commentCount").type(JsonFieldType.NUMBER).description("질문 개수"),
                                fieldWithPath("data.voteCount").type(JsonFieldType.NUMBER).description("투표 개수")
                        ))
                );
    }
}