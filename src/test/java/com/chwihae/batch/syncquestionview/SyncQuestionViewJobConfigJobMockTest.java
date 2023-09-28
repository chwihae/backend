package com.chwihae.batch.syncquestionview;

import com.chwihae.config.redis.QuestionViewCacheRepository;
import com.chwihae.domain.question.QuestionViewRepository;
import com.chwihae.infra.test.AbstractMockTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;

class SyncQuestionViewJobConfigJobMockTest extends AbstractMockTest {

    @InjectMocks
    SyncQuestionViewJobConfig jobConfig;

    @Mock
    Step mockStep;

    @Mock
    JobRepository jobRepository;

    @Mock
    RedisTemplate<String, Long> questionViewRedisTemplate;

    @Mock
    QuestionViewRepository questionViewRepository;

    @Mock
    QuestionViewCacheRepository questionViewCacheRepository;

    @Mock
    PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("SyncQuestionViewJob 구성 확인")
    void syncQuestionViewJob() {
        //when
        Job job = jobConfig.syncQuestionViewJob();

        //then
        Assertions.assertThat(job).isNotNull();
        Assertions.assertThat(job.getName()).isEqualTo("syncQuestionViewJob");
    }
}