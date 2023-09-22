package com.chwihae.service.bookmark;

import com.chwihae.domain.bookmark.BookmarkEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.exception.CustomException;
import com.chwihae.exception.CustomExceptionError;
import com.chwihae.infra.test.AbstractIntegrationTest;
import com.chwihae.infra.fixture.UserEntityFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Transactional
class BookmarkServiceTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("질문을 스크랩한다")
    void bookmark_returnTrue() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of();
        UserEntity viewer = UserEntityFixture.of();
        userRepository.saveAll(List.of(questioner, viewer));
        QuestionEntity question = questionRepository.save(createQuestion(questioner));

        //when
        boolean result = bookmarkService.bookmark(question.getId(), viewer.getId());

        //then
        Assertions.assertThat(result).isTrue();
        Assertions.assertThat(bookmarkRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("스크랩한 질문을 삭제한다")
    void bookmark_returnFalse() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of();
        UserEntity viewer = UserEntityFixture.of();
        userRepository.saveAll(List.of(questioner, viewer));
        QuestionEntity question = questionRepository.save(createQuestion(questioner));
        BookmarkEntity saved = bookmarkRepository.save(createBookmark(viewer, question));

        //when
        boolean result = bookmarkService.bookmark(question.getId(), viewer.getId());

        //then
        Assertions.assertThat(result).isFalse();
        Assertions.assertThat(bookmarkRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("질문글 작성자가 질문을 스크랩하려고 하면 예외가 발생한다")
    void bookmark_byQuestioner_throwsException() throws Exception {
        //given
        UserEntity questioner = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(questioner));

        //when //then
        Assertions.assertThatThrownBy(() -> bookmarkService.bookmark(question.getId(), questioner.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.FORBIDDEN);
    }

    public QuestionEntity createQuestion(UserEntity userEntity) {
        return QuestionEntity.builder()
                .userEntity(userEntity)
                .title("title")
                .content("content")
                .closeAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .type(QuestionType.SPEC)
                .build();
    }

    public BookmarkEntity createBookmark(UserEntity userEntity, QuestionEntity questionEntity) {
        return BookmarkEntity.builder()
                .questionEntity(questionEntity)
                .userEntity(userEntity)
                .build();
    }
}