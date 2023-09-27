package com.chwihae.service.question;

import com.chwihae.domain.bookmark.BookmarkEntity;
import com.chwihae.domain.comment.CommentEntity;
import com.chwihae.domain.commenter.CommenterAliasEntity;
import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionStatus;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.vote.VoteEntity;
import com.chwihae.dto.option.request.OptionCreateRequest;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.dto.question.response.QuestionDetailResponse;
import com.chwihae.dto.question.response.QuestionListResponse;
import com.chwihae.dto.user.UserQuestionFilterType;
import com.chwihae.exception.CustomException;
import com.chwihae.exception.CustomExceptionError;
import com.chwihae.infra.fixture.*;
import com.chwihae.infra.test.AbstractIntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.chwihae.domain.question.QuestionType.*;
import static com.chwihae.exception.CustomExceptionError.*;
import static com.chwihae.infra.utils.TimeUtils.KST;

@Transactional
class QuestionServiceTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("질문과 옵션을 저장한 후 질문 아이디를 반환한다")
    void createQuestionWithOptions_returnsQuestionId() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
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
                .type(SPEC)
                .closeAt(closeAt)
                .content("content")
                .options(options)
                .build();

        //when
        Long id = questionService.createQuestion(request, userEntity.getId());

        //then
        Assertions.assertThat(id).isNotNull();
        Assertions.assertThat(questionRepository.findAll()).hasSize(1);
        Assertions.assertThat(optionRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("질문 생성 시, 댓글 작성자의 순서 정보가 0으로 저장된다")
    void createCommenterSequence() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());

        QuestionCreateRequest request = QuestionCreateRequest.builder()
                .title("title")
                .type(SPEC)
                .closeAt(LocalDateTime.of(2023, 11, 11, 0, 0))
                .content("content")
                .options(List.of())
                .build();

        //when
        questionService.createQuestion(request, user.getId());

        //then
        Assertions.assertThat(commenterSequenceRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("질문 생성 시, 조회 수를 0으로 저장한다")
    void createQuestionView() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());

        QuestionCreateRequest request = QuestionCreateRequest.builder()
                .title("title")
                .type(SPEC)
                .closeAt(LocalDateTime.of(2023, 11, 11, 0, 0))
                .content("content")
                .options(List.of())
                .build();

        //when
        questionService.createQuestion(request, user.getId());

        //then
        Assertions.assertThat(questionViewRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("등록 시 사용자가 조회되지 않으면 예외가 발생한다")
    void createQuestionWithOptions_whenUserNotFound_throwsCustomException() throws Exception {
        //given
        long notExistingUserId = 0L;
        QuestionCreateRequest request = QuestionCreateRequest.builder()
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> questionService.createQuestion(request, notExistingUserId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(USER_NOT_FOUND);
    }

    @Test
    @DisplayName("질문 아이디로 조회하여 질문을 반환한다")
    void getQuestion_returnsQuestionResponse() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        QuestionEntity questionEntity = questionRepository.save(createQuestion(userEntity, SPEC));
        questionViewRepository.save(createQuestionView(questionEntity));

        //when
        QuestionDetailResponse response = questionService.getQuestion(questionEntity.getId(), userEntity.getId());

        //then
        Assertions.assertThat(response)
                .extracting("title", "content", "type", "editable")
                .containsExactly(questionEntity.getTitle(), questionEntity.getContent(), questionEntity.getType(), true);
    }

    @Test
    @DisplayName("질문을 조회하면 질문 조회 이벤트를 발행한다")
    void handleQuestionViewEvent() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        QuestionEntity questionEntity = questionRepository.save(createQuestion(userEntity, SPEC));
        questionViewRepository.save(createQuestionView(questionEntity));

        //when
        questionService.getQuestion(questionEntity.getId(), userEntity.getId());

        //then
        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> Mockito.verify(questionViewEventListener, Mockito.times(1)).handleQuestionViewEvent(Mockito.any()));
    }

    @Test
    @DisplayName("존재하지 않는 질문 아이디로 조회하면 예외가 발생한다")
    void getQuestion_withNotExistingQuestionId_throwsCustomException() throws Exception {
        //given
        long notExistingQuestionId = 0L;
        long notExistingUserId = 0L;

        //when //then
        Assertions.assertThatThrownBy(() -> questionService.getQuestion(notExistingQuestionId, notExistingUserId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.QUESTION_NOT_FOUND);
    }

    @Test
    @DisplayName("질문 작성자는 질문이 마감 되었을 때 질문을 삭제할 수 있다")
    void deleteQuestion_pass() throws Exception {
        //given
        UserEntity userEntity1 = UserEntityFixture.of();
        UserEntity userEntity2 = UserEntityFixture.of();
        userRepository.saveAll(List.of(userEntity1, userEntity2));
        LocalDateTime closeAt = LocalDateTime.now(KST).minusDays(1);
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(userEntity1, closeAt));

        OptionEntity option1 = OptionEntityFixture.of(question);
        OptionEntity option2 = OptionEntityFixture.of(question);
        optionRepository.saveAll(List.of(option1, option2));

        VoteEntity vote1 = VoteEntityFixture.of(option1, userEntity1);
        VoteEntity vote2 = VoteEntityFixture.of(option1, userEntity2);
        voteRepository.saveAll(List.of(vote1, vote2));

        BookmarkEntity bookmark1 = BookmarkFixture.of(question, userEntity1);
        BookmarkEntity bookmark2 = BookmarkFixture.of(question, userEntity2);
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2));

        commenterSequenceRepository.save(CommenterSequenceFixture.of(question));
        questionViewRepository.save(createQuestionView(question));

        CommenterAliasEntity alias1 = CommenterAliasFixture.of(userEntity1, question);
        CommenterAliasEntity alias2 = CommenterAliasFixture.of(userEntity2, question);
        commenterAliasRepository.saveAll(List.of(alias1, alias2));

        CommentEntity comment1 = CommentEntityFixture.of(userEntity1, question, alias1);
        CommentEntity comment2 = CommentEntityFixture.of(userEntity2, question, alias2);
        commentRepository.saveAll(List.of(comment1, comment2));

        //when
        questionService.deleteQuestion(question.getId(), userEntity1.getId());

        //then
        Assertions.assertThat(questionRepository.findAll()).isEmpty();
        Assertions.assertThat(commentRepository.findAll()).isEmpty();
        Assertions.assertThat(commenterAliasRepository.findAll()).isEmpty();
        Assertions.assertThat(questionViewRepository.findAll()).isEmpty();
        Assertions.assertThat(commenterSequenceRepository.findAll()).isEmpty();
        Assertions.assertThat(bookmarkRepository.findAll()).isEmpty();
        Assertions.assertThat(voteRepository.findAll()).isEmpty();
        Assertions.assertThat(optionRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("마감되지 않은 질문을 질문 작성자가 삭제하려고 하면 예외가 발생한다")
    void deleteQuestion_whenQuestionNotClosed_throwException() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(userEntity));

        //when //then
        Assertions.assertThatThrownBy(() -> questionService.deleteQuestion(question.getId(), userEntity.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FORBIDDEN);
    }

    @Test
    @DisplayName("존재하지 않은 질문을 삭제하려고 하면 예외가 발생한다")
    void deleteQuestion_withNotExistingQuestion_throwException() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        long notExistingQuestionId = 0L;

        //when //then
        Assertions.assertThatThrownBy(() -> questionService.deleteQuestion(notExistingQuestionId, userEntity.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(QUESTION_NOT_FOUND);
    }

    @Test
    @DisplayName("질문 작성자가 아닌 사용자가 질문을 삭제하려고 하면 예외가 발생한다")
    void deleteQuestion_byOther_throwException() throws Exception {
        //given
        UserEntity userEntity1 = UserEntityFixture.of();
        UserEntity userEntity2 = UserEntityFixture.of();
        userRepository.saveAll(List.of(userEntity1, userEntity2));
        LocalDateTime closeAt = LocalDateTime.now(KST).minusDays(1);
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(userEntity1, closeAt));

        //when //then
        Assertions.assertThatThrownBy(() -> questionService.deleteQuestion(question.getId(), userEntity2.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FORBIDDEN);
    }

    @Test
    @DisplayName("질문 전체를 질문 타입,질문 상태 조건으로 페이지네이션으로 조회한다")
    void getQuestions_byTypeAndStatus_returnsPageResponse() throws Exception {
        //given
        final int PAGE_SIZE = 2;
        final int PAGE_NUMBER = 0;

        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        QuestionEntity question1 = createQuestion(userEntity, SPEC);
        QuestionEntity question2 = createQuestion(userEntity, COMPANY);
        QuestionEntity question3 = createQuestion(userEntity, ETC);
        QuestionEntity question4 = createQuestion(userEntity, SPEC);
        QuestionEntity question5 = createQuestion(userEntity, SPEC);
        questionRepository.saveAll(List.of(question1, question2, question3, question4, question5));

        QuestionViewEntity view1 = QuestionViewFixture.of(question1);
        QuestionViewEntity view2 = QuestionViewFixture.of(question2);
        QuestionViewEntity view3 = QuestionViewFixture.of(question3);
        QuestionViewEntity view4 = QuestionViewFixture.of(question4);
        QuestionViewEntity view5 = QuestionViewFixture.of(question5);
        questionViewRepository.saveAll(List.of(view1, view2, view3, view4, view5));


        PageRequest pageRequest = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        QuestionStatus status = QuestionStatus.IN_PROGRESS;

        //when
        Page<QuestionListResponse> response = questionService.getQuestionsByTypeAndStatus(SPEC, status, pageRequest);

        //then
        Assertions.assertThat(response.getContent()).hasSize(PAGE_SIZE);
        Assertions.assertThat(response.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("사용자가 작성한 질문 리스트를 페이지네이션으로 반환한다")
    void getUserQuestions_withMyType_returnsPagination() throws Exception {
        // Given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        QuestionEntity question1 = createQuestion(userEntity, SPEC);
        QuestionEntity question2 = createQuestion(userEntity, COMPANY);
        QuestionEntity question3 = createQuestion(userEntity, ETC);
        QuestionEntity question4 = createQuestion(userEntity, SPEC);
        QuestionEntity question5 = createQuestion(userEntity, CAREER);
        questionRepository.saveAll(List.of(question1, question2, question3, question4, question5));

        QuestionViewEntity view1 = QuestionViewFixture.of(question1);
        QuestionViewEntity view2 = QuestionViewFixture.of(question2);
        QuestionViewEntity view3 = QuestionViewFixture.of(question3);
        QuestionViewEntity view4 = QuestionViewFixture.of(question4);
        QuestionViewEntity view5 = QuestionViewFixture.of(question5);
        questionViewRepository.saveAll(List.of(view1, view2, view3, view4, view5));

        UserQuestionFilterType filterType = UserQuestionFilterType.ME;
        PageRequest pageRequest = PageRequest.of(0, 2);

        // When
        Page<QuestionListResponse> response = questionService.getUserQuestions(userEntity.getId(), filterType, pageRequest);

        // Then
        Assertions.assertThat(response.getTotalElements()).isEqualTo(5);
        Assertions.assertThat(response.getTotalPages()).isEqualTo(3);
        Assertions.assertThat(response.getNumber()).isEqualTo(0);
        Assertions.assertThat(response.getSize()).isEqualTo(2);
        Assertions.assertThat(response.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("사용자가 북마크한 질문 리스트를 페이지네이션으로 반환한다")
    void getUserQuestions_withBookmarkedType_returnsPagination() throws Exception {
        // Given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        QuestionEntity question1 = createQuestion(userEntity, SPEC);
        QuestionEntity question2 = createQuestion(userEntity, COMPANY);
        QuestionEntity question3 = createQuestion(userEntity, ETC);
        QuestionEntity question4 = createQuestion(userEntity, SPEC);
        QuestionEntity question5 = createQuestion(userEntity, CAREER);
        questionRepository.saveAll(List.of(question1, question2, question3, question4, question5));

        QuestionViewEntity view1 = QuestionViewFixture.of(question1);
        QuestionViewEntity view2 = QuestionViewFixture.of(question2);
        QuestionViewEntity view3 = QuestionViewFixture.of(question3);
        QuestionViewEntity view4 = QuestionViewFixture.of(question4);
        QuestionViewEntity view5 = QuestionViewFixture.of(question5);
        questionViewRepository.saveAll(List.of(view1, view2, view3, view4, view5));

        BookmarkEntity bookmark1 = BookmarkFixture.of(question1, userEntity);
        BookmarkEntity bookmark2 = BookmarkFixture.of(question2, userEntity);
        BookmarkEntity bookmark3 = BookmarkFixture.of(question3, userEntity);
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2, bookmark3));

        UserQuestionFilterType filterType = UserQuestionFilterType.BOOKMARKED;
        PageRequest pageRequest = PageRequest.of(0, 2);

        // When
        Page<QuestionListResponse> response = questionService.getUserQuestions(userEntity.getId(), filterType, pageRequest);

        // Then
        Assertions.assertThat(response.getTotalElements()).isEqualTo(3);
        Assertions.assertThat(response.getTotalPages()).isEqualTo(2);
        Assertions.assertThat(response.getNumber()).isEqualTo(0);
        Assertions.assertThat(response.getSize()).isEqualTo(2);
        Assertions.assertThat(response.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("사용자가 투표한 질문 리스트를 페이지네이션으로 반환한다")
    void getUserQuestions_withVotedType_returnsPagination() throws Exception {

        // Given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        QuestionEntity question1 = createQuestion(userEntity, ETC);
        QuestionEntity question2 = createQuestion(userEntity, ETC);
        QuestionEntity question3 = createQuestion(userEntity, ETC);
        QuestionEntity question4 = createQuestion(userEntity, ETC);
        QuestionEntity question5 = createQuestion(userEntity, ETC);
        questionRepository.saveAll(List.of(question1, question2, question3, question4, question5));

        QuestionViewEntity view1 = QuestionViewFixture.of(question1);
        QuestionViewEntity view2 = QuestionViewFixture.of(question2);
        QuestionViewEntity view3 = QuestionViewFixture.of(question3);
        QuestionViewEntity view4 = QuestionViewFixture.of(question4);
        QuestionViewEntity view5 = QuestionViewFixture.of(question5);
        questionViewRepository.saveAll(List.of(view1, view2, view3, view4, view5));

        OptionEntity option1 = OptionEntityFixture.of(question1);
        OptionEntity option2 = OptionEntityFixture.of(question2);
        OptionEntity option3 = OptionEntityFixture.of(question3);
        OptionEntity option4 = OptionEntityFixture.of(question4);
        OptionEntity option5 = OptionEntityFixture.of(question5);
        optionRepository.saveAll(List.of(option1, option2, option3, option4, option5));

        VoteEntity vote1 = VoteEntityFixture.of(option1, userEntity);
        VoteEntity vote2 = VoteEntityFixture.of(option2, userEntity);
        VoteEntity vote3 = VoteEntityFixture.of(option3, userEntity);
        VoteEntity vote4 = VoteEntityFixture.of(option4, userEntity);
        VoteEntity vote5 = VoteEntityFixture.of(option5, userEntity);
        voteRepository.saveAll(List.of(vote1, vote2, vote3, vote4, vote5));

        UserQuestionFilterType filterType = UserQuestionFilterType.VOTED;
        PageRequest pageRequest = PageRequest.of(0, 3);

        // When
        Page<QuestionListResponse> response = questionService.getUserQuestions(userEntity.getId(), filterType, pageRequest);

        // Then
        Assertions.assertThat(response.getTotalElements()).isEqualTo(5);
        Assertions.assertThat(response.getTotalPages()).isEqualTo(2);
        Assertions.assertThat(response.getNumber()).isEqualTo(0);
        Assertions.assertThat(response.getSize()).isEqualTo(3);
        Assertions.assertThat(response.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("존재하지 않는 타입으로 질문 리스트를 요청하면 예외가 발생한다")
    void getUserQuestions_withNotExistingType_returnsPagination() throws Exception {
        //given
        long notExistingUserId = 0L;
        UserQuestionFilterType type = null;
        PageRequest pageRequest = PageRequest.of(0, 1);

        //when //then
        Assertions.assertThatThrownBy(() -> questionService.getUserQuestions(notExistingUserId, type, pageRequest))
                .isInstanceOf(IllegalStateException.class);
    }

    // TODO - 캐싱되어 있는거
    // TODO - DB에만 있는거

    public QuestionEntity createQuestion(UserEntity userEntity, QuestionType type) {
        return QuestionEntity.builder()
                .userEntity(userEntity)
                .title("title")
                .content("content")
                .closeAt(LocalDateTime.now(KST).plusHours(1))
                .type(type)
                .build();
    }

    public QuestionViewEntity createQuestionView(QuestionEntity questionEntity) {
        return QuestionViewEntity.builder()
                .questionEntity(questionEntity)
                .build();
    }
}