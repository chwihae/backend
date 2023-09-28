package com.chwihae.service.question.core;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.fixture.QuestionEntityFixture;
import com.chwihae.infra.fixture.QuestionViewFixture;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.infra.test.AbstractConcurrencyTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

class QuestionViewServiceConcurrencyTest extends AbstractConcurrencyTest {

    @AfterEach
    void tearDown() {
        questionViewCacheRepository.clear();
        questionViewRepository.physicallyDeleteAll();
        questionRepository.physicallyDeleteAll();
        userRepository.physicallyDeleteAll();
    }

    @Test
    @DisplayName("조회 수를 동시에 증가 요청해도 정확하게 증가한다")
    void incrementViewCount_concurrency() throws Exception {
        //given
        final int TOTAL_REQUEST_COUNT = 100;
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
        QuestionViewEntity view = QuestionViewFixture.of(question);
        long viewCount = 0;
        view.setViewCount(viewCount);
        questionViewRepository.save(view);
        questionViewCacheRepository.setViewCount(question.getId(), viewCount);

        List<Callable<Void>> incrementViewCountTasks = doGenerateConcurrentTasks(TOTAL_REQUEST_COUNT, () -> {
            questionViewService.incrementViewCount(question.getId());
            return null;
        });

        //when
        List<Future<Void>> futures = executorService.invokeAll(incrementViewCountTasks);

        //then
        Assertions.assertThat(questionViewCacheRepository.getViewCount(question.getId()))
                .isPresent()
                .hasValueSatisfying(
                        it -> {
                            Assertions.assertThat(it).isEqualTo(TOTAL_REQUEST_COUNT);
                        }
                );
    }
}