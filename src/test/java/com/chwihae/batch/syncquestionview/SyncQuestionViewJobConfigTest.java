package com.chwihae.batch.syncquestionview;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.fixture.QuestionEntityFixture;
import com.chwihae.infra.fixture.QuestionViewFixture;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.infra.test.AbstractBatchTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;

import java.util.List;
import java.util.stream.IntStream;

class SyncQuestionViewJobConfigTest extends AbstractBatchTest {

    @AfterEach
    void tearDown() {
        questionViewCacheRepository.clear();
        questionViewRepository.physicallyDeleteAll();
        questionRepository.physicallyDeleteAll();
        userRepository.physicallyDeleteAll();
    }

    @Test
    @DisplayName("여러 청크를 걸쳐서 캐싱되어 있는 질문 조회수를 DB에 동기화한다")
    void syncQuestionViewStep() throws Exception {
        // given
        final int totalItems = SyncQuestionViewJobConfig.CHUNK_SIZE * 2 + 5; // 2.5 청크의 데이터 생성
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        List<QuestionEntity> questionEntityList = IntStream.range(0, totalItems)
                .mapToObj(count -> QuestionEntityFixture.of(userEntity))
                .toList();
        questionRepository.saveAll(questionEntityList);

        List<QuestionViewEntity> questionViewEntityList = IntStream.range(0, totalItems)
                .mapToObj(index -> QuestionViewFixture.of(questionEntityList.get(index)))
                .toList();
        questionViewRepository.saveAll(questionViewEntityList);

        // caching
        IntStream.range(0, totalItems).forEach(index -> {
            questionViewCacheRepository.setViewCount(questionEntityList.get(index).getId(), (long) index);
        });

        // when: job 실행
        JobExecution jobExecution = jobLauncher.run(syncQuestionViewJob, getJobParameters());

        // then: 청크 사이즈만큼 업데이트가 잘 이루어졌는지 확인
        Assertions.assertThat(jobExecution.getExitStatus()).isEqualByComparingTo(ExitStatus.COMPLETED);
        List<Long> viewCounts = questionViewRepository.findAll().stream()
                .map(QuestionViewEntity::getViewCount)
                .toList();
        IntStream.range(0, totalItems).forEach(index -> {
            Assertions.assertThat(viewCounts).contains((long) index);
        });
    }
}