package com.chwihae.batch;

import com.chwihae.domain.question.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.chwihae.utils.TimeUtils.KST;

@Slf4j
@RequiredArgsConstructor
@Component
public class CloseQuestionScheduler {

    private static final long ONE_MINUTE_IN_MILLISECONDS = 60 * 1000L;
    private final JobLauncher jobLauncher;
    private final Job closeQuestionJob;
    private final QuestionRepository questionRepository;

    @Scheduled(fixedDelay = ONE_MINUTE_IN_MILLISECONDS)
    public void closeQuestion() {
        if (questionRepository.existsByCloseAtBefore(LocalDateTime.now(KST))) {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            try {
                jobLauncher.run(closeQuestionJob, jobParameters);
            } catch (JobExecutionException ex) {
                log.error("Error executing closeQuestion job", ex);
            }
        }
    }
}
