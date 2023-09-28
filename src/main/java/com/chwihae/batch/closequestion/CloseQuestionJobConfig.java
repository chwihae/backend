package com.chwihae.batch.closequestion;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Collections;

import static com.chwihae.utils.TimeUtils.KST;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class CloseQuestionJobConfig {

    private final int CHUNK_SIZE = 50;
    private final JobRepository jobRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager transactionManager;
    private final QuestionRepository questionRepository;

    @Bean(name = "closeQuestionJob")
    public Job closeQuestionJob() {
        return new JobBuilder("closeQuestionJob", jobRepository)
                .start(closeQuestionStep())
                .build();
    }

    // TODO 재시도 테스트
    @Bean(name = "closeQuestionStep")
    public Step closeQuestionStep() {
        return new StepBuilder("closeQuestionStep", jobRepository)
                .repository(jobRepository)
                .<QuestionEntity, QuestionEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(closeQuestionItemReader())
                .writer(closeQuestionItemWriter())
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .skipLimit(10)
                .skip(Exception.class)
                .listener(new SkipListener<>() {
                    @Override
                    public void onSkipInRead(Throwable t) {
                        log.error("Skip during READ", t);
                    }

                    @Override
                    public void onSkipInWrite(QuestionEntity item, Throwable t) {
                        log.error("Skip during WRITE for item: {}", item, t);
                    }

                    @Override
                    public void onSkipInProcess(QuestionEntity item, Throwable t) {
                        log.error("Skip during PROCESS for item: {}", item, t);
                    }
                })
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<QuestionEntity> closeQuestionItemReader() {
        return new JpaPagingItemReaderBuilder<QuestionEntity>()
                .name("closeQuestionItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT qe " +
                        "FROM QuestionEntity qe " +
                        "WHERE qe.closeAt < :now")
                .parameterValues(Collections.singletonMap("now", LocalDateTime.now(KST)))
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    public ItemWriter<QuestionEntity> closeQuestionItemWriter() {
        return questionEntities -> {
            for (QuestionEntity questionEntity : questionEntities) {
                questionEntity.close();
            }
            questionRepository.saveAll(questionEntities);
        };
    }
}
