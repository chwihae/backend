package com.chwihae.domain.question;

import com.chwihae.domain.bookmark.BookmarkEntity;
import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.vote.VoteEntity;
import com.chwihae.dto.question.response.QuestionListResponse;
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

import static com.chwihae.domain.question.QuestionStatus.IN_PROGRESS;
import static com.chwihae.domain.question.QuestionType.*;

@Transactional
class QuestionRepositoryExtensionTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("상태,타입으로 질문 리스트를 페이지네이션으로 조회한다")
    void findByStatusAndType_returnsPagination() throws Exception {
        //given
        final int PAGE_SIZE = 2;
        final int PAGE_NUMBER = 0;
        PageRequest pageRequest = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        QuestionEntity question1 = createQuestion(userEntity, SPEC);
        QuestionEntity question2 = createQuestion(userEntity, COMPANY);
        QuestionEntity question3 = createQuestion(userEntity, ETC);
        QuestionEntity question4 = createQuestion(userEntity, SPEC);
        QuestionEntity question5 = createQuestion(userEntity, CAREER);
        questionRepository.saveAll(List.of(question1, question2, question3, question4, question5));

        //when
        Page<QuestionListResponse> response = questionRepository.findByTypeAndStatusWithCounts(IN_PROGRESS, ETC, pageRequest);

        //then
        Assertions.assertThat(response.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(response.getTotalPages()).isEqualTo(1);
        Assertions.assertThat(response.getNumber()).isEqualTo(PAGE_NUMBER);
        Assertions.assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("사용자 아이디로 사용자가 작성한 질문 리스트를 페이지네이션으로 조회한다")
    void findMyByUserIdWithCounts_returnPagination() throws Exception {
        //given
        final int PAGE_SIZE = 2;
        final int PAGE_NUMBER = 0;
        PageRequest pageRequest = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        QuestionEntity question1 = createQuestion(userEntity, SPEC);
        QuestionEntity question2 = createQuestion(userEntity, COMPANY);
        QuestionEntity question3 = createQuestion(userEntity, ETC);
        QuestionEntity question4 = createQuestion(userEntity, SPEC);
        QuestionEntity question5 = createQuestion(userEntity, CAREER);
        questionRepository.saveAll(List.of(question1, question2, question3, question4, question5));

        //when
        Page<QuestionListResponse> response = questionRepository.findMyByUserIdWithCounts(userEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(response.getTotalElements()).isEqualTo(5);
        Assertions.assertThat(response.getTotalPages()).isEqualTo(3);
        Assertions.assertThat(response.getNumber()).isEqualTo(PAGE_NUMBER);
        Assertions.assertThat(response.getContent()).hasSize(PAGE_SIZE);
    }

    @Test
    @DisplayName("사용자 아이디로 사용자가 북마크한 질문 리스트를 페이지네이션으로 조회한다")
    void findBookmarkedByUserIdWithCounts_returnPagination() throws Exception {
//given
        final int PAGE_SIZE = 2;
        final int PAGE_NUMBER = 0;
        PageRequest pageRequest = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        QuestionEntity question1 = createQuestion(userEntity, SPEC);
        QuestionEntity question2 = createQuestion(userEntity, COMPANY);
        QuestionEntity question3 = createQuestion(userEntity, ETC);
        QuestionEntity question4 = createQuestion(userEntity, SPEC);
        QuestionEntity question5 = createQuestion(userEntity, CAREER);
        questionRepository.saveAll(List.of(question1, question2, question3, question4, question5));

        BookmarkEntity bookmark1 = createBookmark(userEntity, question1);
        BookmarkEntity bookmark2 = createBookmark(userEntity, question2);
        BookmarkEntity bookmark3 = createBookmark(userEntity, question3);
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2, bookmark3));

        //when
        Page<QuestionListResponse> response = questionRepository.findBookmarkedByUserIdWithCounts(userEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(response.getTotalElements()).isEqualTo(3);
        Assertions.assertThat(response.getTotalPages()).isEqualTo(2);
        Assertions.assertThat(response.getNumber()).isEqualTo(PAGE_NUMBER);
        Assertions.assertThat(response.getContent()).hasSize(PAGE_SIZE);
    }

    @Test
    @DisplayName("사용자 아이디로 사용자가 투표한 질문 리스트를 페이지네이션으로 조회한다")
    void findVotedByUserIdWithCounts_returnPagination() throws Exception {
        //given
        final int PAGE_SIZE = 3;
        final int PAGE_NUMBER = 0;
        PageRequest pageRequest = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question1 = createQuestion(user, ETC);
        QuestionEntity question2 = createQuestion(user, ETC);
        QuestionEntity question3 = createQuestion(user, ETC);
        QuestionEntity question4 = createQuestion(user, ETC);
        QuestionEntity question5 = createQuestion(user, ETC);
        questionRepository.saveAll(List.of(question1, question2, question3, question4, question5));

        OptionEntity option1 = createOption(question1);
        OptionEntity option2 = createOption(question2);
        OptionEntity option3 = createOption(question3);
        OptionEntity option4 = createOption(question4);
        OptionEntity option5 = createOption(question5);
        optionRepository.saveAll(List.of(option1, option2, option3, option4, option5));

        VoteEntity vote1 = createVote(option1, user);
        VoteEntity vote2 = createVote(option2, user);
        VoteEntity vote3 = createVote(option3, user);
        VoteEntity vote4 = createVote(option4, user);
        VoteEntity vote5 = createVote(option5, user);
        voteRepository.saveAll(List.of(vote1, vote2, vote3, vote4, vote5));

        //when
        Page<QuestionListResponse> response = questionRepository.findVotedByUserIdWithCounts(user.getId(), pageRequest);

        //then
        Assertions.assertThat(response.getTotalElements()).isEqualTo(5);
        Assertions.assertThat(response.getTotalPages()).isEqualTo(2);
        Assertions.assertThat(response.getNumber()).isEqualTo(PAGE_NUMBER);
        Assertions.assertThat(response.getContent()).hasSize(PAGE_SIZE);
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