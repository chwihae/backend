package com.chwihae.batch;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.infra.test.AbstractBatchTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static com.chwihae.infra.utils.TimeUtils.KST;

class CloseExpiredQuestionJobConfigTest extends AbstractBatchTest {

    @AfterEach
    void tearDown() {
        questionRepository.physicallyDeleteAll();
        userRepository.physicallyDeleteAll();
    }

    @Test
    @DisplayName("마감 시간이 지난 질문들을 마감 처리한다")
    void closeQuestionStep() throws Exception {
        //given
        final int CHUNK_SIZE = 100;
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());

        LocalDateTime closeAt = LocalDateTime.now(KST).minusDays(1);
        List<QuestionEntity> questionEntityList =
                IntStream.range(0, CHUNK_SIZE)
                        .mapToObj(question -> createQuestion(userEntity, closeAt))
                        .toList();

        questionRepository.saveAll(questionEntityList);

        //when
        JobExecution jobExecution = jobLauncher.run(closeQuestionJob, getJobParameters());

        //then
        Assertions.assertThat(jobExecution.getExitStatus()).isEqualByComparingTo(ExitStatus.COMPLETED);
    }

    public QuestionEntity createQuestion(UserEntity userEntity, LocalDateTime closeAt) {
        return QuestionEntity.builder()
                .userEntity(userEntity)
                .title("title")
                .content("content")
                .closeAt(closeAt)
                .type(QuestionType.ETC)
                .build();
    }
}
