package com.chwihae.controller.user;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.fixture.QuestionEntityFixture;
import com.chwihae.infra.fixture.QuestionViewFixture;
import com.chwihae.infra.support.WithTestUser;
import com.chwihae.infra.test.AbstractMockMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.chwihae.dto.user.UserQuestionFilterType.ME;
import static com.chwihae.exception.CustomExceptionError.INVALID_ARGUMENT;
import static com.chwihae.exception.CustomExceptionError.INVALID_TOKEN;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class UserControllerTest extends AbstractMockMvcTest {

    @Test
    @DisplayName("GET /api/v1/users/statistics - 성공")
    @WithTestUser
    void getUserStatistics_returnsSuccessCode() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/api/v1/users/statistics")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/users/statistics - 실패 (미인증 사용자)")
    @WithAnonymousUser
    void getUserStatistics_returnsInvalidCode() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/api/v1/users/statistics")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/users/questions?type={type} - 성공(type = ME)")
    @WithTestUser("questioner@email.com")
    void getUserQuestions_returnsSuccessCode() throws Exception {
        //given
        final int PAGE_SIZE = 5;
        final int PAGE_NUMBER = 0;

        UserEntity userEntity = userRepository.findByEmail("questioner@email.com").get();

        QuestionEntity question1 = QuestionEntityFixture.of(userEntity);
        QuestionEntity question2 = QuestionEntityFixture.of(userEntity);
        QuestionEntity question3 = QuestionEntityFixture.of(userEntity);
        QuestionEntity question4 = QuestionEntityFixture.of(userEntity);
        questionRepository.saveAll(List.of(question1, question2, question3, question4));

        QuestionViewEntity view1 = QuestionViewFixture.of(question1);
        QuestionViewEntity view2 = QuestionViewFixture.of(question2);
        QuestionViewEntity view3 = QuestionViewFixture.of(question3);
        QuestionViewEntity view4 = QuestionViewFixture.of(question4);
        questionViewRepository.saveAll(List.of(view1, view2, view3, view4));

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/questions")
                                .queryParam("type", ME.name())
                                .queryParam("page", String.valueOf(PAGE_NUMBER))
                                .queryParam("size", String.valueOf(PAGE_SIZE))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.size()").value(4));
    }

    @Test
    @DisplayName("GET /api/v1/users/questions?type={type} - 실패 (올바르지 않은 타입)")
    @WithTestUser
    void getUserQuestions_withInvalidType_returnsInvalidArgumentCode() throws Exception {
        //given
        final int PAGE_SIZE = 5;
        final int PAGE_NUMBER = 0;
        String invalidType = "invalid type";

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/questions")
                                .queryParam("type", invalidType)
                                .queryParam("page", String.valueOf(PAGE_NUMBER))
                                .queryParam("size", String.valueOf(PAGE_SIZE))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("GET /api/v1/users/questions?type={type} - 실패 (미인증 사용자)")
    @WithAnonymousUser
    void getUserQuestions_byAnonymousUser_returnsInvalidTokenCode() throws Exception {
        //given
        final int PAGE_SIZE = 5;
        final int PAGE_NUMBER = 0;
        String invalidType = "invalid type";

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/questions")
                                .queryParam("type", invalidType)
                                .queryParam("page", String.valueOf(PAGE_NUMBER))
                                .queryParam("size", String.valueOf(PAGE_SIZE))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    public QuestionEntity createQuestion(UserEntity userEntity) {
        return QuestionEntity.builder()
                .userEntity(userEntity)
                .title("title")
                .content("content")
                .closeAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusHours(1))
                .type(QuestionType.ETC)
                .build();
    }
}