package com.chwihae.service.bookmark;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.infra.test.AbstractConcurrencyTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class BookmarkServiceConcurrencyTest extends AbstractConcurrencyTest {

    @AfterEach
    void tearDown() {
        executorService.shutdown();
        bookmarkRepository.physicallyDeleteAll();
        questionRepository.physicallyDeleteAll();
        userRepository.physicallyDeleteAll();
    }

    @CsvSource({"100,false", "101,true"})
    @ParameterizedTest
    @DisplayName("사용자가 북마크 요청을 동시에 해도 북마크 등록/해제가 순서대로 실행된다")
    void bookmark_withConcurrency(int totalRequestCount, boolean expected) throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of();
        UserEntity viewer = UserEntityFixture.of();
        userRepository.saveAll(List.of(questioner, viewer));
        QuestionEntity question = questionRepository.save(createQuestion(questioner));

        List<Callable<Boolean>> bookmarkTasks = doGenerateConcurrentTasks(totalRequestCount, () ->
                bookmarkService.bookmark(question.getId(), viewer.getId())
        );

        //when
        List<Future<Boolean>> bookmarkFutures = executorService.invokeAll(bookmarkTasks);

        //then
        int exceptionCount = 0;
        for (Future<Boolean> future : bookmarkFutures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                exceptionCount++;
            }
        }

        Assertions.assertThat(exceptionCount).isZero();
        Assertions.assertThat(bookmarkRepository.existsByQuestionEntityIdAndUserEntityId(question.getId(), viewer.getId())).isEqualTo(expected);
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
}