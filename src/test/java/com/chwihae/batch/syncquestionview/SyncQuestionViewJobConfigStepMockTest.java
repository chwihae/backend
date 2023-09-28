package com.chwihae.batch.syncquestionview;

import com.chwihae.config.redis.QuestionViewCacheRepository;
import com.chwihae.domain.question.QuestionViewRepository;
import com.chwihae.dto.question.response.QuestionViewResponse;
import com.chwihae.infra.test.AbstractMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;

class SyncQuestionViewJobConfigStepMockTest extends AbstractMockTest {

    @InjectMocks
    SyncQuestionViewJobConfig syncQuestionViewJobConfig;

    @Mock
    JobRepository mockJobRepository;

    @Mock
    RedisTemplate<String, Long> mockQuestionViewRedisTemplate;

    @Mock
    SyncQuestionViewItemReader syncQuestionViewItemReader;

    @Mock
    QuestionViewRepository mockQuestionViewRepository;

    @Mock
    QuestionViewCacheRepository mockQuestionViewCacheRepository;

    @Mock
    PlatformTransactionManager mockTransactionManager;

    @Test
    @DisplayName("syncQuestionViewStep()는 올바르게 스텝을 구성한다.")
    void syncQuestionViewStepCreationTest() {
        //when
        Step resultStep = syncQuestionViewJobConfig.syncQuestionViewStep();

        //then
        assertThat(resultStep).isNotNull();
        assertThat(resultStep.getName()).isEqualTo("syncQuestionViewStep");
    }

    @Test
    @DisplayName("syncQuestionViewItemReader()는 올바르게 리더를 생성한다.")
    void syncQuestionViewItemReader() {
        //when
        ItemReader<QuestionViewResponse> resultReader = syncQuestionViewJobConfig.syncQuestionViewItemReader();

        //then
        assertThat(resultReader).isNotNull();
    }
}
