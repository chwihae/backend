package com.chwihae.batch.syncquestionview;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.fixture.QuestionEntityFixture;
import com.chwihae.infra.fixture.QuestionViewFixture;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.infra.test.AbstractBatchTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;

import java.util.List;
import java.util.stream.IntStream;

class SyncQuestionViewJobConfigTest extends AbstractBatchTest {

    @Test
    @DisplayName("캐싱되어 있는 질문 조회수를 DB로 동기화한다")
    void syncQuestionViewStep() throws Exception {
        //given
        final int QUESTION_COUNT = 100;

        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        List<QuestionEntity> questionEntityList = IntStream.range(0, QUESTION_COUNT)
                .mapToObj(count -> QuestionEntityFixture.of(userEntity))
                .toList();
        questionRepository.saveAll(questionEntityList);

        List<QuestionViewEntity> questionViewEntityList = IntStream.range(0, QUESTION_COUNT)
                .mapToObj(index -> QuestionViewFixture.of(questionEntityList.get(index)))
                .toList();
        questionViewRepository.saveAll(questionViewEntityList);

        // caching
        IntStream.range(0, QUESTION_COUNT).forEach(index -> {
            questionViewCacheRepository.setViewCount(questionEntityList.get(index).getId(), (long) index);
        });

        //when
        JobExecution jobExecution = jobLauncher.run(syncQuestionViewJob, getJobParameters());

        //then
        Assertions.assertThat(jobExecution.getExitStatus()).isEqualByComparingTo(ExitStatus.COMPLETED);
        List<Long> viewCounts = questionViewRepository.findAll().stream()
                .map(QuestionViewEntity::getViewCount)
                .toList();
        IntStream.range(0, QUESTION_COUNT).forEach(index -> {
            Assertions.assertThat(viewCounts).contains((long) index);
        });
    }
}