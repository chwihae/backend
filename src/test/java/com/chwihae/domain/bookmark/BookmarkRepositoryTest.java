package com.chwihae.domain.bookmark;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.infra.test.AbstractIntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Transactional
class BookmarkRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("질문 아이디로 북마크 집계를 한다")
    void countByQuestionEntityId() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of();
        UserEntity user1 = UserEntityFixture.of();
        UserEntity user2 = UserEntityFixture.of();
        UserEntity user3 = UserEntityFixture.of();
        UserEntity user4 = UserEntityFixture.of();
        UserEntity user5 = UserEntityFixture.of();
        userRepository.saveAll(List.of(questioner, user1, user2, user3, user4, user5));

        QuestionEntity question = questionRepository.save(createQuestion(questioner));
        BookmarkEntity bookmark1 = createBookmark(user1, question);
        BookmarkEntity bookmark2 = createBookmark(user1, question);
        BookmarkEntity bookmark3 = createBookmark(user1, question);
        BookmarkEntity bookmark4 = createBookmark(user1, question);
        BookmarkEntity bookmark5 = createBookmark(user1, question);
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2, bookmark3, bookmark4, bookmark5));

        //when
        long bookmarkedCount = bookmarkRepository.countByQuestionEntityId(question.getId());

        //then
        Assertions.assertThat(bookmarkedCount).isEqualTo(5L);
    }

    @Test
    @DisplayName("사용자가 질문을 북마크 했는지 확인한다")
    void existsByQuestionEntityIdAndUserEntityId() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of();
        UserEntity viewer = UserEntityFixture.of();
        userRepository.saveAll(List.of(questioner, viewer));
        QuestionEntity question = questionRepository.save(createQuestion(questioner));
        bookmarkRepository.save(createBookmark(viewer, question));

        //when
        boolean result = bookmarkRepository.existsByQuestionEntityIdAndUserEntityId(question.getId(), viewer.getId());

        //then
        Assertions.assertThat(result).isTrue();
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