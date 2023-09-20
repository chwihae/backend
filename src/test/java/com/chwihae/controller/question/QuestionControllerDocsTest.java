package com.chwihae.controller.question;

import com.chwihae.domain.question.QuestionType;
import com.chwihae.dto.option.request.OptionCreateRequest;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.dto.question.response.QuestionCreateResponse;
import com.chwihae.infra.RestDocsSupport;
import com.chwihae.infra.WithTestUser;
import com.chwihae.service.question.QuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class QuestionControllerDocsTest extends RestDocsSupport {

    private final QuestionService questionService = mock(QuestionService.class);
    private final QuestionValidator questionValidator = mock(QuestionValidator.class);

    @Override
    protected Object initController() {
        return new QuestionController(questionService, questionValidator);
    }

    @Test
    @DisplayName("질문 등록 API")
    @WithTestUser
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
                .type(QuestionType.SPEC)
                .content("content")
                .closeAt(closeAt)
                .options(options)
                .build();

        QuestionCreateResponse questionCreateResponse = QuestionCreateResponse.builder()
                .questionId(25L)
                .build();

        given(questionService.createQuestionWithOptions(any(), any()))
                .willReturn(questionCreateResponse);

        //when //then
        mockMvc.perform(post("/api/v1/questions")
                        .header(AUTHORIZATION, token(1L))
                        .contentType(APPLICATION_JSON)
                        .content(body(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("question-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("질문 제목"),
                                fieldWithPath("type").type(JsonFieldType.STRING).description("질문 타입, 가능한 값 [SPEC, STUDY, COMPANY, ETC]"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("질문 내용"),
                                fieldWithPath("closeAt").type(JsonFieldType.STRING).description("질문 마감 시간"),
                                fieldWithPath("options[]").type(JsonFieldType.ARRAY).description("질문 옵션"),
                                fieldWithPath("options[].name").type(JsonFieldType.STRING).description("옵션 이름")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.questionId").type(JsonFieldType.NUMBER).description("생성된 질문 아이디")
                        )
                ));
    }
}
