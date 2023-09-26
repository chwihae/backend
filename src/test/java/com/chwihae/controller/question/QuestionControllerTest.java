package com.chwihae.controller.question;

import com.chwihae.domain.bookmark.BookmarkEntity;
import com.chwihae.domain.comment.CommentEntity;
import com.chwihae.domain.commenter.CommenterAliasEntity;
import com.chwihae.domain.commenter.CommenterAliasPrefix;
import com.chwihae.domain.commenter.CommenterSequenceEntity;
import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionStatus;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.vote.VoteEntity;
import com.chwihae.dto.comment.request.QuestionCommentRequest;
import com.chwihae.dto.option.request.OptionCreateRequest;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.infra.fixture.CommentEntityFixture;
import com.chwihae.infra.fixture.QuestionEntityFixture;
import com.chwihae.infra.fixture.QuestionViewFixture;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.infra.support.WithTestUser;
import com.chwihae.infra.test.AbstractMockMvcTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.chwihae.domain.question.QuestionStatus.IN_PROGRESS;
import static com.chwihae.exception.CustomExceptionError.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        questionViewRepository.save(createQuestionView(questionEntity));

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
        questionViewRepository.save(createQuestionView(questionEntity));

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}", questionEntity.getId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.editable").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/questions/{questionId} - 성공 (북마크한 조회자)")
    @WithTestUser("viewer@email.com")
    void getQuestion_byBookmarkUser_returnsSuccessCode() throws Exception {
        //given
        UserEntity questioner = userRepository.save(UserEntityFixture.of("questioner@email.com"));
        UserEntity viewer = userRepository.findByEmail("viewer@email.com").get();
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(1);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        bookmarkRepository.save(createBookmark(viewer, questionEntity));
        questionViewRepository.save(createQuestionView(questionEntity));

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}", questionEntity.getId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookmarked").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/questions/{questionId}/bookmark - 성공 (북마크 저장)")
    @WithTestUser
    void bookmark_whenBookmarked_returnSuccessCode() throws Exception {
        //given
        UserEntity questioner = userRepository.save(UserEntityFixture.of());
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(1);
        QuestionEntity question = questionRepository.save(createQuestion(questioner, closeAt));

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions/{questionId}/bookmark", question.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result").value(true));

        Assertions.assertThat(bookmarkRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("POST /api/v1/questions/{questionId}/bookmark - 성공 (북마크 해제)")
    @WithTestUser("viewer@email.com")
    void bookmark_whenUnBookmarked_returnSuccessCode() throws Exception {
        //given
        UserEntity questioner = userRepository.save(UserEntityFixture.of());
        UserEntity userEntity = userRepository.findByEmail("viewer@email.com").get();
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(1);
        QuestionEntity question = questionRepository.save(createQuestion(questioner, closeAt));
        bookmarkRepository.save(createBookmark(userEntity, question));

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions/{questionId}/bookmark", question.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result").value(false));

        Assertions.assertThat(bookmarkRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("POST /api/v1/questions/{questionId}/bookmark - 실패 (질문 작성자)")
    @WithTestUser("questioner@email.com")
    void bookmark_byQuestioner_returnForbiddenCode() throws Exception {
        //given
        UserEntity questioner = userRepository.findByEmail("questioner@email.com").get();
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(1);
        QuestionEntity question = questionRepository.save(createQuestion(questioner, closeAt));

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions/{questionId}/bookmark", question.getId())
                )
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/questions/{questionId}/bookmark - 실패 (미인증 사용자)")
    @WithAnonymousUser
    void bookmark_byAnonymousUser_returnInvalidTokenCode() throws Exception {
        //given
        long notExistingQuestionId = 0L;

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions/{questionId}/bookmark", notExistingQuestionId)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/questions/{questionId} - 실패 (미인증 사용자)")
    @WithAnonymousUser
    void getQuestion_byAnonymousUser__returnsInvalidTokenCode() throws Exception {
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
                .andExpect(jsonPath("$.data.showVoteCount").value(true));
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
                .andExpect(jsonPath("$.data.showVoteCount").value(true));
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
                .andExpect(jsonPath("$.data.showVoteCount").value(false));
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
                .andExpect(jsonPath("$.data.showVoteCount").value(true));
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
    @DisplayName("GET /api/v1/questions?type={type}&status={status}&page={pageNumber}&size={size} - 성공 (without QuestionStatus 요청)")
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

        QuestionViewEntity view1 = questionViewRepository.save(QuestionViewFixture.of(question1));
        QuestionViewEntity view2 = questionViewRepository.save(QuestionViewFixture.of(question2));
        QuestionViewEntity view3 = questionViewRepository.save(QuestionViewFixture.of(question3));
        QuestionViewEntity view4 = questionViewRepository.save(QuestionViewFixture.of(question4));
        questionViewRepository.saveAll(List.of(view1, view2, view3, view4));

        final int pageNumber = 0;
        final int pageSize = 4;

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
    @DisplayName("GET /api/v1/questions?type={type}&status={status}&page={pageNumber}&size={size} - 성공 (with QuestionStatus 요청)")
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

        QuestionViewEntity view1 = QuestionViewFixture.of(question1);
        QuestionViewEntity view2 = QuestionViewFixture.of(question2);
        QuestionViewEntity view3 = QuestionViewFixture.of(question3);
        QuestionViewEntity view4 = QuestionViewFixture.of(question4);
        questionViewRepository.saveAll(List.of(view1, view2, view3, view4));

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
    @DisplayName("GET /api/v1/questions?type={type}&status={status}&page={pageNumber}&size={size} - 실패 (with InvalidQuestionStatus 요청)")
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
    @DisplayName("GET /api/v1/questions?type={type}&status={status}&page={pageNumber}&size={size} - 실패 (미인증 사용자)")
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

    @Test
    @DisplayName("DELETE /api/v1/questions/{questionId}/options/{optionId} - 성공")
    @WithTestUser("voter@email.com")
    void deleteVote_returnsSuccessCode() throws Exception {
        //given
        UserEntity questioner = userRepository.save(UserEntityFixture.of("questioner@email.com"));
        UserEntity voter = userRepository.findByEmail("voter@email.com").get();
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(1);
        QuestionEntity question = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option = optionRepository.save(createOption(question, "name"));
        voteRepository.save(createVote(voter, option));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/questions/{questionId}/options/{optionId}", question.getId(), option.getId())
                )
                .andExpect(status().isOk());

        Assertions.assertThat(voteRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/questions/{questionId}/options/{optionId} - 실패 (투표하지 않았을 경우)")
    @WithTestUser("voter@email.com")
    void deleteVote_whenNotVote_returnsNotFoundCode() throws Exception {
        //given
        UserEntity questioner = userRepository.save(UserEntityFixture.of("questioner@email.com"));
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(1);
        QuestionEntity question = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option = optionRepository.save(createOption(question, "name"));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/questions/{questionId}/options/{optionId}", question.getId(), option.getId())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(VOTE_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/questions/{questionId}/options/{optionId} - 실패 (미인증 사용자)")
    @WithAnonymousUser
    void deleteVote_byAnonymousUser_returnsUnauthorizedCode() throws Exception {
        //given
        long notExistingQuestionId = 0L;
        long notExistingOptionId = 0L;
        //when //then
        mockMvc.perform(
                        delete("/api/v1/questions/{questionId}/options/{optionId}", notExistingQuestionId, notExistingOptionId)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/questions/{questionId}/comments - 성공")
    @WithTestUser
    void createComment_returnSuccessCode() throws Exception {
        //given
        UserEntity questioner = userRepository.save(UserEntityFixture.of("questioner@email.com"));
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        commenterSequenceRepository.save(createSequence(questionEntity));

        QuestionCommentRequest request = QuestionCommentRequest.builder()
                .content("content")
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions/{questionId}/comments", questionEntity.getId())
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isOk());

        Assertions.assertThat(commentRepository.findAll()).hasSize(1);
        Assertions.assertThat(commenterAliasRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("POST /api/v1/questions/{questionId}/comments - 실패 (올바르지 않은 파라미터)")
    @WithTestUser
    void createComment_withInvalidParameter_returnInvalidArgumentCode() throws Exception {
        //given
        long notExistingQuestionId = 0L;
        QuestionCommentRequest request = QuestionCommentRequest.builder()
                .content("")
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions/{questionId}/comments", notExistingQuestionId)
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("POST /api/v1/questions/{questionId}/comments - 실패 (존재하지 않는 질문)")
    @WithTestUser
    void createComment_withNotExistingQuestionId_returnNotFoundCode() throws Exception {
        //given
        long notExistingQuestionId = 0L;
        QuestionCommentRequest request = QuestionCommentRequest.builder()
                .content("content")
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions/{questionId}/comments", notExistingQuestionId)
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(QUESTION_NOT_FOUND.code()));
    }

    @Test
    @DisplayName(" POST /api/v1/questions/{questionId}/comments - 실패 (미인증 사용자)")
    @WithAnonymousUser
    void createComment_byWhoNotAuthenticated_returnNotFoundCode() throws Exception {
        //given
        long notExistingQuestionId = 0L;
        QuestionCommentRequest request = QuestionCommentRequest.builder()
                .content("content")
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions/{questionId}/comments", notExistingQuestionId)
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/questions/{questionId}/comments - 성공")
    @WithTestUser
    void getComments_returnsSuccessCode() throws Exception {
        //given
        final int USER_COUNT = 10;
        final int COMMENT_COUNT = 10;
        final int PAGE_NUMBER = 0;
        final int PAGE_SIZE = 2;

        UserEntity questioner = userRepository.save(UserEntityFixture.of());
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(1);
        QuestionEntity question = questionRepository.save(createQuestion(questioner, closeAt));
        commenterSequenceRepository.save(createSequence(question));

        List<UserEntity> users = IntStream.range(0, USER_COUNT)
                .mapToObj(i -> UserEntityFixture.of())
                .toList();
        userRepository.saveAll(users);

        IntStream.range(0, COMMENT_COUNT).forEach(commentIndex -> {
            CommenterAliasEntity alias = createAlias(CommenterAliasPrefix.getAlias(commentIndex), question, users.get(commentIndex));
            CommentEntity comment = createComment(questioner, "content", alias, question);
            commenterAliasRepository.save(alias);
            commentRepository.save(comment);
        });

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}/comments", question.getId())
                                .param("page", String.valueOf(PAGE_NUMBER))
                                .param("size", String.valueOf(PAGE_SIZE))

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/questions/{questionId}/comments/{commentId} - 성공 (댓글 작성자는 댓글을 수정할 수 있다)")
    @WithTestUser("commenter@email.com")
    void modifyComment_returnSuccessCode() throws Exception {
        //given
        UserEntity user = userRepository.findByEmail("commenter@email.com").get();
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
        CommenterAliasEntity commenterAlias = commenterAliasRepository.save(createAlias("alias", question, user));
        CommentEntity comment = commentRepository.save(CommentEntityFixture.of(user, question, commenterAlias));

        String modifiedContent = "modified content";
        QuestionCommentRequest request = QuestionCommentRequest.builder()
                .content(modifiedContent)
                .build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/questions/{questionId}/comments/{commentId}", question.getId(), comment.getId())
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isOk());
        Assertions.assertThat(commentRepository.findById(comment.getId()).get().getContent()).isEqualTo(modifiedContent);
    }

    @Test
    @DisplayName("PUT /api/v1/questions/{questionId}/comments/{commentId} - 실패 (댓글 작성자가 아니면 댓글을 수정할 수 없다)")
    @WithTestUser
    void modifyComment_returnForbiddenCode() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
        CommenterAliasEntity commenterAlias = commenterAliasRepository.save(createAlias("alias", question, user));
        CommentEntity comment = commentRepository.save(CommentEntityFixture.of(user, question, commenterAlias));

        String modifiedContent = "modified content";
        QuestionCommentRequest request = QuestionCommentRequest.builder()
                .content(modifiedContent)
                .build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/questions/{questionId}/comments/{commentId}", question.getId(), comment.getId())
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("PUT /api/v1/questions/{questionId}/comments/{commentId} - 실패 (댓글 수정 파라미터가 올바르지 않은 경우)")
    @WithTestUser
    void modifyComment_withInvalidParameter_returnBadRequestCode() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
        CommenterAliasEntity commenterAlias = commenterAliasRepository.save(createAlias("alias", question, user));
        CommentEntity comment = commentRepository.save(CommentEntityFixture.of(user, question, commenterAlias));

        QuestionCommentRequest request = QuestionCommentRequest.builder()
                .content("")
                .build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/questions/{questionId}/comments/{commentId}", question.getId(), comment.getId())
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("PUT /api/v1/questions/{questionId}/comments/{commentId} - 실패 (댓글이 존재하지 않는 경우)")
    @WithTestUser
    void modifyComment_whenCommentNotExists_returnBadRequestCode() throws Exception {
        //given
        long notExistingQuestionId = 0L;
        long notExistingCommentId = 0L;

        QuestionCommentRequest request = QuestionCommentRequest.builder()
                .content("content")
                .build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/questions/{questionId}/comments/{commentId}", notExistingQuestionId, notExistingCommentId)
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(COMMENT_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/questions/{questionId}/comments/{commentId} - 성공 (댓글 작성자는 댓글을 삭제할 수 있다)")
    @WithTestUser("commenter@email.com")
    void deleteComment_returnSuccessCode() throws Exception {
        //given
        UserEntity user = userRepository.findByEmail("commenter@email.com").get();
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
        CommenterAliasEntity commenterAlias = commenterAliasRepository.save(createAlias("alias", question, user));
        CommentEntity comment = commentRepository.save(CommentEntityFixture.of(user, question, commenterAlias));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/questions/{questionId}/comments/{commentId}", question.getId(), comment.getId())
                )
                .andExpect(status().isOk());
        Assertions.assertThat(commentRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/questions/{questionId}/comments/{commentId} - 실패 (댓글 작성자가 아니면 댓글을 삭제할 수 없다)")
    @WithTestUser
    void deleteComment_returnForbiddenCode() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
        CommenterAliasEntity commenterAlias = commenterAliasRepository.save(createAlias("alias", question, user));
        CommentEntity comment = commentRepository.save(CommentEntityFixture.of(user, question, commenterAlias));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/questions/{questionId}/comments/{commentId}", question.getId(), comment.getId())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/questions/{questionId}/comments/{commentId} - 실패 (댓글이 존재하지 않는 경우)")
    @WithTestUser
    void deleteComment_whenCommentNotExists_returnBadRequestCode() throws Exception {
        //given
        long notExistingQuestionId = 0L;
        long notExistingCommentId = 0L;

        //when //then
        mockMvc.perform(
                        delete("/api/v1/questions/{questionId}/comments/{commentId}", notExistingQuestionId, notExistingCommentId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(COMMENT_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("GET /api/v1/questions/{questionId}/comments - 실패 (존재하지 않는 질문 아이디)")
    @WithTestUser
    void getComments_withNotExistingQuestionId_returnsSuccessCode() throws Exception {
        //given
        final int PAGE_NUMBER = 0;
        final int PAGE_SIZE = 2;
        long notExistingUserId = 0L;

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}/comments", notExistingUserId)
                                .param("page", String.valueOf(PAGE_NUMBER))
                                .param("size", String.valueOf(PAGE_SIZE))

                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(QUESTION_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("GET /api/v1/questions/{questionId}/comments - 실패 (미인증 사용자)")
    @WithAnonymousUser
    void getComments_byWhoNotAuthenticated_returnsSuccessCode() throws Exception {
        //given
        final int PAGE_NUMBER = 0;
        final int PAGE_SIZE = 2;
        long notExistingUserId = 0L;

        //when //then
        mockMvc.perform(
                        get("/api/v1/questions/{questionId}/comments", notExistingUserId)
                                .param("page", String.valueOf(PAGE_NUMBER))
                                .param("size", String.valueOf(PAGE_SIZE))

                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
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

    public CommenterSequenceEntity createSequence(QuestionEntity questionEntity) {
        return CommenterSequenceEntity.builder()
                .questionEntity(questionEntity)
                .build();
    }

    public CommenterAliasEntity createAlias(String alias, QuestionEntity questionEntity, UserEntity userEntity) {
        return CommenterAliasEntity.builder()
                .alias(alias)
                .questionEntity(questionEntity)
                .userEntity(userEntity)
                .build();
    }

    public CommentEntity createComment(UserEntity userEntity, String content, CommenterAliasEntity commenterAliasEntity, QuestionEntity questionEntity) {
        return CommentEntity.builder()
                .content(content)
                .userEntity(userEntity)
                .commenterAliasEntity(commenterAliasEntity)
                .questionEntity(questionEntity)
                .build();
    }

    public BookmarkEntity createBookmark(UserEntity userEntity, QuestionEntity questionEntity) {
        return BookmarkEntity.builder()
                .questionEntity(questionEntity)
                .userEntity(userEntity)
                .build();
    }

    public QuestionViewEntity createQuestionView(QuestionEntity questionEntity) {
        return QuestionViewEntity.builder()
                .questionEntity(questionEntity)
                .build();
    }
}