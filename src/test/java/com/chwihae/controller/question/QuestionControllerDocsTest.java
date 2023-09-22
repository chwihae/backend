package com.chwihae.controller.question;

import com.chwihae.domain.question.QuestionType;
import com.chwihae.dto.option.request.OptionCreateRequest;
import com.chwihae.dto.option.response.Option;
import com.chwihae.dto.option.response.VoteOptionResponse;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.dto.question.response.QuestionDetailResponse;
import com.chwihae.dto.question.response.QuestionListResponse;
import com.chwihae.infra.AbstractRestDocsTest;
import com.chwihae.service.question.QuestionService;
import com.chwihae.service.vote.VoteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.restdocs.payload.JsonFieldType;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.chwihae.domain.question.QuestionStatus.IN_PROGRESS;
import static com.chwihae.domain.question.QuestionType.COMPANY;
import static com.chwihae.domain.question.QuestionType.SPEC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QuestionControllerDocsTest extends AbstractRestDocsTest {

    private final QuestionService questionService = mock(QuestionService.class);
    private final QuestionValidator questionValidator = mock(QuestionValidator.class);
    private final VoteService voteService = mock(VoteService.class);

    @Override
    protected Object initController() {
        return new QuestionController(questionService, questionValidator, voteService);
    }

    @Test
    @DisplayName("질문 등록 API")
    void createQuestion_restDocs() throws Exception {
        //given
        List<OptionCreateRequest> options = new ArrayList<>();
        for (int optionNumber = 1; optionNumber <= 2; optionNumber++) {
            options.add(OptionCreateRequest.builder()
                    .name("option name " + optionNumber)
                    .build()
            );
        }

        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(30);

        QuestionCreateRequest request = QuestionCreateRequest.builder()
                .title("title")
                .type(SPEC)
                .content("content")
                .closeAt(closeAt)
                .options(options)
                .build();

        given(questionService.createQuestion(any(), any()))
                .willReturn(25L);

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions")
                                .header(AUTHORIZATION, token(1L))
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("question-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("[Required] 인증 토큰 (타입: 문자열)")
                        ),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("질문 제목"),
                                fieldWithPath("type").type(JsonFieldType.STRING).description("질문 타입 (가능한 값: [SPEC, STUDY, COMPANY, ETC])"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("질문 내용"),
                                fieldWithPath("closeAt").type(JsonFieldType.STRING).description("질문 마감 시간 (형식: yyyy-mm-ddThh:mm:ss)"),
                                fieldWithPath("options[]").type(JsonFieldType.ARRAY).description("질문 옵션"),
                                fieldWithPath("options[].name").type(JsonFieldType.STRING).description("옵션 이름")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("질문 아이디")
                        )
                ));
    }

    @Test
    @DisplayName("질문 리스트 조회 API")
    void getQuestions_restDocs() throws Exception {
        //given
        final int contentSize = 5;
        List<QuestionListResponse> content = new ArrayList<>();
        for (int number = 1; number <= contentSize; number++) {
            content.add(
                    QuestionListResponse.builder()
                            .id((long) number)
                            .status(IN_PROGRESS)
                            .title("question title " + number)
                            .type(COMPANY)
                            .build()
            );
        }

        final int pageNumber = 0;
        final int pageSize = 2;
        QuestionType type = COMPANY;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Page<QuestionListResponse> mockPage = new PageImpl<>(content, pageRequest, contentSize);

        given(questionService.getQuestionsByTypeAndStatus(any(), any(), any()))
                .willReturn(mockPage);

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions?type={type}&status={status}&page={page}&size={size}", COMPANY, IN_PROGRESS, pageNumber, pageSize)
                                .header(AUTHORIZATION, token(1L))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("question-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("[Required] 인증 토큰 (타입: 문자열)")
                        ),
                        queryParameters(
                                parameterWithName("type").description("[Optional] 질문 타입 (가능한 값: [SPEC, STUDY, COMPANY, ETC])"),
                                parameterWithName("status").description("[Optional] 질문 상태 (가능한 값: [IN_PROGRESS, COMPLETED])"),
                                parameterWithName("page").description("[Optional] 페이지 번호 (default: 0)"),
                                parameterWithName("size").description("[Optional] 페이지 사이즈 (default: 10)")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                                fieldWithPath("data.content[]").type(JsonFieldType.ARRAY).description("질문 목록"),
                                fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("질문 아이디"),
                                fieldWithPath("data.content[].title").type(JsonFieldType.STRING).description("질문 제목"),
                                fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("질문 타입 [SPEC, STUDY, COMPANY, ETC]"),
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
                        )
                ));
    }

    @Test
    @DisplayName("질문 단건 조회 API")
    void getQuestion_restDocs() throws Exception {
        //given
        QuestionDetailResponse questionDetailResponse = QuestionDetailResponse.builder()
                .id(43L)
                .title("title")
                .content("content")
                .type(COMPANY)
                .status(IN_PROGRESS)
                .closeAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(30))
                .editable(true)
                .build();

        given(questionService.getQuestion(any(), any()))
                .willReturn(questionDetailResponse);

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}", 1L)
                                .header(AUTHORIZATION, token(1L))
                                .contentType(APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("question-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("[Required] 인증 토큰 (타입: 문자열)")
                        ),
                        pathParameters(
                                parameterWithName("questionId").description("[Required] 질문 아이디 (타입: 숫자)")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("질문 아이디"),
                                fieldWithPath("data.title").type(JsonFieldType.STRING).description("질문 제목"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("질문 내용"),
                                fieldWithPath("data.type").type(JsonFieldType.STRING).description("질문 타입 [SPEC, STUDY, COMPANY, ETC]"),
                                fieldWithPath("data.status").type(JsonFieldType.STRING).description("질문 상태 [IN_PROGRESS, COMPLETED]"),
                                fieldWithPath("data.closeAt").type(JsonFieldType.STRING).description("질문 마감 시간 (형식: yyyy-mm-ddThh:mm:ss)"),
                                fieldWithPath("data.editable").type(JsonFieldType.BOOLEAN).description("질문 수정 가능 여부(질문 작성자이면 true, 아니면 false)"),
                                fieldWithPath("data.viewCount").type(JsonFieldType.NUMBER).description("질문 조회 수"),
                                fieldWithPath("data.commentCount").type(JsonFieldType.NUMBER).description("질문에 달린 댓글 수"),
                                fieldWithPath("data.voteCount").type(JsonFieldType.NUMBER).description("질문 투표 수"),
                                fieldWithPath("data.bookmarkCount").type(JsonFieldType.NUMBER).description("질문 저장 수"),
                                fieldWithPath("data.bookmarked").type(JsonFieldType.BOOLEAN).description("질문 저장 여부(질문 저장시 true, 아니면 false)")
                        )
                ));
    }

    @Test
    @DisplayName("질문 옵션 조회 API")
    void getOptions_restDocs() throws Exception {
        //given
        final int optionSize = 5;
        List<Option> options = new ArrayList<>();
        for (int i = 1; i <= optionSize; i++) {
            options.add(Option.builder()
                    .id((long) i)
                    .name("option name " + i)
                    .voteCount((long) i)
                    .build());
        }

        VoteOptionResponse response = VoteOptionResponse.builder()
                .votedOptionId(3L)
                .showVoteCount(true)
                .options(options)
                .build();

        given(voteService.getVoteOptions(any(), any()))
                .willReturn(response);

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}/options", 25L)
                                .header(AUTHORIZATION, token(1L))
                                .contentType(APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("question-options",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("[Required] 인증 토큰 (타입: 문자열)")
                        ),
                        pathParameters(
                                parameterWithName("questionId").description("[Required] 질문 아이디 (타입: 숫자)")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.votedOptionId").type(JsonFieldType.NUMBER).description("사용자가 투표한 옵션 아이디 (투표하지 않았으면 NULL)").optional(),
                                fieldWithPath("data.showVoteCount").type(JsonFieldType.BOOLEAN).description("옵션에 대한 투표 결과 확인 가능 여부"),
                                fieldWithPath("data.options[]").type(JsonFieldType.ARRAY).description("질문 옵션 리스트"),
                                fieldWithPath("data.options[].id").type(JsonFieldType.NUMBER).description("질문 옵션 아이디"),
                                fieldWithPath("data.options[].name").type(JsonFieldType.STRING).description("질문 옵션 이름"),
                                fieldWithPath("data.options[].voteCount").type(JsonFieldType.NUMBER).description("질문 옵션 투표 수 (투표 결과를 볼 수 있으면 숫자, 없으면 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("투표 등록 API")
    void createVote_restDocs() throws Exception {
        //when //then
        mockMvc.perform(
                        post("/api/v1/questions/{questionId}/options/{optionId}", 25L, 100L)
                                .header(AUTHORIZATION, token(1L))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("question-vote-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("[Required] 인증 토큰 (타입: 문자열)")
                        ),
                        pathParameters(
                                parameterWithName("questionId").description("[Required] 질문 아이디 (타입: 숫자)"),
                                parameterWithName("optionId").description("[Required] 옵션 아이디 (타입: 숫자)")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("투표 삭제 API")
    void deleteVote_restDocs() throws Exception {
        //when //then
        mockMvc.perform(
                        delete("/api/v1/questions/{questionId}/options/{optionId}", 25L, 100L)
                                .header(AUTHORIZATION, token(1L))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("question-vote-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("[Required] 인증 토큰 (타입: 문자열)")
                        ),
                        pathParameters(
                                parameterWithName("questionId").description("[Required] 질문 아이디 (타입: 숫자)"),
                                parameterWithName("optionId").description("[Required] 옵션 아이디 (타입: 숫자)")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));
    }
}
