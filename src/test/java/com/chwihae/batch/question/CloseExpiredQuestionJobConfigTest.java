package com.chwihae.batch.question;

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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());

        List<QuestionEntity> questionEntityList = new ArrayList<>();
        IntStream.range(0, CHUNK_SIZE).forEach(question -> {
            questionEntityList.add(createQuestion(userEntity, closeAt));
        });
        questionRepository.saveAll(questionEntityList);

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("closeQuestionStep");

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
