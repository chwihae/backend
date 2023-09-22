package com.chwihae.service.bookmark;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.AbstractConcurrencyTest;
import com.chwihae.infra.fixture.UserEntityFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

class BookmarkServiceConcurrencyTest extends AbstractConcurrencyTest {

    @AfterEach
    void tearDown() {
        bookmarkRepository.physicallyDeleteAll();
        questionRepository.physicallyDeleteAll();
        userRepository.physicallyDeleteAll();
        executorService.shutdown();
    }

    @Test
    @DisplayName("북마크 요청을 동시에 해도 순서대로 실행된다")
    void bookmark_withConcurrency() throws Exception {
        //given
        final int REQUEST_COUNT = 10;
        List<Callable<Boolean>> bookmarkTasks = new ArrayList<>();

        UserEntity questioner = UserEntityFixture.of();
        UserEntity viewer = UserEntityFixture.of();
        userRepository.saveAll(List.of(questioner, viewer));
        QuestionEntity question = questionRepository.save(createQuestion(questioner));

        IntStream.range(0, REQUEST_COUNT).forEach(req -> {
            bookmarkTasks.add(() -> bookmarkService.bookmark(question.getId(), viewer.getId()));
        });

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

        Assertions.assertThat(bookmarkRepository.findAll()).isEmpty();
        Assertions.assertThat(exceptionCount).isZero();
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