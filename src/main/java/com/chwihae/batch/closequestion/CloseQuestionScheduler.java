package com.chwihae.batch.closequestion;

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

    private final JobLauncher jobLauncher;
    private final Job closeQuestionJob;
    private final QuestionRepository questionRepository;

    @Scheduled(cron = "0/30 * * * * ?")
    public void closeQuestion() {
        if (questionRepository.existsByCloseAtBefore(LocalDateTime.now(KST))) {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            try {
                log.info("Close expired question job started");
                jobLauncher.run(closeQuestionJob, jobParameters);
                log.info("Close expired question job finished");
            } catch (JobExecutionException ex) {
                log.error("Error executing closeQuestion job", ex);
            }
        }
    }
}
