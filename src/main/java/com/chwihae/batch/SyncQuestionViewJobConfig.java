package com.chwihae.batch;

import com.chwihae.config.redis.QuestionViewCacheRepository;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.question.QuestionViewRepository;
import com.chwihae.dto.question.response.QuestionViewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SyncQuestionViewJobConfig {

    public final int CHUNK_SIZE = 20;
    private final JobRepository jobRepository;
    private final RedisTemplate<String, Long> questionViewRedisTemplate;
    private final QuestionViewRepository questionViewRepository;
    private final QuestionViewCacheRepository questionViewCacheRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean(name = "syncQuestionViewJob")
    public Job syncQuestionViewJob() {
        return new JobBuilder("syncQuestionViewJob", jobRepository)
                .start(syncQuestionViewStep())
                .build();
    }

    @Bean(name = "syncQuestionViewStep")
    public Step syncQuestionViewStep() {
        return new StepBuilder("syncQuestionViewStep", jobRepository)
                .repository(jobRepository)
                .<QuestionViewResponse, QuestionViewResponse>chunk(CHUNK_SIZE, transactionManager)
                .reader(itemReader())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public ItemReader<QuestionViewResponse> itemReader() {
        return new SyncQuestionViewItemReader(questionViewRedisTemplate, questionViewCacheRepository, CHUNK_SIZE);
    }

    @Bean
    public ItemWriter<QuestionViewResponse> itemWriter() {
        return items -> {
            List<Long> ids = items.getItems().stream().map(QuestionViewResponse::getQuestionId).toList();
            List<QuestionViewEntity> questionViewEntityList = questionViewRepository.findByQuestionEntityIds(ids);

            Map<Long, QuestionViewResponse> itemIdToResponseMap = items.getItems().stream()
                    .collect(Collectors.toMap(
                            QuestionViewResponse::getQuestionId,
                            Function.identity(),
                            (existing, replacement) -> existing.getViewCount() >= replacement.getViewCount() ? existing : replacement));

            for (QuestionViewEntity questionViewEntity : questionViewEntityList) {
                QuestionViewResponse correspondingResponse = itemIdToResponseMap.get(questionViewEntity.getQuestionId());

                if (correspondingResponse != null && correspondingResponse.getViewCount() > questionViewEntity.getViewCount()) {
                    questionViewEntity.setViewCount(correspondingResponse.getViewCount());
                }
            }
            questionViewRepository.saveAll(questionViewEntityList);
        };
    }
}
