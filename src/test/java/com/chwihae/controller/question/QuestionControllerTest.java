package com.chwihae.controller.question;

import com.chwihae.domain.option.OptionRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.dto.option.request.OptionCreateRequest;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.fixture.UserEntityFixture;
import com.chwihae.infra.MockMvcTestSupport;
import com.chwihae.infra.WithTestUser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.chwihae.exception.CustomExceptionError.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class QuestionControllerTest extends MockMvcTestSupport {

    @Autowired
    OptionRepository optionRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("POST /api/v1/questions - 성공")
    @WithTestUser
    void createQuestion_returnsSuccessCode() throws Exception {
        //given
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(30);

        final int optionSize = 2;
        List<OptionCreateRequest> options = new ArrayList<>();
        for (int optionName = 1; optionName <= optionSize; optionName++) {
            options.add(OptionCreateRequest.builder()
                    .name("option name " + optionName)
                    .build()
            );
        }

        QuestionCreateRequest request = QuestionCreateRequest.builder()
                .title("title")
                .type(QuestionType.SPEC)
                .closeAt(closeAt)
                .content("content")
                .options(options)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions")
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questionId").isNumber());

        Assertions.assertThat(questionRepository.findAll()).hasSize(1);
        Assertions.assertThat(optionRepository.findAll()).hasSize(optionSize);
    }

    @Test
    @DisplayName("POST /api/v1/questions - 실패 (질문 마감 시간이 유효하지 않은 경우)")
    @WithTestUser
    void createQuestion_withInvalidCloseAt_returnsInvalidParameterCode() throws Exception {
        //given
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(1);

        List<OptionCreateRequest> options = new ArrayList<>();
        for (int optionName = 0; optionName < 2; optionName++) {
            options.add(OptionCreateRequest.builder()
                    .name(String.valueOf(optionName))
                    .build()
            );
        }

        QuestionCreateRequest request = QuestionCreateRequest.builder()
                .title("title")
                .type(QuestionType.SPEC)
                .closeAt(closeAt)
                .content("content")
                .options(options)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions")
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("POST /api/v1/questions - 실패 (유효하지 않은 요청 파라미터)")
    @WithTestUser
    void createQuestion_withInvalidParameter_returnsInvalidParameterCode() throws Exception {
        //given
        QuestionCreateRequest request = QuestionCreateRequest.builder()
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions")
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("POST /api/v1/questions - 실패 (유효하지 않은 토큰)")
    @WithAnonymousUser
    void createQuestion_withInvalidToken_returnsInvalidTokenCode() throws Exception {
        //given
        String invalidToken = "invalid token";

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions")
                                .header(AUTHORIZATION, invalidToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/questions - 실패 (인증 토큰 없이)")
    @WithAnonymousUser
    void createQuestion_withoutToken_returnsInvalidTokenCode() throws Exception {
        //when //then
        mockMvc.perform(
                        post("/api/v1/questions")
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/questions/{questionId} - 성공 (질문 작성자)")
    @WithTestUser("questioner@email.com")
    void getQuestion_byQuestioner_returnsSuccessCode() throws Exception {
        //given
        UserEntity userEntity = userRepository.findByEmail("questioner@email.com").get();
        QuestionEntity questionEntity = questionRepository.save(createQuestion(userEntity));

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}", questionEntity.getId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.editable").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/questions/{questionId} - 성공 (질문 조회자)")
    @WithTestUser("viewer@email.com")
    void getQuestion_byViewer_returnsSuccessCode() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("questioner@email.com"));
        QuestionEntity questionEntity = questionRepository.save(createQuestion(userEntity));

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}", questionEntity.getId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.editable").value(false));
    }

    // TODO GET /api/v1/questions/{questionId} - 성공 (북마크한 조회자)

    @Test
    @DisplayName("GET /api/v1/questions/{questionId} - 실패 (미인증 사용자)")
    @WithAnonymousUser
    void getQuestion_byUnauthenticated_returnsInvalidTokenCode() throws Exception {
        //given
        long questionId = 1L;

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}", questionId)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/questions/{questionId} - 실패 (존재하지 않는 질문 아이디)")
    @WithTestUser
    void getQuestion_byUnauthenticated_returnsQuestionNotFoundCode() throws Exception {
        //given
        long notExistingQuestionId = 0L;

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}", notExistingQuestionId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(QUESTION_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("GET /api/v1/questions/{questionId} - 실패 (올바르지 않은 경로 변수)")
    @WithTestUser
    void getQuestion_withInvalidPathVariable_returnsInvalidArgumentCode() throws Exception {
        //given
        String invalidPathVariable = "invalid";

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}", invalidPathVariable)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    public QuestionEntity createQuestion(UserEntity userEntity) {
        return QuestionEntity.builder()
                .userEntity(userEntity)
                .title("title")
                .content("content")
                .closeAt(LocalDateTime.of(2023, 11, 11, 0, 0))
                .type(QuestionType.SPEC)
                .build();
    }
}