package com.chwihae.batch.syncquestionview;

import com.chwihae.config.redis.QuestionViewCacheRepository;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.question.QuestionViewRepository;
import com.chwihae.dto.question.response.QuestionViewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SyncQuestionViewJobConfig {

    public static final int CHUNK_SIZE = 20;
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
                .reader(syncQuestionViewItemReader())
                .writer(syncQuestionViewItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<QuestionViewResponse> syncQuestionViewItemReader() {
        return new SyncQuestionViewItemReader(questionViewRedisTemplate, questionViewCacheRepository, CHUNK_SIZE);
    }

    @Bean
    @StepScope
    public ItemWriter<QuestionViewResponse> syncQuestionViewItemWriter() {
        return items -> {
            if (CollectionUtils.isEmpty(items.getItems())) {
                return;
            }

            List<Long> ids = items.getItems().stream().map(QuestionViewResponse::getQuestionId).toList();
            List<QuestionViewEntity> questionViewEntityList = questionViewRepository.findByQuestionEntityIds(ids);

            Map<Long, QuestionViewResponse> itemIdToResponseMap = items.getItems().stream()
                    .collect(Collectors.toMap(
                            QuestionViewResponse::getQuestionId,
                            Function.identity(),
                            (existing, replacement) -> existing.getViewCount() >= replacement.getViewCount() ? existing : replacement));

            for (QuestionViewEntity questionViewEntity : questionViewEntityList) {
                QuestionViewResponse correspondingResponse = itemIdToResponseMap.get(questionViewEntity.getQuestionId());

                if (Objects.nonNull(correspondingResponse) && correspondingResponse.getViewCount() > questionViewEntity.getViewCount()) {
                    questionViewEntity.setViewCount(correspondingResponse.getViewCount());
                }
            }
            questionViewRepository.saveAll(questionViewEntityList);
        };
    }
}
