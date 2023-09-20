package com.chwihae.controller.user;

import com.chwihae.infra.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerDocsTest extends RestDocsSupport {

    @Override
    protected Object initController() {
        return new UserController();
    }

    @Test
    @DisplayName("사용자 활동 API")
    void getUserStatistics_restDocs() throws Exception {
        //given

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
                                headerWithName(AUTHORIZATION).description("인증 토큰 (타입: 문자열) (필수값)")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.commentCount").type(JsonFieldType.NUMBER).description("질문 개수"),
                                fieldWithPath("data.voteCount").type(JsonFieldType.NUMBER).description("투표 개수")
                        ))
                );
    }
}