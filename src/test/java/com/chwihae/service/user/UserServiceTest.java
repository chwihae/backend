package com.chwihae.service.user;

import com.chwihae.domain.bookmark.BookmarkEntity;
import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.vote.VoteEntity;
import com.chwihae.dto.question.response.QuestionListResponse;
import com.chwihae.dto.user.UserContext;
import com.chwihae.dto.user.UserQuestionFilterType;
import com.chwihae.exception.CustomException;
import com.chwihae.infra.fixture.QuestionViewFixture;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.infra.test.AbstractIntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.chwihae.domain.question.QuestionType.*;
import static com.chwihae.exception.CustomExceptionError.USER_NOT_FOUND;

@Transactional
class UserServiceTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("이메일로 사용자를 조회하여 존재하지 않으면 사용자를 저장한다")
    void createUser_returnsUserEntity() throws Exception {
        //given
        String email = "test@email.com";

        //when
        userService.getOrCreateUser(email);

        //then
        Assertions.assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("사용자 아이디로 사용자가 조회될 경우 UserContext로 반환한다")
    void getUserContextOrException_returnsUserContext() throws Exception {
        //given
        String email = "test@email.com";

        UserEntity saved = userRepository.save(
                UserEntity.builder()
                        .email(email)
                        .build()
        );

        //when
        UserContext userContext = userService.getUserContextOrException(saved.getId());

        //then
        Assertions.assertThat(userContext)
                .extracting("id", "email")
                .containsExactly(saved.getId(), saved.getEmail());
    }

    @Test
    @DisplayName("사용자 아이디로 사용자가 조회되지 않으면 예외가 발생한다")
    void getUserContextOrException_throwsCustomException() throws Exception {
        //given
        long notExistingUserId = 0L;

        //when //then
        Assertions.assertThatThrownBy(() -> userService.getUserContextOrException(notExistingUserId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(USER_NOT_FOUND);
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
        Page<QuestionListResponse> response = userService.getUserQuestions(userEntity.getId(), filterType, pageRequest);

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

        BookmarkEntity bookmark1 = createBookmark(userEntity, question1);
        BookmarkEntity bookmark2 = createBookmark(userEntity, question2);
        BookmarkEntity bookmark3 = createBookmark(userEntity, question3);
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2, bookmark3));

        UserQuestionFilterType filterType = UserQuestionFilterType.BOOKMARKED;
        PageRequest pageRequest = PageRequest.of(0, 2);

        // When
        Page<QuestionListResponse> response = userService.getUserQuestions(userEntity.getId(), filterType, pageRequest);

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

        OptionEntity option1 = createOption(question1);
        OptionEntity option2 = createOption(question2);
        OptionEntity option3 = createOption(question3);
        OptionEntity option4 = createOption(question4);
        OptionEntity option5 = createOption(question5);
        optionRepository.saveAll(List.of(option1, option2, option3, option4, option5));

        VoteEntity vote1 = createVote(option1, userEntity);
        VoteEntity vote2 = createVote(option2, userEntity);
        VoteEntity vote3 = createVote(option3, userEntity);
        VoteEntity vote4 = createVote(option4, userEntity);
        VoteEntity vote5 = createVote(option5, userEntity);
        voteRepository.saveAll(List.of(vote1, vote2, vote3, vote4, vote5));

        UserQuestionFilterType filterType = UserQuestionFilterType.VOTED;
        PageRequest pageRequest = PageRequest.of(0, 3);

        // When
        Page<QuestionListResponse> response = userService.getUserQuestions(userEntity.getId(), filterType, pageRequest);

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
        Assertions.assertThatThrownBy(() -> userService.getUserQuestions(notExistingUserId, type, pageRequest))
                .isInstanceOf(IllegalStateException.class);
    }

    public QuestionEntity createQuestion(UserEntity userEntity, QuestionType type) {
        return QuestionEntity.builder()
                .userEntity(userEntity)
                .title("title")
                .content("content")
                .closeAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusHours(1))
                .type(type)
                .build();
    }

    public OptionEntity createOption(QuestionEntity questionEntity) {
        return OptionEntity.builder()
                .questionEntity(questionEntity)
                .name("name")
                .build();
    }

    public BookmarkEntity createBookmark(UserEntity userEntity, QuestionEntity questionEntity) {
        return BookmarkEntity.builder()
                .questionEntity(questionEntity)
                .userEntity(userEntity)
                .build();
    }

    public VoteEntity createVote(OptionEntity optionEntity, UserEntity userEntity) {
        return VoteEntity.builder()
                .questionEntity(optionEntity.getQuestionEntity())
                .optionEntity(optionEntity)
                .userEntity(userEntity)
                .build();
    }
}