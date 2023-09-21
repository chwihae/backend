package com.chwihae.controller.question;

import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionStatus;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.vote.VoteEntity;
import com.chwihae.dto.option.request.OptionCreateRequest;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.infra.AbstractMockMvcTest;
import com.chwihae.infra.WithTestUser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.chwihae.domain.question.QuestionStatus.IN_PROGRESS;
import static com.chwihae.exception.CustomExceptionError.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class QuestionControllerTest extends AbstractMockMvcTest {

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
                .andExpect(jsonPath("$.code").value(CREATED.value()))
                .andExpect(jsonPath("$.data.id").isNumber());

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
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(30);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(userEntity, closeAt));

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
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(30);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(userEntity, closeAt));

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

    @Test
    @DisplayName("GET /api/v1/questions/{questionId}/options - 성공 (질문자)")
    @WithTestUser("questioner@email.com")
    void getOptions_byQuestioner_returnSuccessCode() throws Exception {
        //given
        UserEntity questioner = userRepository.findByEmail("questioner@email.com").get();
        UserEntity voter1 = UserEntityFixture.of("voter1@email.com");
        UserEntity voter2 = UserEntityFixture.of("voter2@email.com");
        UserEntity voter3 = UserEntityFixture.of("voter3@email.com");
        userRepository.saveAll(List.of(voter1, voter2, voter3));

        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(30);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option1 = createOption(questionEntity, "name1");
        OptionEntity option2 = createOption(questionEntity, "name2");
        optionRepository.saveAll(List.of(option1, option2));

        VoteEntity vote1 = createVote(voter1, option1);
        VoteEntity vote2 = createVote(voter2, option2);
        VoteEntity vote3 = createVote(voter3, option2);
        voteRepository.saveAll(List.of(vote1, vote2, vote3));

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}/options", questionEntity.getId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canViewVoteResult").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/questions/{questionId}/options - 성공 (투표자)")
    @WithTestUser("voter1@email.com")
    void getOptions_byVoter_returnSuccessCode() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of("questioner@email.com");
        UserEntity voter1 = userRepository.findByEmail("voter1@email.com").get();
        UserEntity voter2 = UserEntityFixture.of("voter2@email.com");
        UserEntity voter3 = UserEntityFixture.of("voter3@email.com");
        userRepository.saveAll(List.of(questioner, voter2, voter3));

        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(30);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option1 = createOption(questionEntity, "name1");
        OptionEntity option2 = createOption(questionEntity, "name2");
        optionRepository.saveAll(List.of(option1, option2));

        VoteEntity vote1 = createVote(voter1, option1);
        VoteEntity vote2 = createVote(voter2, option2);
        VoteEntity vote3 = createVote(voter3, option2);
        voteRepository.saveAll(List.of(vote1, vote2, vote3));

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}/options", questionEntity.getId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canViewVoteResult").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/questions/{questionId}/options - 성공 (마감되지 않은 질문에 투표하지 않은 사용자)")
    @WithTestUser
    void getOptions_byViewer_returnSuccessCode() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of("questioner@email.com");
        UserEntity voter1 = UserEntityFixture.of("voter1@email.com");
        UserEntity voter2 = UserEntityFixture.of("voter2@email.com");
        UserEntity voter3 = UserEntityFixture.of("voter3@email.com");
        userRepository.saveAll(List.of(questioner, voter1, voter2, voter3));

        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(30);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option1 = createOption(questionEntity, "name1");
        OptionEntity option2 = createOption(questionEntity, "name2");
        optionRepository.saveAll(List.of(option1, option2));

        VoteEntity vote1 = createVote(voter1, option1);
        VoteEntity vote2 = createVote(voter2, option2);
        VoteEntity vote3 = createVote(voter3, option2);
        voteRepository.saveAll(List.of(vote1, vote2, vote3));

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}/options", questionEntity.getId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canViewVoteResult").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/questions/{questionId}/options - 성공 (마감된 질문에 투표하지 않은 사용자)")
    @WithTestUser
    void getOptions_byViewer_whenQuestionClosed_returnSuccessCode() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of("questioner@email.com");
        UserEntity voter1 = UserEntityFixture.of("voter1@email.com");
        UserEntity voter2 = UserEntityFixture.of("voter2@email.com");
        UserEntity voter3 = UserEntityFixture.of("voter3@email.com");
        userRepository.saveAll(List.of(questioner, voter1, voter2, voter3));

        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(30);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option1 = createOption(questionEntity, "name1");
        OptionEntity option2 = createOption(questionEntity, "name2");
        optionRepository.saveAll(List.of(option1, option2));

        VoteEntity vote1 = createVote(voter1, option1);
        VoteEntity vote2 = createVote(voter2, option2);
        VoteEntity vote3 = createVote(voter3, option2);
        voteRepository.saveAll(List.of(vote1, vote2, vote3));

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}/options", questionEntity.getId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canViewVoteResult").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/questions/{questionId}/options - 실패 (존재하지 않는 질문)")
    @WithTestUser
    void getOptions_byViewer_whenQuestionNotExists_returnNotFoundCode() throws Exception {
        //given
        long notExistingQuestionId = 0L;
        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}/options", notExistingQuestionId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(QUESTION_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("GET /api/v1/questions/{questionId}/options - 실패 (인증되지 않은 사용자)")
    @WithAnonymousUser
    void getOptions_byUnAuthenticated_returnInvalidTokenCode() throws Exception {
        //given
        long notExistingQuestionId = 0L;

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}/options", notExistingQuestionId)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/questions?type={type}&status={}&page={pageNumber}&size={size} - 성공 (without QuestionStatus 요청)")
    @WithTestUser
    void getQuestions_withoutQuestionStatus_returnsSuccessCode() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("questioner@email.com"));
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(30);
        QuestionEntity question1 = createQuestion(userEntity, closeAt);
        QuestionEntity question2 = createQuestion(userEntity, closeAt);
        QuestionEntity question3 = createQuestion(userEntity, closeAt);
        QuestionEntity question4 = createQuestion(userEntity, closeAt);
        questionRepository.saveAll(List.of(question1, question2, question3, question4));

        final int pageNumber = 0;
        final int pageSize = 2;

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions?page={pageNumber}&size={size}", pageNumber, pageSize)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size").value(pageSize))
                .andExpect(jsonPath("$.data.number").value(pageNumber));
    }

    @Test
    @DisplayName("GET /api/v1/questions?type={type}&status={}&page={pageNumber}&size={size} - 성공 (with QuestionStatus 요청)")
    @WithTestUser
    void getQuestions_withQuestionStatus_returnsSuccessCode() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("questioner@email.com"));

        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(30);
        QuestionEntity question1 = createQuestion(userEntity, closeAt);
        QuestionEntity question2 = createQuestion(userEntity, closeAt);
        QuestionEntity question3 = createQuestion(userEntity, closeAt);
        QuestionEntity question4 = createQuestion(userEntity, closeAt);
        questionRepository.saveAll(List.of(question1, question2, question3, question4));

        QuestionStatus status = IN_PROGRESS;
        final int pageNumber = 0;
        final int pageSize = 2;

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions?status={status}&page={pageNumber}&size={size}", status, pageNumber, pageSize)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size").value(pageSize))
                .andExpect(jsonPath("$.data.number").value(pageNumber));
    }

    @Test
    @DisplayName("GET /api/v1/questions?type={type}&status={}&page={pageNumber}&size={size} - 실패 (with InvalidQuestionStatus 요청)")
    @WithTestUser
    void getQuestions_withInvalidQuestionStatus_returnsSuccessCode() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("questioner@email.com"));

        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(30);
        QuestionEntity question1 = createQuestion(userEntity, closeAt);
        QuestionEntity question2 = createQuestion(userEntity, closeAt);
        QuestionEntity question3 = createQuestion(userEntity, closeAt);
        QuestionEntity question4 = createQuestion(userEntity, closeAt);
        questionRepository.saveAll(List.of(question1, question2, question3, question4));

        String notExistingStatus = "invalid";
        final int pageNumber = 0;
        final int pageSize = 2;

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions?status={status}&page={pageNumber}&size={size}", notExistingStatus, pageNumber, pageSize)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("GET /api/v1/questions?type={type}&status={}&page={pageNumber}&size={size} - 실패 (미인증 사용자)")
    @WithAnonymousUser
    void getQuestions_byAnonymousUser_returnsSuccessCode() throws Exception {
        //given
        QuestionStatus status = IN_PROGRESS;
        final int pageNumber = 0;
        final int pageSize = 2;

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions?status={status}&page={pageNumber}&size={size}", status, pageNumber, pageSize)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/questions/{questionId}/options/{optionId} - 성공")
    @WithTestUser("voter@email.com")
    void createVote_byVoter_returnsSuccessCode() throws Exception {
        //given
        UserEntity questioner = userRepository.save(UserEntityFixture.of("questioner@email.com"));
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(1);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option1 = createOption(questionEntity, "name1");
        OptionEntity option2 = createOption(questionEntity, "name2");
        optionRepository.saveAll(List.of(option1, option2));

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions/{questionId}/options/{optionId}", questionEntity.getId(), option1.getId())
                )
                .andExpect(status().isOk());

        Assertions.assertThat(voteRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("POST /api/v1/questions/{questionId}/options/{optionId} - 실패 (미인증 사용자)")
    @WithAnonymousUser
    void createVote_byAnonymousUser_returnsUnauthorizedCode() throws Exception {
        //given
        long notExistingQuestionId = 0L;
        long notExistingOptionId = 0L;

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions/{questionId}/options/{optionId}", notExistingQuestionId, notExistingOptionId)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/questions/{questionId}/options/{optionId} - 실패 (질문 작성자)")
    @WithTestUser("questioner@email.com")
    void createVote_byQuestioner_returnsUnauthorizedCode() throws Exception {
        //given
        UserEntity questioner = userRepository.findByEmail("questioner@email.com").get();
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(1);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option1 = createOption(questionEntity, "name1");
        OptionEntity option2 = createOption(questionEntity, "name2");
        optionRepository.saveAll(List.of(option1, option2));

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions/{questionId}/options/{optionId}", questionEntity.getId(), option1.getId())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/questions/{questionId}/options/{optionId} - 실패 (이미 투표한 투표자)")
    @WithTestUser("voter@email.com")
    void createVote_byVoterWhoAlreadyVote_returnsDuplicateVoteCode() throws Exception {
        //given
        UserEntity questioner = userRepository.save(UserEntityFixture.of("questioner@email.com"));
        UserEntity voter = userRepository.findByEmail("voter@email.com").get();
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(1);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option1 = createOption(questionEntity, "name1");
        OptionEntity option2 = createOption(questionEntity, "name2");
        optionRepository.saveAll(List.of(option1, option2));

        voteRepository.save(createVote(voter, option1));

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions/{questionId}/options/{optionId}", questionEntity.getId(), option1.getId())
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(DUPLICATE_VOTE.code()));

        Assertions.assertThat(voteRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("POST /api/v1/questions/{questionId}/options/{optionId} - 실패 (마감된 질문)")
    @WithTestUser("voter@email.com")
    void createVote_whenQuestionClosed_returnsQuestionClosedCode() throws Exception {
        //given
        UserEntity questioner = userRepository.save(UserEntityFixture.of("questioner@email.com"));
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        long notExistingOptionId = 0L;

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions/{questionId}/options/{optionId}", questionEntity.getId(), notExistingOptionId)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(QUESTION_CLOSED.code()));
    }

    @Test
    @DisplayName("POST /api/v1/questions/{questionId}/options/{optionId} - 실패 (존재하지 않는 질문)")
    @WithTestUser("voter@email.com")
    void createVote_whenQuestionNotFound_returnsNotFoundCode() throws Exception {
        //given
        long notExistingQuestionId = 0L;
        long notExistingOptionId = 0L;

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions/{questionId}/options/{optionId}", notExistingQuestionId, notExistingOptionId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(QUESTION_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("POST /api/v1/questions/{questionId}/options/{optionId} - 실패 (존재하지 않는 질문)")
    @WithTestUser("voter@email.com")
    void createVote_whenOptionNotFound_returnsNotFoundCode() throws Exception {
        //given
        UserEntity questioner = userRepository.save(UserEntityFixture.of("questioner@email.com"));
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(1);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        long notExistingOptionId = 0L;

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions/{questionId}/options/{optionId}", questionEntity.getId(), notExistingOptionId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(OPTION_NOT_FOUND.code()));
    }

    public QuestionEntity createQuestion(UserEntity userEntity, LocalDateTime closeAt) {
        return QuestionEntity.builder()
                .userEntity(userEntity)
                .title("title")
                .content("content")
                .closeAt(closeAt)
                .type(QuestionType.SPEC)
                .build();
    }

    public OptionEntity createOption(QuestionEntity questionEntity, String name) {
        return OptionEntity.builder()
                .name(name)
                .questionEntity(questionEntity)
                .build();
    }

    public VoteEntity createVote(UserEntity userEntity, OptionEntity optionEntity) {
        return VoteEntity.builder()
                .questionEntity(optionEntity.getQuestionEntity())
                .optionEntity(optionEntity)
                .userEntity(userEntity)
                .build();
    }
}