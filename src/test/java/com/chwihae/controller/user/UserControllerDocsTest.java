package com.chwihae.controller.user;

import com.chwihae.dto.question.response.QuestionListResponse;
import com.chwihae.dto.user.UserQuestionFilterType;
import com.chwihae.dto.user.UserStatisticsResponse;
import com.chwihae.infra.test.AbstractRestDocsTest;
import com.chwihae.service.question.QuestionService;
import com.chwihae.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.chwihae.domain.question.QuestionStatus.IN_PROGRESS;
import static com.chwihae.domain.question.QuestionType.ETC;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerDocsTest extends AbstractRestDocsTest {

    private final UserService userService = mock(UserService.class);
    private final QuestionService questionService = mock(QuestionService.class);

    @Override
    protected Object initController() {
        return new UserController(userService, questionService);
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

    @Test
    @DisplayName("사용자 질문 API")
    void getUserQuestions_restDocs() throws Exception {
        //given
        final int CONTENT_SIZE = 5;
        final int PAGE_NUMBER = 0;
        final int PAGE_SIZE = 2;

        List<QuestionListResponse> content = new ArrayList<>();
        IntStream.range(0, CONTENT_SIZE).forEach(responseIndex -> {
            content.add(
                    QuestionListResponse.builder()
                            .id((long) responseIndex)
                            .status(IN_PROGRESS)
                            .title("question title " + responseIndex)
                            .type(ETC)
                            .build()
            );
        });

        PageRequest pageRequest = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        Page<QuestionListResponse> mockPage = new PageImpl<>(content, pageRequest, CONTENT_SIZE);

        given(questionService.getUserQuestions(any(), any(), any()))
                .willReturn(mockPage);

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/questions?type={type}&page={page}&size={size}", UserQuestionFilterType.ME, PAGE_NUMBER, PAGE_SIZE)
                                .header(AUTHORIZATION, token(1L))
                )
                .andExpect(status().isOk())
                .andDo(document("user-questions",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("[Required] 인증 토큰 (타입: 문자열)")
                        ),
                        queryParameters(
                                parameterWithName("type").description("[Required] 타입 (가능한 값: [ME, BOOKMARKED, VOTED])"),
                                parameterWithName("page").description("[Optional] 페이지 번호 (default: 0)"),
                                parameterWithName("size").description("[Optional] 페이지 사이즈 (default: 10)")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                                fieldWithPath("data.content[]").type(JsonFieldType.ARRAY).description("질문 목록"),
                                fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("질문 아이디"),
                                fieldWithPath("data.content[].title").type(JsonFieldType.STRING).description("질문 제목"),
                                fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("질문 타입 [CAREER, SPEC, COMPANY, ETC]"),
                                fieldWithPath("data.content[].status").type(JsonFieldType.STRING).description("질문 상태 [IN_PROGRESS, COMPLETED]"),
                                fieldWithPath("data.content[].viewCount").type(JsonFieldType.NUMBER).description("질문 조회수"),
                                fieldWithPath("data.content[].commentCount").type(JsonFieldType.NUMBER).description("질문 댓글수"),
                                fieldWithPath("data.content[].bookmarkCount").type(JsonFieldType.NUMBER).description("질문 저장수"),
                                fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수"),
                                fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("총 원소 개수"),
                                fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("data.first").type(JsonFieldType.BOOLEAN).description("처음 페이지 여부"),
                                fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("현재 페이지 크기"),
                                fieldWithPath("data.numberOfElements").type(JsonFieldType.NUMBER).description("현재 페이지 원소 개수"),
                                fieldWithPath("data.empty").type(JsonFieldType.BOOLEAN).description("현재 페이지 원소 존재 여부"),
                                fieldWithPath("data.number").type(JsonFieldType.NUMBER).description("현재 페이지 번호")
                        ))
                );
    }
}